from random import choice

import numpy as np

from Hippocampus_Language_Framework.models.BaseTextModel import BaseTextModel
from Hippocampus_Language_Framework.paths import prediction_matrices_dir
from Hippocampus_Language_Framework.rules import Rule


class OHE_OHE(BaseTextModel):
    """
    Liest einen Text mit spacy aus, berechnet 1-Hot-Encoded Vektoren und nutzt diese
    für's Training.
    """
    def __init__(self,
                 model_name="OHE_OHE",
                 epochs=20,
                 batch_size=100,
                 nmb_hidden_layers=1,
                 nmb_concatenations=1,
                 period=0,
                 pages=5,
                 resume_training="",
                 at_epoch=0,
                 nmb_tokens=0,
                 book_name=0):
        super().__init__(
            model_name=model_name,
            epochs=epochs,
            batch_size=batch_size,
            nmb_hidden_layers=nmb_hidden_layers,
            nmb_concatenations=nmb_concatenations,
            period=period,
            pages=pages,
            resume_training=resume_training,
            at_epoch=at_epoch,
            nmb_tokens=nmb_tokens,
            book_name=book_name)
        self.rules = self.build_rules()
        self.ground_truth_type = "words"
        self.ground_truth_file_name = f"{self.book_name[0]}_{self.pages}pages_{self.nmb_tokens}T_{self.ground_truth_type}"
        self.configure()
        self.prepare_data()

    def build_rules(self):
        rules = []
        token_pairs = self.extract_tokenpairs(self.lt)
        for token, token_nbor in token_pairs:
            # Now <token> and <token_nbor> are both not in <skip_pos> hence in admissible_UD_pos_tags
            # and can be used to craft a <Rule>
            rule = Rule(token.lemma_, token_nbor.lemma_)
            if rule not in rules:  # Irgendwie klappt es mit "set" nicht
                rules.append(rule)
        return rules

    def configure(self):
        super().configure()
        self.input_size = len(self.cognitive_room)

    def prepare_data(self):
        """
        1-hot-encoded-Version
        """
        # Eine Regel wurde einfach aus zwei aufeinander folgenden Wörtern gebildet.
        # Deswegen gibt es keine Unterscheidung in Wortarten mehr
        rows_x, rows_y = [], []
        for rule in self.rules:
            # Build input vector
            hot_vector = np.zeros((len(self.cognitive_room)))  # Hier gesamt, also alle Wörter aller Wortarten
            index = self.cognitive_room.index(rule[0])  # Index des ersten Wortes der Regel im <self.cognitive_room>
            hot_vector[index] = 1  # 1 setzen für 1 got encoded
            rows_x.append(hot_vector)

            # Build output vector; similar to input vector
            hot_vector = np.zeros((len(self.cognitive_room)))
            index = self.cognitive_room.index(rule[1])
            hot_vector[index] = 1
            rows_y.append(hot_vector)
        # Trainingsdaten <self.nmb_concatenations> mal wiederholen
        self.training_data_x = np.vstack(rows_x * self.nmb_concatenations)
        self.training_data_y = np.vstack(rows_y * self.nmb_concatenations)

    def do_prediction(self, **kwargs):
        checkpoint_info = kwargs["checkpoint_info"]
        self.do_prediction_helper(checkpoint_info)

        # If saved instance is available, load it before calculating it again
        transition_probability_matrix = self.load_prediction_matrix("npy", checkpoint_info)
        if transition_probability_matrix is not None:
            return transition_probability_matrix
        else:  # Make predictions for all states with the model
            model = kwargs["model"]
            input_size = kwargs["input_size"]  # TODO kann wasl. mit <self.input_size> ersetzt werden
            dia_predict_vectors = np.zeros((input_size, input_size))
            np.fill_diagonal(dia_predict_vectors, 1)
            transition_probability_matrix = model.predict(dia_predict_vectors)
            matrix_file_name = f"{prediction_matrices_dir}/{self.full_name_no_time}/{checkpoint_info}/{self.full_name_no_time}.npy"
            np.save(matrix_file_name, transition_probability_matrix)
            return transition_probability_matrix
