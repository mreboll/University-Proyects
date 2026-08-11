"""
Modulo que contiene diferentes modelos de consulta para la seccion de "trivia".
"""
import random
from typing import List
from abc import ABC, abstractmethod
from .operaciones_coleccion import OperacionesEurovision


# Clases para encapsular las preguntas y respuestas generadas aleatoriamente
class Trivia(ABC):
    """
    Clase abstracta con los metodos que deben implementar todas las preguntas de trivia.
    """

    @abstractmethod
    def __init__(self, parametros: OperacionesEurovision):
        # Obligamos a que todos los constructores les pasen un objeto con los parametros aleatorios
        pass

    @property
    @abstractmethod
    def pregunta(self) -> str:
        """
        Pregunta que se debe mostrar
        """
        pass

    @property
    @abstractmethod
    def opciones_invalidas(self) -> List[str]:
        """
        Lista de opciones invalidas. Deben ser exactamente 3
        """
        pass

    @property
    @abstractmethod
    def respuesta(self) -> str:
        """
        Respuesta correcta
        """
        pass

    @property
    @abstractmethod
    def puntuacion(self) -> int:
        """
        Puntuacion asociada a la pregunta
        """
        pass

    def to_dict(self):
        # Sorteamos aleatoriamente las respuestas
        respuestas = [self.respuesta, *self.opciones_invalidas]
        random.shuffle(respuestas)

        # Funcion que genera la informacion que pasamos al script de trivia en el formato adecuado
        return {"pregunta": self.pregunta,
                "correcta": respuestas.index(self.respuesta),
                "respuestas": respuestas,
                "puntuacion": self.puntuacion,
                "tipo": "pregunta"
        }


class PrimerAnyoParticipacion(Trivia):
    """
    Pregunta que anyo fue el primero en el que participo un pais seleccionado aleatoriamente
    """

    def __init__(self, parametros: OperacionesEurovision):
        paises = parametros.paises_participantes_aleatorios(1)
        self.pais = paises[0]

        pipeline = [
            {"$unwind": "$concursantes"},
            {"$match": {"concursantes.pais": self.pais}},
            {"$group": {"_id": None, "debut": {"$min": "$anyo"}}}
        ]

        res = list(parametros.agregacion(pipeline))
        self._respuesta = str(res[0]["debut"])

        falsos = parametros.anyo_aleatorio(3, condiciones_extras=[
            {"$match": {"anyo": {"$ne": int(self._respuesta)}}}
        ])
        self._falsas = [str(a) for a in falsos]

    @property
    def pregunta(self):
        return f"¿En qué año participó por primera vez {self.pais}?"

    @property
    def opciones_invalidas(self):
        return self._falsas

    @property
    def respuesta(self):
        return self._respuesta

    @property
    def puntuacion(self):
        """
        Puntuacion asociada a la pregunta
        """
        return 2


class CancionPais(Trivia):
    """
    Pregunta de que pais es el interprete de una cancion, dada el titulo de la cancion
    """

    def __init__(self, parametros: OperacionesEurovision):
        # Obtenemos una participacion para la respuesta

        res = parametros.participacion_aleatoria(1)

        if not res:
            res = list(parametros.agregacion([
                {"$unwind": "$concursantes"},
                {"$match": {"concursantes.cancion": {"$exists": True}}},
                {"$sample": {"size": 1}}
            ]))

        doc = res[0]
        part = doc.get('concursantes', doc)

        self._cancion = part.get('cancion') or part.get('titulo')
        self._respuesta = part.get('pais')

        self._falsas = parametros.paises_participantes_aleatorios(3, condiciones_extras=[
            {"$match": {"concursantes.pais": {"$ne": self._respuesta}}}
        ])

        while len(self._falsas) < 3:
            self._falsas.append("Otros")

    @property
    def pregunta(self):
        return f"¿De qué país es el intérprete de la canción '{self._cancion}'?"

    @property
    def opciones_invalidas(self):
        return self._falsas

    @property
    def respuesta(self):
        return self._respuesta

    @property
    def puntuacion(self):
        """
        Puntuacion asociada a la pregunta
        """
        return 1

class MejorClasificacion(Trivia):
    """
    Pregunta: ¿Que cancion/pais obtuvo la mejor posicion en un anyo dado?

    Respuesta: las respuestas deben ser de la forma cancion/pais.

    IMPORTANTE: la solucion debe ser unica. Ademas, todos las opciones
    deben haber participado el mismo anyo.
    """

    def __init__(self, parametros: OperacionesEurovision):
        self._anyo = parametros.anyo_aleatorio(1)[0]

        pipeline = [
            {"$match": {"anyo": self._anyo}},
            {"$unwind": "$concursantes"},
            {"$sort": {"concursantes.resultado": 1}}
        ]

        todos = list(parametros.agregacion(pipeline))
        ganador = todos[0]
        self._respuesta = f"{ganador['concursantes']['cancion']}/{ganador['concursantes']['pais']}"

        falsas_posibles = []
        for c in todos[1:]:
            txt = f"{c['concursantes']['cancion']}/{c['concursantes']['pais']}"
            if c['concursantes']['resultado'] != ganador['concursantes']['resultado']:
                falsas_posibles.append(txt)

        self._falsas = random.sample(falsas_posibles, 3)

    @property
    def pregunta(self):
        return f"¿Qué canción/país obtuvo la mejor posición en {self._anyo}?"

    @property
    def opciones_invalidas(self):
        return self._falsas

    @property
    def respuesta(self):
        return self._respuesta

    @property
    def puntuacion(self):
        return 3

class MejorMediaPuntos(Trivia):
    """
    Pregunta que pais ha tenido mejor media de resultados en un periodo determinado.

    IMPORTANTE: la solucion debe ser unica.
    """

    def __init__(self, parametros: OperacionesEurovision):
        # Elegimos un rango de 10 años
        todos_anyos = sorted(parametros.anyos if parametros.anyos else range(1960, 2024))
        inicio_idx = random.randint(0, max(0, len(todos_anyos) - 11))

        self.ini = todos_anyos[inicio_idx]
        self.fin = todos_anyos[min(inicio_idx + 10, len(todos_anyos) - 1)]

        pipeline = [
            {"$match": {"anyo": {"$gte": self.ini, "$lte": self.fin}}},
            {"$unwind": "$concursantes"},
            {"$group": {
                "_id": "$concursantes.pais",
                "media": {"$avg": "$concursantes.resultado"}
            }},
            {"$sort": {"media": 1}}
        ]

        datos = list(parametros.agregacion(pipeline))
        self._respuesta = datos[0]["_id"]

        falsas_lista = [d["_id"] for d in datos[1:] if d["media"] != datos[0]["media"]]
        self._falsas = random.sample(falsas_lista, 3)

    @property
    def pregunta(self):
        return f"¿Qué país tuvo mejor media entre {self.ini} y {self.fin}?"

    @property
    def opciones_invalidas(self):
        return self._falsas

    @property
    def respuesta(self):
        return self._respuesta

    @property
    def puntuacion(self):
        return 4