"""
Módulo de Python que contiene las rutas
"""
import datetime
import functools
from typing import List

from flask import current_app as app, render_template, redirect, url_for, flash, abort, request
from flask_login import login_user, logout_user, login_required, current_user
from sqlalchemy import select, func, desc
from sqlalchemy.orm import load_only, selectinload, joinedload
from sqlalchemy.exc import IntegrityError
from .formularios import SignInForm, SignupForm
from . import db, login_manager
from .modelos import Pelicula, Persona, Actua, Dirige, Usuario, Contrato

login_manager.login_view = 'sign_in'

@login_manager.user_loader
def carga_usuario(id_usuario: str):
    return db.session.get(Usuario, int(id_usuario))

@app.route('/sign_up', methods=['GET', 'POST'])
def sign_up():
    form = SignupForm()
    if form.validate_on_submit():
        usuario = Usuario(email=form.data["email"])
        usuario.password = form.data["password"]

        try:
            db.session.add(usuario)
            db.session.commit()
            login_user(usuario)
            return redirect(url_for('mostrar_mercado'))
        except IntegrityError:
            db.session.rollback()
            flash("Ya existe un usuario con este email")

    return render_template('sign_up.html', form=form)

@app.route('/sign_in', methods=['GET', 'POST'])
def sign_in():
    form = SignInForm()
    if form.validate_on_submit():
        email = form.data["email"]
        password = form.data["password"]

        usuario = db.first_or_404(select(Usuario).where(Usuario.email == email).options(load_only(Usuario.email,
                                                                                                  Usuario.password_hash))
        )

        if usuario.check_password(password):
            login_user(usuario)
            return redirect(url_for('mostrar_mercado'))
        else:
            flash("Contraseña incorrecta :(")
    return render_template("sign_in.html", form=form)

@app.route("/log_out")
@login_required
def log_out():
    logout_user()
    flash("Te has desconectado correctamente :)")
    return redirect(url_for('listar_peliculas'))

@app.route("/")
@app.route("/peliculas")
def listar_peliculas():
    page = db.paginate(select(Pelicula)
                        .options(load_only(Pelicula.id, Pelicula.titulo, Pelicula.url_imagen, Pelicula.popularidad, Pelicula.ganancias))
                        .order_by(desc(Pelicula.popularidad), desc(Pelicula.ganancias)), per_page=20)

    return render_template('listar_peliculas.html', page=page)

@app.route("/pelicula/<int:id_pelicula>")
def mostrar_pelicula(id_pelicula: int):
    pelicula = db.first_or_404(select(Pelicula)
                                .where(Pelicula.id == id_pelicula)
                                .options(selectinload(Pelicula.reparto).selectinload(Actua.persona), selectinload(Pelicula.directores)
                                .selectinload(Dirige.persona))
    )

    lista_directores = [d.persona for d in pelicula.directores]

    lista_reparto = [
        (r.persona.id, r.persona.nombre, r.papel)
        for r in pelicula.reparto
    ]

    return render_template('mostrar_pelicula_completa.html', pelicula=pelicula, directores=lista_directores, reparto=lista_reparto)

@app.route("/persona/<int:id_persona>")
def mostrar_persona(id_persona: int):
    persona = db.first_or_404(select(Persona)
                                .where(Persona.id == id_persona)
                                .options(selectinload(Persona.peliculas_actuadas)
                                .selectinload(Actua.pelicula), selectinload(Persona.peliculas_dirigidas).selectinload(Dirige.pelicula))
    )

    lista_actuaciones = [
        (a.pelicula.id, a.pelicula.titulo, a.papel)
        for a in persona.peliculas_actuadas
    ]

    lista_direcciones = [
        (d.pelicula.id, d.pelicula.titulo)
        for d in persona.peliculas_dirigidas
    ]

    return render_template('mostrar_persona.html', persona=persona, actuaciones=lista_actuaciones, direcciones=lista_direcciones)

@app.route("/mercado")
@login_required
def mostrar_mercado():
    page = db.paginate(select(Persona)
                        .where(Persona.en_mercado == True)
                        .order_by(desc(Persona.popularidad), Persona.nombre), per_page=16)

    return render_template('mostrar_mercado.html', page=page, presupuesto=current_user.presupuesto)

@app.route('/contratar/<int:id_persona>', methods=["POST"])
@login_required
def contratar_persona(id_persona: int):
    persona = db.session.get(Persona, id_persona)

    if persona is None:
        flash(f"No existe ninguna persona con el id {id_persona}")
        return redirect(url_for('mostrar_mercado'))

    if not persona.en_mercado:
        flash(f"{persona.nombre} no se encuentra actualmente disponible en el mercado")
        return redirect(url_for('mostrar_mercado'))

    contrato_existente = db.session.scalar(select(Contrato).where(Contrato.id_persona == id_persona))

    if contrato_existente is not None:
        if contrato_existente.id_usuario != current_user.id:
            flash(f"{persona.nombre} ya ha sido contratado :(, estate mas atento cuando vuelva a estar a la venta ;)")
        else:
            flash(f"ya has contratado a {persona.nombre} (espabila), en Mis fichajes puedes ver a tus actores contratados")
        return redirect(url_for('mostrar_fichajes'))

    coste = persona.cache

    if current_user.presupuesto < coste:
        flash(f"No hay suficientes fondos: tienes {current_user.presupuesto} y contratar a {persona.nombre} cuesta {coste} :(")
        return redirect(url_for('mostrar_mercado'))

    nuevo_contrato = Contrato(id_persona=id_persona, id_usuario=current_user.id)
    persona.en_mercado = False
    current_user.presupuesto -= coste
    db.session.add(nuevo_contrato)
    db.session.commit()

    flash(f"Se ha contratado correctamente a {persona.nombre} :)")
    return redirect(url_for('mostrar_mercado'))

@app.route('/mis_fichajes')
@login_required
def mostrar_fichajes():
    penalizaciones = {}
    hoy = datetime.date.today()
    lista_personas = []

    page = db.paginate(select(Contrato)
                        .where(Contrato.id_usuario == current_user.id)
                        .options(selectinload(Contrato.persona)).order_by(desc(Contrato.fecha)), per_page=4)

    for contrato in page.items:
        persona = contrato.persona
        dias = (hoy - contrato.fecha).days
        penalizaciones[persona.id] = persona.computar_penalizacion(dias)
        lista_personas.append(persona)

    page.items = lista_personas
    return render_template('mostrar_contratos.html', page=page, penalizaciones=penalizaciones)

@app.route('/despedir/<int:id_persona>', methods=["POST"])
@login_required
def despedir_persona(id_persona: int):
    persona = db.session.get(Persona, id_persona)

    if persona is None:
        flash(f"No existe ninguna persona con id {id_persona}")
        return redirect(url_for('mostrar_fichajes'))

    contrato = db.session.scalars(select(Contrato)
                                    .where(Contrato.id_persona == id_persona)
                                    .where(Contrato.id_usuario == current_user.id)).first()

    if contrato is None:
        flash(f"No tienes contratad@ a {persona.nombre}")
        return redirect(url_for('mostrar_fichajes'))

    dias = (datetime.date.today() - contrato.fecha).days
    penalizacion = persona.computar_penalizacion(dias)
    reembolso = persona.cache - penalizacion
    current_user.presupuesto += reembolso
    persona.en_mercado = True
    db.session.delete(contrato)
    db.session.commit()

    flash(f"Se ha despedido correctamente a {persona.nombre} :)")
    return redirect(url_for('mostrar_fichajes'))

@app.route('/clasificacion')
def clasificacion_usuarios():
    sub_actores = (select(Contrato.id_usuario, Actua.id_pelicula, func.count(Contrato.id_persona).label('num_interp'), func.min(Persona.popularidad).label('min_pop'))
                    .join(Persona, Contrato.id_persona == Persona.id)
                    .join(Actua, Actua.id_persona == Persona.id)
                    .group_by(Contrato.id_usuario, Actua.id_pelicula).subquery())

    sub_pts_interp = (select(sub_actores.c.id_usuario, func.sum(sub_actores.c.num_interp * sub_actores.c.min_pop).label('pts_interp'))
                    .group_by(sub_actores.c.id_usuario).subquery())

    sub_dirs = (select(Contrato.id_usuario, func.count(Contrato.id_persona).label('num_dirs'), func.count(func.distinct(Dirige.id_pelicula)).label('num_pelis'))
                .join(Persona, Contrato.id_persona == Persona.id)
                .join(Dirige, Dirige.id_persona == Persona.id)
                .group_by(Contrato.id_usuario).subquery())

    resultado = db.session.execute(select(Usuario, (func.coalesce(sub_pts_interp.c.pts_interp, 0) + func.coalesce(sub_dirs.c.num_dirs * sub_dirs.c.num_pelis, 0)).label('puntuacion'))
                                    .outerjoin(sub_pts_interp, sub_pts_interp.c.id_usuario == Usuario.id)
                                    .outerjoin(sub_dirs, sub_dirs.c.id_usuario == Usuario.id)
                                    .order_by(desc('puntuacion'))).all()

    return render_template('clasificacion.html', ranking=resultado)