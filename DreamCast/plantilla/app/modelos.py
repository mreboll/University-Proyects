from typing import List
import datetime
import math
from . import db
from werkzeug.security import generate_password_hash, check_password_hash
from sqlalchemy import String, Integer, Boolean, ForeignKey
from sqlalchemy.orm import Mapped, mapped_column, relationship
from flask_login import UserMixin


class Pelicula(db.Model):
    __tablename__ = 'pelicula'

    id: Mapped[int] = mapped_column(Integer, autoincrement=True, primary_key=True, unique=True)
    titulo: Mapped[str] = mapped_column(nullable=False)
    resumen: Mapped[str] = mapped_column(nullable=False)
    presupuesto: Mapped[float] = mapped_column(nullable=False)
    ganancias: Mapped[float] = mapped_column(nullable=False)
    popularidad: Mapped[float] = mapped_column(nullable=False)
    url_imagen: Mapped[str] = mapped_column(nullable=False)

    reparto: Mapped[List["Actua"]] = relationship(back_populates="pelicula")
    directores: Mapped[List["Dirige"]] = relationship(back_populates="pelicula")

class Persona(db.Model):
    __tablename__ = 'persona'

    id: Mapped[int] = mapped_column(Integer, autoincrement=True, primary_key=True, unique=True)
    nombre: Mapped[str] = mapped_column(nullable=False)
    cumple: Mapped[datetime.date] = mapped_column(nullable=True)
    biografia: Mapped[str] = mapped_column(nullable=False)
    popularidad: Mapped[float] = mapped_column(nullable=True)
    url_imagen: Mapped[str] = mapped_column(nullable=False)
    en_mercado: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)

    peliculas_actuadas: Mapped[List["Actua"]] = relationship(back_populates="persona")
    peliculas_dirigidas: Mapped[List["Dirige"]] = relationship(back_populates="persona")
    contratos: Mapped[List["Contrato"]] = relationship(back_populates="persona")

    @property
    def cache(self) -> int:
        return math.ceil(self.popularidad) if self.popularidad is not None else 1

    def computar_penalizacion(self, dias: int) -> float:
        return self.cache * min(0.05 * dias, 0.25)

class Usuario(db.Model, UserMixin):
    __tablename__ = 'usuario'

    id: Mapped[int] = mapped_column(Integer, autoincrement=True, primary_key=True)
    email: Mapped[str] = mapped_column(nullable=False, unique=True)
    presupuesto: Mapped[float] = mapped_column(nullable=False, default=100)
    password_hash: Mapped[str] = mapped_column(String(256), nullable=False)

    mis_fichajes: Mapped[List["Contrato"]] = relationship(back_populates="usuario")

    @property
    def password(self):
        raise AttributeError('No se puede leer el atributo password')

    @password.setter
    def password(self, password: str) -> None:
        self.password_hash = generate_password_hash(password)

    def check_password(self, password: str) -> bool:
        return check_password_hash(self.password_hash, password)

class Actua(db.Model):
    __tablename__ = 'actua'
    id_persona: Mapped[int] = mapped_column(ForeignKey("persona.id"), primary_key=True)
    id_pelicula: Mapped[int] = mapped_column(ForeignKey("pelicula.id"), primary_key=True)
    papel: Mapped[str] = mapped_column(nullable=False)

    persona: Mapped["Persona"] = relationship(back_populates="peliculas_actuadas")
    pelicula: Mapped["Pelicula"] = relationship(back_populates="reparto")

class Dirige(db.Model):
    __tablename__ = 'dirige'
    id_persona: Mapped[int] = mapped_column(ForeignKey("persona.id"), primary_key=True)
    id_pelicula: Mapped[int] = mapped_column(ForeignKey("pelicula.id"), primary_key=True)

    persona: Mapped["Persona"] = relationship(back_populates="peliculas_dirigidas")
    pelicula: Mapped["Pelicula"] = relationship(back_populates="directores")

class Contrato(db.Model):
    __tablename__ = 'contrato'
    id_persona: Mapped[int] = mapped_column(ForeignKey("persona.id"), primary_key=True)
    id_usuario: Mapped[int] = mapped_column(ForeignKey("usuario.id"), primary_key=True)
    fecha: Mapped[datetime.date] = mapped_column(nullable=False, default=datetime.date.today)

    persona: Mapped["Persona"] = relationship(back_populates="contratos")
    usuario: Mapped["Usuario"] = relationship(back_populates="mis_fichajes")