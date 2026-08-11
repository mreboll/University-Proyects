from app import db, create_app
from app.modelos import Persona, Usuario, Contrato
from sqlalchemy import update, delete

def reiniciar():
    flask_app = create_app()
    with flask_app.app_context():
        db.session.execute(delete(Contrato))
        db.session.execute(update(Persona).values(en_mercado=False)) #no me queda claro si tambien se reinicia el mercado
        db.session.execute(update(Usuario).values(presupuesto=100.0))
        db.session.commit()

if __name__ == "__main__":
    reiniciar()