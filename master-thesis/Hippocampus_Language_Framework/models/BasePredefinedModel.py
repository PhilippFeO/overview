import random

import numpy as np

from Hippocampus_Language_Framework.models.BaseModel import BaseModel


class BasePredefinedModel(BaseModel):
    def __init__(self,
                 model_name,
                 epochs,
                 batch_size,
                 nmb_hidden_layers,
                 nmb_samples,
                 nmb_words_per_class):
        super().__init__(model_name,
                         epochs,
                         batch_size,
                         nmb_hidden_layers)
        # Determines rows of <training_data_{x, y}>
        self.nmb_samples = nmb_samples
        self.nmb_words_per_class = nmb_words_per_class

    # is overwritten by ModelBasedOnConllu
    def extract_wordclasses(self, rule):
        first_wordclass = self.words[rule[0]]
        second_wordclass = self.words[rule[1]]
        return first_wordclass, second_wordclass

    def prepare_data(self):
        # "self.nmb_samples" × #Wörter-Matrix; Jede Zeile wird mit einem 1-Vektor versehen
        # Regel: Wortart 1 -> Wortart 2 (bspw. A->N), dann ist
        #   training_data_x     für Wortart 1 (A)
        #   training_data_y     für Wortart 2 (N)
        training_data_x = np.zeros((self.nmb_samples, len(self.cognitive_room)))
        training_data_y = np.zeros((self.nmb_samples, len(self.cognitive_room)))
        for i in range(self.nmb_samples):
            rule = random.choice(self.rules)  # Wähle Regel zufällig, Enum-Nummerierung beginnt bei 1
            wordclass_1, wordclass_2 = self.extract_wordclasses(rule)
            sample_idx = np.random.randint(0, len(wordclass_1))  # Wortauswahl
            hot_vector = np.zeros((len(self.cognitive_room)))  # Hier gesamt, also alle Wörter alles Wortarten
            index = self.cognitive_room.index(wordclass_1[sample_idx])
            hot_vector[index] = 1
            training_data_x[i] = hot_vector
            # sample gt
            sample_idx = np.random.randint(0, len(wordclass_2))
            hot_vector = np.zeros((len(self.cognitive_room)))
            index = self.cognitive_room.index(wordclass_2[sample_idx])
            hot_vector[index] = 1
            training_data_y[i] = hot_vector
        self.training_data_x = training_data_x
        self.training_data_y = training_data_y

    def configure(self):
        pass

    def do_prediction(self, **kwargs):
        model = kwargs["model"]
        input_size = kwargs["input_size"]  # TODO kann wasl. mit <self.input_size> ersetzt werden
        # Make preditctions for all states with the model
        dia_predict_vectors = np.zeros((self.input_size, self.input_size))
        np.fill_diagonal(dia_predict_vectors, 1)
        return model.predict(dia_predict_vectors)

    @staticmethod
    def retrieve_subset(wordclass, nmb_words):
        return random.sample(wordclass, nmb_words) if len(wordclass) >= nmb_words else wordclass
