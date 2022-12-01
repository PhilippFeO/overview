from random import choice

import numpy as np

from Hippocampus_Language_Framework.models.BaseTextModel import BaseTextModel

"""
Ich glaube, diese Klasse habe ich erstellt, weil ich an mehreren Stellen die <prepare_data>-Methode
benötige, sie aber nicht so gut in die Vererbungshierarchie gepasst hat, da sie eben wieder nicht bei allen
Klassen, die von <BaseTextModel> erben, benötigt wird.
"""


class BaseTextModelHelper(BaseTextModel):
    def __init__(self,
                 model_name,
                 epochs,
                 batch_size,
                 nmb_hidden_layers,
                 nmb_concatenations=1):
        super().__init__(
            model_name=model_name,
            epochs=epochs,
            batch_size=batch_size,
            nmb_hidden_layers=nmb_hidden_layers,
            nmb_concatenations=nmb_concatenations)
        self.nmb_samples = 10

    def prepare_data(self):
        """
        1-hot-encoded-Version
        """
        # Eine Regel wurde einfach aus zwei aufeinander folgenden Wörtern gebildet.
        # Deswegen gibt es keine Unterscheidung in Wortarten mehr
        training_data_x = np.zeros((self.nmb_samples, len(self.cognitive_room)))
        training_data_y = np.zeros((self.nmb_samples, len(self.cognitive_room)))

        for i in range(self.nmb_samples):
            rule = choice(self.rules)  # Wähle Regel zufällig

            hot_vector = np.zeros((len(self.cognitive_room)))  # Hier gesamt, also alle Wörter aller Wortarten
            index = self.cognitive_room.index(rule[0])  # Index des ersten Wortes der Regel
            hot_vector[index] = 1
            training_data_x[i] = hot_vector

            # Analog zu oben
            hot_vector = np.zeros((len(self.cognitive_room)))
            index = self.cognitive_room.index(rule[1])
            hot_vector[index] = 1
            training_data_y[i] = hot_vector
        # Trainingsdaten <self.nmb_concatenations> mal wiederholen
        self.training_data_x = np.vstack([training_data_x] * self.nmb_concatenations)
        self.training_data_y = np.vstack([training_data_y] * self.nmb_concatenations)

    def do_prediction(self, **kwargs):
        model = kwargs["model"]
        input_size = kwargs["input_size"]  # TODO kann wasl. mit <self.input_size> ersetzt werden
        # Make preditctions for all states with the model
        dia_predict_vectors = np.zeros((input_size, input_size))
        np.fill_diagonal(dia_predict_vectors, 1)
        return model.predict(dia_predict_vectors)
