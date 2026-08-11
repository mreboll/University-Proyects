from csv import DictReader
from app import create_app, db
from app.modelos import Persona, Pelicula, Dirige, Actua

def carga_personas():
    # We now read from the 'clean' folder using the database's encoding
    with open('data/people.csv', 'r', encoding='cp1252') as f:
        for row in DictReader(f):
            db.session.add(Persona(
                id=row["person_id"],
                nombre=row["name"],
                popularidad=float(row["popularity"]),
                biografia=row["biography"],
                cumple=row["birthday"] or None,
                url_imagen=row["profile_url"]
            ))

def carga_pelis():
    with open('data/movies.csv', 'r', encoding='cp1252') as f:
        for row in DictReader(f):
            db.session.add(Pelicula(
                id=row["movie_id"],
                titulo=row["title"],
                resumen=row["overview"],
                presupuesto=float(row["budget"]),
                ganancias=float(row["revenue"]),
                popularidad=float(row["popularity"]),
                url_imagen=row["poster_url"]
            ))

def carga_actua():
    with open('data/acts.csv', 'r', encoding='cp1252') as f:
        for row in DictReader(f):
            db.session.add(Actua(
                id_pelicula=row["movie_id"],
                id_persona=row["person_id"],
                papel=row["character"]
            ))

def carga_dirige():
    with open('data/directs.csv', 'r', encoding='cp1252') as f:
        for row in DictReader(f):
            db.session.add(Dirige(
                id_pelicula=row["movie_id"],
                id_persona=row["person_id"]
            ))

def popular_datos():
    carga_personas()
    carga_pelis()
    db.session.commit()
    carga_actua()
    carga_dirige()
    db.session.commit()

def borrar_crear_base_de_datos():
    db.drop_all()
    db.create_all()

if __name__ == "__main__":
    app = create_app()
    with app.app_context():
        borrar_crear_base_de_datos()
        popular_datos()