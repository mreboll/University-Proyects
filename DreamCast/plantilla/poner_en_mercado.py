from typing import List
import random
from app import db, create_app
from app.modelos import Persona, Contrato
from sqlalchemy import select

def poner_personas_en_mercado(ids_personas_disponibles: List[int], n_personas: int) -> None:
    ids_seleccionados = random.sample(ids_personas_disponibles, n_personas)
    for id_persona_aleatoria in ids_seleccionados:
        persona = db.session.get(Persona, id_persona_aleatoria)
        if persona:
            persona.en_mercado = True
    db.session.commit()

def ejecutar_script():
    flask_app = create_app()

    with flask_app.app_context():
        n = 30 #con este numero se decide el numero de actores que se ponen en el mercado.
        subquery_contratados = select(Contrato.id_persona)
        consulta = select(Persona.id).where(
            Persona.en_mercado == False,
            Persona.id.not_in(subquery_contratados)
        )
        ids_disponibles = db.session.scalars(consulta).all()
        n_final = min(n, len(ids_disponibles))
        if n_final > 0:
            poner_personas_en_mercado(ids_disponibles, n_final)

if __name__ == "__main__":
    ejecutar_script()