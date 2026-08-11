"""
Modulo para hacer cuestiones de trivia relacionadas con videos de Youtube. Creamos una clase TriviaVideo que extiende
a Trivia y almacena el id de reproduccion de video
"""

from abc import ABC, abstractmethod
from typing import List
from pathlib import Path
import random
from.operaciones_coleccion import OperacionesEurovision
from .preguntas import Trivia


def extraer_id_url(url) -> str:
    """
    Para renderizar el juego, necesitamos extraer el id desde la url del video.
    Utilizamos expresiones regulares
    """
    try:
        return Path(url).name
    except:
        # Return id for Rick Roll
        return "dQw4w9WgXcQ"


class TriviaVideo(Trivia, ABC):
    """
    Clase abstracta que contiene los metodos que deben incorporar las preguntas asociadas a videos.
    """

    @property
    @abstractmethod
    def url(self) -> str:
        pass

    def to_dict(self):
        # Modifica el diccionario de Trivia con la url del video
        # y el tipo "video"
        super_dict = super().to_dict()
        super_dict["url"] = self.url
        # Extraemos el id de la URL
        super_dict["url_id"] = extraer_id_url(self.url)
        super_dict["tipo"] = "video"
        return super_dict


class PaisActuacion(TriviaVideo):
    """
    ¿Que pais represento la cancion?
    """

    def __init__(self, parametros: OperacionesEurovision):
        pipeline = [
            {"$unwind": "$concursantes"},
            {"$match": {"concursantes.url_youtube": {"$exists": True, "$ne": ""}}},
            {"$sample": {"size": 1}}
        ]

        resultado = list(parametros.agregacion(pipeline))[0]
        participacion = resultado["concursantes"]

        self._url = participacion["url_youtube"]
        self._respuesta = participacion["pais"]

        self._falsas = parametros.paises_participantes_aleatorios(3, condiciones_extras=[
            {"$match": {"concursantes.pais": {"$ne": self._respuesta}}}
        ])

    @property
    def url(self): return self._url

    @property
    def pregunta(self): return "¿A qué país representó esta canción?"

    @property
    def opciones_invalidas(self): return self._falsas

    @property
    def respuesta(self): return self._respuesta

    @property
    def puntuacion(self): return 3


class NombreCancion(TriviaVideo):
    """
    ¿Cual es el titulo de esta cancion?

    NOTA: para dificultar la respuesta, se deben seleccionar canciones del mismo pais.
    """

    def __init__(self, parametros: OperacionesEurovision):
        res = list(parametros.agregacion([
            {"$unwind": "$concursantes"},
            {"$match": {"concursantes.url_youtube": {"$exists": True, "$ne": ""}}},
            {"$sample": {"size": 1}}
        ]))[0]

        datos = res["concursantes"]
        self._url = datos["url_youtube"]
        self._respuesta = datos["cancion"]

        falsas_res = list(parametros.agregacion([
            {"$unwind": "$concursantes"},
            {"$match": {
                "concursantes.pais": datos["pais"],
                "concursantes.cancion": {"$ne": self._respuesta}
            }},
            {"$sample": {"size": 3}}
        ]))

        self._falsas = [f["concursantes"]["cancion"] for f in falsas_res]

    @property
    def url(self): return self._url

    @property
    def pregunta(self): return "¿Cuál es el título de esta canción?"

    @property
    def opciones_invalidas(self): return self._falsas

    @property
    def respuesta(self): return self._respuesta

    @property
    def puntuacion(self): return 2


class InterpreteCancion(TriviaVideo):
    """
    ¿Quien interpreto esta cancion?

    NOTA: para dificultar la respuesta, se deben seleccionar interpretes del mismo pais.
    """

    def __init__(self, parametros: OperacionesEurovision):
        filtro = {"concursantes.url_youtube": {"$exists": True, "$ne": ""}}
        if parametros.anyos: filtro["anyo"] = {"$in": parametros.anyos}
        if parametros.paises: filtro["concursantes.pais"] = {"$in": parametros.paises}

        res = list(parametros.agregacion([
            {"$unwind": "$concursantes"},
            {"$match": filtro},
            {"$sample": {"size": 1}}
        ]))

        if not res:
            res = list(parametros.agregacion([
                {"$unwind": "$concursantes"},
                {"$match": {"concursantes.url_youtube": {"$exists": True, "$ne": ""}}},
                {"$sample": {"size": 1}}
            ]))

        participacion = res[0]["concursantes"]
        self._url = participacion["url_youtube"]
        self._respuesta = participacion["artista"]

        falsos_res = list(parametros.agregacion([
            {"$unwind": "$concursantes"},
            {"$match": {
                "concursantes.pais": participacion["pais"],
                "concursantes.artista": {"$ne": self._respuesta}
            }},
            {"$sample": {"size": 3}}
        ]))

        self._falsas = [f["concursantes"]["artista"] for f in falsos_res]

        if len(self._falsas) < 3:
            extras = parametros.participacion_aleatoria(3 - len(self._falsas))

            self._falsas.extend([e["artista"] for e in extras])

    @property
    def url(self):
        return self._url

    @property
    def pregunta(self):
        return "¿Qué artista interpretó esta canción?"

    @property
    def opciones_invalidas(self):
        return self._falsas

    @property
    def respuesta(self):
        return self._respuesta

    @property
    def puntuacion(self):
        return 4