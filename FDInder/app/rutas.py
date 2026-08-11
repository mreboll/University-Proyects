"""
Modulo de Python que contiene el enrutado de Fdinder
"""
import os
import uuid

from flask import current_app as app, render_template, redirect, url_for, flash, abort, request, make_response, \
    send_from_directory
from flask_login import login_user, logout_user, login_required, current_user
from . import query, login_manager
from .modelos import Persona
from .formularios import SignInForm, SignupForm, MensajeForm, ConfirmarMatchForm, DEFAULT_FOTO_URL


# NOTA: todas las consultas a Neo4j se realizan a traves de la funcion query() definida
# en app/__init__.py. Dicha funcion envuelve driver.execute_query fijando siempre la base
# de datos configurada en NEO4J_DATABASE, de forma que no es necesario pasarla aqui.
# Cada llamada abre y cierra su propia transaccion automaticamente (sin session explicita).


@app.route('/profiles/<path:filename>')
@login_required
def foto_perfil(filename: str):
    """
    NO HAY QUE MODIFICAR.

    Sirve fotos de perfil almacenadas fuera de "static/" (requiere autenticacion).
    """
    return send_from_directory(
        os.path.join(app.root_path, 'profiles'),
        filename
    )


@login_manager.user_loader
def carga_usuario(id_usuario: str):
    """
    Tal y como indican los apuntes, debe cargar el objeto Persona asociado
    a id_usuario si existe, o devolver None en caso contrario. Para ello, utiliza
    el metodo Persona.from_node().
    """

    res = query("""
                MATCH (p:Persona {id: $id}) 
                RETURN p
            """,
                id=id_usuario)

    if res.records:
        return Persona.from_node(res.records[0]['p'])
    return None


@app.route('/')
@app.route('/log_in', methods=['GET', 'POST'])
def log_in():
    """
    Acceso de un usuario ya registrado.
    Acepta tanto peticiones GET como POST.

    En esta funcion de vista se renderiza el formulario de acceso (SignInForm) con el
    template 'log_in.html'. Una vez el usuario introduzca datos correctamente,
    se comprueba si hay un nodo Persona con ese email en Neo4j.
    - Si no existe: mensaje flash "El email introducido no tiene un usuario asociado".
    - Si existe pero la contrasena es incorrecta: mensaje flash "Contraseña incorrecta".
    - Si las credenciales son correctas: login y redireccion a 'explorar'.
    """

    form = SignInForm()

    if form.validate_on_submit():
        res = query("""
                    MATCH (p:Persona {email: $email})
                    RETURN p
                """,
                    email=form.email.data)

        if not res.records:
            flash("El email introducido no tiene un usuario asociado")
        else:
            persona = Persona.from_node(res.records[0]['p'])

            if persona.check_password(form.password.data):
                login_user(persona)
                return redirect(url_for('explorar'))
            else:
                flash("Contraseña incorrecta")

    return render_template('log_in.html', form=form)


@app.route('/sign_up', methods=['GET', 'POST'])
def sign_up():
    """
    Registro completo de un usuario. Acepta GET y POST.
    Para ello, se utiliza el formulario SignupForm.

    En esta funcion de vista renderiza el formulario de acceso (SignupForm) con el
    template 'sign_up.html'.

    - Para peticiones GET o si el formulario no se valida correctamente, se renderiza este template.

    - Si el formulario se valida correctamente, se debe comprobar si existe ya un usuario con ese email.
      En tal caso, se lanza el mensaje flash "Ya existe un usuario con este email" y se renderiza el template.

        Si no existe, se guarda la foto en la carpeta "profiles/" con el siguiente codigo:

        nuevo_id = str(uuid.uuid4())
        archivo = form.data["foto"]
        if archivo and archivo.filename:
            ext = archivo.filename.rsplit('.', 1)[-1].lower()
            archivo.save(os.path.join(os.path.dirname(os.path.realpath(__file__)), 'profiles', f'{nuevo_id}.{ext}'))
            foto_url = f'/profiles/{nuevo_id}.{ext}'
        else:
            foto_url = DEFAULT_FOTO_URL

        y se crean los siguientes nodos y relaciones:

        * Nodo Persona con toda la informacion, incluyendo la variable foto_url del codigo anterior. Utiliza como id la
          variable "nuevo_id" anterior.
        * Informacion para denotar la edad (nodo Edad y relacion TIENE_EDAD).
        * Informacion para denotar las preferencias de edad (nodo Edad y relacion BUSCA_EDAD). Recuerda el operador UNWIND.

      Finalmente hace login y redirige a explorar.
    """

    form = SignupForm()

    if form.validate_on_submit():
        res = query("""
                    MATCH (p:Persona {email: $email})
                    RETURN p
                """,
                    email=form.email.data)

        if res.records:
            flash("Ya existe un usuario con este email")

        else:
            nuevo_id = str(uuid.uuid4())
            archivo = form.data["foto"]
            if archivo and archivo.filename:
                ext = archivo.filename.rsplit('.', 1)[-1].lower()
                archivo.save(os.path.join(os.path.dirname(os.path.realpath(__file__)), 'profiles', f'{nuevo_id}.{ext}'))
                foto_url = f'/profiles/{nuevo_id}.{ext}'
            else:
                foto_url = DEFAULT_FOTO_URL

            nueva_persona = Persona(id=nuevo_id, nombre=form.nombre.data, email=form.email.data)
            nueva_persona.password = form.password.data
            rango_edades = list(range(form.busca_edad_min.data, form.busca_edad_max.data + 1))
            # probando el proyecto me he dado cuenta que te deja crear un usuario donde busca_edad_min > busca_edad_max. Esto hace que sea imposible encontrar a nadie. Se arregla facilmente sacando una excepcion desde formularios.py

            query("""
                MERGE (p:Persona {id: $id})
                SET p.nombre = $nombre, 
                    p.email = $email, 
                    p.password_hash = $hash, 
                    p.foto_url = $foto
                MERGE (edad:Edad {valor: $edad})
                MERGE (p)-[:TIENE_EDAD]->(edad)
                WITH p
                UNWIND $rango as e
                MERGE (busca_edad:Edad {valor: e})
                MERGE (p)-[:BUSCA_EDAD]->(busca_edad)
            """,
                id=nuevo_id,
                nombre=form.nombre.data,
                email=form.email.data,
                hash=nueva_persona.password_hash,
                foto=foto_url,
                edad=form.edad.data,
                rango=rango_edades)

            login_user(nueva_persona)
            return redirect(url_for('explorar'))

    return render_template('sign_up.html', form=form)


@app.route('/log_out')
@login_required
def log_out():
    """
    Desconexion de un usuario. Requiere estar autenticado.
    Hace logout, manda un mensaje flash y redirige al login.
    """
    logout_user()
    flash("Sesión cerrada")
    return redirect(url_for('log_in'))


@app.route('/explorar')
@login_required
def explorar():
    """
    Ruta que muestra 100 candidatos compatibles con el usuario actual.

    Los candidatos a mostrar deben cumplir las siguientes restricciones:

    * Los candidatos compatibles tienen que tener la edad de acuerdo con las
    edades que busca el usuario actual (BUSCA_EDAD) y viceversa.

    * El usuario actual no ha intentado hacer match o ha rechazado previamente al candidato
      (relaciones QUIERE_MATCH y HA_RECHAZADO).

    * El usuario actual no esta actualmente en un MatchActivo (con la relacion :ACEPTA) con el candidato.

    Además, los candidatos se deben ordenar por el numero de personas
    'personas en comun' de forma descendente. Las personas comunes son aquellas
    que estan conectadas a traves de a lo sumo 4 relaciones :QUIERE_MATCH del candidato,
    y a otras cuatro relaciones a lo sumo del usuario actual (da igual el orden de las relaciones).

    Como resultado, se renderiza el template "explorar.html". Si el campo 'foto_url' es None, entonces
    se debe asignar el valor DEFAULT_FOTO_URL.
    """

    res = query("""
                MATCH (me:Persona {id: $me_id})-[:TIENE_EDAD]->(mi_edad:Edad)
                MATCH (c:Persona)-[:TIENE_EDAD]->(su_edad:Edad)
                MATCH (me)-[:BUSCA_EDAD]->(su_edad)
                MATCH (c)-[:BUSCA_EDAD]->(mi_edad)
                WHERE me <> c
                AND NOT (me)-[:QUIERE_MATCH|HA_RECHAZADO]->(c)
                AND NOT (me)-[:ACEPTA]-(:MatchActivo)-[:ACEPTA]-(c)
                OPTIONAL MATCH (me)-[:QUIERE_MATCH*..4]-(comun:Persona)-[:QUIERE_MATCH*..4]-(c)
                WHERE comun <> me AND comun <> c
                WITH c, su_edad, count(DISTINCT comun) as personas_comunes
                ORDER BY personas_comunes DESC
                LIMIT 100
                RETURN c.id as id, c.nombre as nombre, c.foto_url as foto_url, su_edad.valor as edad, personas_comunes
            """,
                me_id=current_user.id)

    candidatos = []
    for n in res.records:
        candidatos.append({
            'id': n['id'],
            'nombre': n['nombre'],
            'foto_url': n['foto_url'] if n['foto_url'] else DEFAULT_FOTO_URL,
            'edad': n['edad'],
            'personas_comunes': n['personas_comunes']
        })

    return render_template("explorar.html", candidatos=candidatos)


@app.route('/aceptar/<id_persona>', methods=['POST'])
@login_required
def aceptar(id_persona: str):
    """
    Swipe right sobre una Persona.

    Se siguen los siguientes pasos:
      1. Si la persona no existe, se aborta con un error 404.

      2. Se debe detectar si se ha formado un ciclo con la persona con la que se quiere hacer match.
         Aqui distinguimos varios casos:

        2.1 Sin ciclo: Se crea la relacion (yo)-[:QUIERE_MATCH]->(candidato) con MERGE a la espera de que
                       la otra persona tambien acepte (o no).
        2.2 Si hay un ciclo de longitud 1 (el candidato tambien ha hecho Swipe y es mutuo): match directo entre
                                  dos personas, se crea MatchActivo{estado:"activo"} con ACEPTA con confirmado:true.
        2.3 Si todos los ciclos son de longitud > 1: match poliamoroso, se crea MatchActivo con {estado:"pendiente"}
                                  y todos los participantes del ciclo reciben ACEPTA con confirmado:false.

      En los casos 2.2 y 2.3 se eliminan todos los QUIERE_MATCH entre participantes.

      Como resultado, la funcion devuelve una respuesta 204 con make_response('', 204)
    """

    res = query("""
                MATCH (c:Persona {id: $id})
                RETURN c
            """,
                id=id_persona)

    if not res.records:
        abort(404)

    ciclo_res = query("""
                    MATCH p = shortestPath((c:Persona {id: $c_id})-[:QUIERE_MATCH*]->(me:Persona {id: $me_id}))
                    RETURN p
                """,
                    c_id=id_persona,
                    me_id=current_user.id)

    if ciclo_res.records:
        ciclo = ciclo_res.records[0]['p']
        participantes = [n['id'] for n in ciclo.nodes]
        longitud = len(ciclo)
        match_id = str(uuid.uuid4())
        if longitud == 1:
            estado = 'activo'
        else:
            estado = 'pendiente'

        query("""
                MATCH (me:Persona {id: $me_id})
                MATCH (c:Persona {id: $c_id})
                CREATE (m:MatchActivo {id: $m_id, estado: $estado, fecha_creacion: datetime()})
                WITH m
                UNWIND $participantes as p_id
                MATCH (p:Persona {id: p_id})
                CREATE (p)-[:ACEPTA {confirmado: ($longitud = 1)}]->(m)
            """,
                me_id=current_user.id,
                c_id=id_persona,
                m_id=match_id,
                estado=estado,
                participantes=participantes,
                longitud=longitud)

        query("""
                UNWIND $participantes as p1_id
                UNWIND $participantes as p2_id
                MATCH (per1:Persona {id: p1_id})-[r:QUIERE_MATCH]->(per2:Persona {id: p2_id})
                DELETE r
            """,
                participantes=participantes)

    else:
        query("""
                MATCH (me:Persona {id: $me_id}), (c:Persona {id: $c_id})
                MERGE (me)-[:QUIERE_MATCH]->(c)
            """,
                me_id=current_user.id,
                c_id=id_persona)

    return make_response('', 204)


@app.route('/rechazar/<id_persona>', methods=['POST'])
@login_required
def rechazar(id_persona: str):
    """
    Swipe left sobre una Persona. Solo acepta POST.
    Registra la relacion HA_RECHAZADO con MERGE para que ese
    candidato no vuelva a aparecer en /explorar. No hace falta gestionar el caso de que
    el usuario ya haya rechazado previamente a la persona (MERGE repite la creacion).

    Como resultado, la funcion devuelve una respuesta 204 con make_response('', 204)
    """
    res = query("""
                MATCH (c:Persona {id: $id})
                RETURN c
            """,
                id=id_persona)

    if not res.records:
        abort(404)

    query("""
            MATCH (me:Persona {id: $me_id}), (c:Persona {id: $c_id})
            MERGE (me)-[:HA_RECHAZADO]->(c)
        """,
            me_id=current_user.id,
            c_id=id_persona)

    return make_response('', 204)


@app.route('/matches')
@login_required
def matches():
    """
    Lista los matches del usuario actual. Solo acepta GET.
    Muestra tanto los matches activos como los pendientes de confirmar.
    Para cada match se carga: resto de participantes, numero de mensajes
    y fecha del ultimo mensaje. Los resultados se deben ordenar por la fecha
    del ultimo mensaje de forma descendente (la funcion max sobre fechas devuelve la
    fecha mas reciente).

    Como resultado, se debe renderizar el template "matches.html".
    """

    res = query("""
            MATCH (me:Persona {id: $me_id})-[:ACEPTA]->(m:MatchActivo)
            MATCH (otros:Persona)-[:ACEPTA]->(m)
            WHERE otros <> me
            OPTIONAL MATCH (m)<-[:PARA]-(msg:Mensaje)
            WITH m, 
                 collect(DISTINCT otros) as lista_otros, 
                 count(DISTINCT msg) as total_msg, 
                 max(msg.fecha_envio) as fecha_ult  
            ORDER BY fecha_ult DESC, m.fecha_creacion DESC  //de esta forma consigo ordenar los matches que aun no tengan mensajes
                                                            //(aparecen ordenados desde el mas nuevo hasta al mas antiguo al final del codigo)
            RETURN m, lista_otros, total_msg, fecha_ult
        """,
            me_id=current_user.id)

    lista_matches = []
    for n in res.records:
        lista_matches.append({
            'm': n['m'],
            'otros': [
                {'id': p['id'], 'nombre': p['nombre'], 'foto_url': p.get('foto_url', DEFAULT_FOTO_URL)}
                for p in n['lista_otros']
            ],
            'num_mensajes': n['total_msg'],
            'ultimo_mensaje': n['fecha_ult']
        })

    return render_template("matches.html", matches=lista_matches)


@app.route('/match/<id_match>', methods=['GET', 'POST'])
@login_required
def match(id_match: str):
    """
    Ruta que explora un match concreto. Acepta tanto metodos GET como POST porque permite visualizar
    participantes y mensajes (GET) o enviar mensajes utilizando el formulario MensajeForm (POST).

    Devuelve 404 si el match no existe o el usuario no participa.


    * Si se manda una peticion POST, el formulario se valida correctamente y el estado del match es activo (y no pendiente),
      se debe crear un mensaje por parte del usuario actual a ese match. Como resultado, se redirige a la ruta "math/<id-match>"
      con el match actual.

    * En caso contrario, se renderiza el template "match_detalle.html". Para ello, hay que cargar los mensajes
      cronologicamente de forma ascendente (del mas antiguo al mas nuevo).
    """

    form = MensajeForm()

    res = query("""
                MATCH (p:Persona)-[:ACEPTA]->(m:MatchActivo {id: $m_id})
                WITH m, collect(p) as personas
                WHERE any(c in personas WHERE c.id = $me_id)
                RETURN m, personas
            """,
                m_id=id_match,
                me_id=current_user.id)

    if not res.records:
        abort(404)

    match_nodo = res.records[0]['m']
    participantes = [
        {'id': n['id'], 'nombre': n['nombre'], 'foto_url': n.get('foto_url', DEFAULT_FOTO_URL)}
        for n in res.records[0]['personas']
    ]

    if form.validate_on_submit():
        if match_nodo['estado'] == 'activo':
            query("""
                MATCH (p:Persona {id: $me_id}), (m:MatchActivo {id: $m_id})
                CREATE (p)-[:ENVIA]->(msg:Mensaje {contenido: $contenido, fecha_envio: datetime()})-[:PARA]->(m)
            """,
                me_id=current_user.id,
                m_id=id_match,
                contenido=form.contenido.data)

            return redirect(url_for('match', id_match=id_match))

    res_msg = query("""
            MATCH (emisor:Persona)-[:ENVIA]->(msg:Mensaje)-[:PARA]->(m:MatchActivo {id: $m_id})
            RETURN msg, emisor
            ORDER BY msg.fecha_envio ASC
        """,
            m_id=id_match)

    mensajes_chat = []
    for n in res_msg.records:
        mensajes_chat.append({
            'msg': n['msg'],
            'emisor': {
                'id': n['emisor']['id'],
                'nombre': n['emisor']['nombre'],
                'foto_url': n['emisor'].get('foto_url', DEFAULT_FOTO_URL)
            }
        })

    return render_template('match_detalle.html', match=match_nodo, participantes=participantes, mensajes=mensajes_chat, form=form)


@app.route('/confirmar_match/<id_match>', methods=['GET', 'POST'])
@login_required
def confirmar_match(id_match: str):
    """
    Confirmacion o rechazo de un match poliamoroso pendiente. Acepta GET y POST.
    Si no existe un MatchActivo entre el usuario actual y el id del match, entonces
    se aborta con un error 404. Hay que gestionar 3 casos:

    -> GET: muestra el template "confirmar_match.html" con los participantes
            del match pendiente y los botones para aceptar o rechazar. Para ello,
            utilizar el formulario ConfirmarMatchForm.

    -> Con POST, hay dos variantes en funcion del valor del campo 'accion' del formulario:
      * Si el formulario tiene accion='aceptar':
        - Se marca la relacion ACEPTA del usuario actual como confirmado=true.
        - Si todos los participantes han confirmado, el match pasa a estado 'activo'
          y se redirige a la ruta asociada a ese match.
        - Si hay algun participante que no haya confirmado, se redirige a la lista de matches.

      * Si el formulario tiene accion='rechazar':
        - Se crean relaciones HA_RECHAZADO entre todos los participantes para que no
          vuelvan a aparecer entre si en /explorar, y se elimina el MatchActivo directamente.
          Se redirige a la lista de matches.
    """

    form = ConfirmarMatchForm()

    res = query("""
            MATCH (me:Persona {id: $me_id})-[r:ACEPTA]->(m:MatchActivo {id: $m_id})
            RETURN m
        """,
            me_id=current_user.id,
            m_id=id_match)

    if not res.records:
        abort(404)

    match_nodo = res.records[0]['m']
    if form.validate_on_submit():
        if form.accion.data == 'aceptar':
            res_estado = query("""
                    MATCH (me:Persona {id: $me_id})-[r:ACEPTA]->(m:MatchActivo {id: $m_id})
                    SET r.confirmado = true
                    WITH m
                    MATCH (otros:Persona)-[relacion:ACEPTA]->(m)
                    WITH m, sum(CASE WHEN relacion.confirmado = false THEN 1 ELSE 0 END) as no
                    SET m.estado = CASE WHEN no = 0 THEN 'activo' ELSE m.estado END
                    RETURN m.estado as estado
                """,
                    me_id=current_user.id,
                    m_id=id_match)

            if res_estado.records[0]['estado'] == 'activo':
                return redirect(url_for('match', id_match=id_match))

            return redirect(url_for('matches'))

        elif form.accion.data == 'rechazar':
            query("""
                    MATCH (p:Persona)-[:ACEPTA]->(m:MatchActivo {id: $m_id})
                    WITH collect(p) as participantes, m
                    UNWIND participantes as p1
                    UNWIND participantes as p2
                    WITH p1, p2, m WHERE p1 <> p2
                    MERGE (p1)-[:HA_RECHAZADO]->(p2)
                    WITH DISTINCT m
                    DETACH DELETE m
                """,
                    m_id=id_match)

            return redirect(url_for('matches'))

    res_part = query("""
                    MATCH (p:Persona)-[r:ACEPTA]->(m:MatchActivo {id: $m_id})
                    RETURN p, r.confirmado as confirmado
                """,
                    m_id=id_match)

    participantes_info = [
        {'persona': n['p'], 'confirmado': n['confirmado']}
        for n in res_part.records
    ]

    return render_template('confirmar_match.html', match=match_nodo, participantes=participantes_info, form=form)
