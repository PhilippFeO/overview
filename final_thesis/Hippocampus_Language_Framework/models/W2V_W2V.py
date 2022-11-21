import math
import subprocess
from collections import Counter
from glob import glob

import numpy as np
from matplotlib import pyplot as plt
from numpy import ndarray
from tqdm import tqdm

from Hippocampus_Language_Framework.models.BaseTextModel import BaseTextModel
from Hippocampus_Language_Framework.paths import plots_dir
from Hippocampus_Language_Framework.paths import prediction_matrices_dir
from Hippocampus_Language_Framework.rules import Rule

"""
Ziel dieses Modells ist es mit den spacy-Wortvektoren zu trainieren.
Diese werden in die jeweilige Datenstruktur einfach eingefügt.
==> Man erhält Wortvektor-ähnliche Ausgaben und kein 1-hot-Encoding

Diese Ausgaben werden per spacy wieder zurückgerechnet. Dieser Code
findet sich (27.3.) in visualize.py. Damit kann man eine quadratische Matrix
für die Berechnung der SR aufstellen, da man dann in Zeile i einfach
das Wort markiert, das die Zurückrechnung des Vorhersagevektors ergibt.
"""


class W2V_W2V(BaseTextModel):
    def __init__(self,
                 model_name="W2V_W2V",
                 epochs=20,
                 batch_size=100,
                 nmb_hidden_layers=1,
                 nmb_concatenations=1,
                 nmb_of_retrieved_words=10,
                 period=0,
                 pages=5,
                 resume_training="",
                 at_epoch=0,
                 nmb_tokens=0,
                 book_name=0):
        super().__init__(model_name=model_name,
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
        self.nmb_of_retrieved_words = nmb_of_retrieved_words
        self.min = math.inf
        # werden in <prepare_data> durch Tuple, das Lemmata enthält, ersetzt!
        self.rules = self.build_rules()
        self.modified_rules = []
        self.axes_labels_y = []
        self.last_activation = "linear"
        self.loss_fct = "mean_squared_error"
        self.ground_truth_type = "words"
        self.ground_truth_file_name = f"{self.book_name[0]}_{self.pages}pages_{self.nmb_tokens}T_{self.ground_truth_type}"
        self.idx_in_tdx = {}  # Only used by <W2V_W2V>
        self.configure()
        self.prepare_data()

    def build_rules(self):
        rules = []
        token_pairs = super().extract_tokenpairs(self.lt)
        for token, token_nbor in token_pairs:
            self.min = len(token.vector) if len(token.vector) < self.min else self.min
            rule = Rule(token, token_nbor)
            rules.append(rule)
        return rules

    def prepare_data(self):
        """
        <training_data_{x,y}> are basically the rule. This method will just transform <self.rules>.
        """
        # TODO an <prepare_data> von <RealText_1HotEncoded> anpassen
        shape = (len(self.rules), self.min)
        training_data_x = np.empty(shape)
        training_data_y = np.empty(shape)
        for i, rule in enumerate(self.rules):
            # Necessary for Average version
            idx_cr = self.cognitive_room.index(rule.first.lemma_)
            if rule.first.pos_ in self.idx_in_tdx:
                self.idx_in_tdx[rule.first.pos_].add(idx_cr)
            else:
                if rule.first.pos_ not in self.ud_pos_tags:  # pos_ == SCONJ, INTJ, CCONJ
                    if self.REST[0] in self.idx_in_tdx:
                        self.idx_in_tdx[self.REST[0]].add(idx_cr)
                    else:
                        self.idx_in_tdx[self.REST[0]] = {idx_cr}
                else:
                    self.idx_in_tdx[rule.first.pos_] = {idx_cr}
            training_data_x[i] = rule.first.vector[:self.min].copy()
            training_data_y[i] = rule.second.vector[:self.min].copy()
            self.modified_rules.append((rule.first.lemma_, rule.second.lemma_))
        del self.rules
        self.training_data_x = np.vstack([training_data_x] * self.nmb_concatenations)
        self.training_data_y = np.vstack([training_data_y] * self.nmb_concatenations)

    def configure(self):
        super().configure()
        self.input_size = self.min

    def _helper_fct(self, transition_probability_matrix: ndarray, predicted_outputs: ndarray) -> None:
        """
        This function calculates according to the predictions in <dia_dia_predict_vectors> the best fitting word
        by using the <Vectors.most_similar()>-function [1]. Afterwards it checks whether each word is part of
        the <coginitve_room>. If so, it sets the distance in the <transition_probability_matrix>. <row> is given
        by the index of <lemma> in <cognitive_room>, which can be deduced from <rules[i]> for <row[i]> in
        <dia_predict_vectors>.

        [1]: https://spacy.io/api/vectors#most_similar
        """
        # start_time = time.time()
        # https://spacy.io/api/vectors#most_similar
        most_similar = self.lt.nlp.vocab.vectors.most_similar(
            np.asarray(predicted_outputs),
            # batch_size=1000,  # Standardwert: 1024
            n=self.nmb_of_retrieved_words,
            sort=True)
        # end_time = time.time()
        # print(f"Berechnung für {self.nmb_of_retrieved_words} Werte: {end_time - start_time} s")
        # Hinter <most_similar[0]> findet sich ein 2D-Array mit <len(dia_predict_vectors)> Zeilen
        # und <self.nmb_of_retrieved_words> Spalten
        words = [[self.lt.nlp.vocab.strings[w] for w in word_row] for word_row in most_similar[0]]
        # Distances from the predicted word vector to the most similar word vectors spacy knows.
        # Was für <most_similar[0]> gilt, gilt auch für <distances[2]>
        distances = most_similar[2]
        for word_row_idx, word_row in enumerate(words):
            # Calculating the index of the first state
            # If concatenation (<self.nmb_concatenations > 1>) present, the index <word_row_idx> becomes
            # to large and exceeds the list <self.modified_rules>. Thus it has to be trimmed to the size of
            # initial training data which is achived by dividing by <self.nmb_concatenations> and then modulo.
            rule = self.modified_rules[word_row_idx % (len(self.training_data_x) // self.nmb_concatenations)]
            lemma = rule[0]
            try:
                idx_lemma_in_cr = self.cognitive_room.index(lemma)
            except ValueError:
                continue
            for word_idx, word in enumerate(word_row):
                try:
                    # If <word> is in <cognitive_room> (i.e. valid state), save it's index
                    idx_word_in_cr = self.cognitive_room.index(word)
                    # print(word)
                except ValueError:
                    continue
                transition_probability_matrix[idx_lemma_in_cr][idx_word_in_cr] \
                    = distances[word_row_idx][word_idx]  # If <word> doesn't appear in <cognitive_room> this line won't be executed

    def do_prediction(self, **kwargs):
        checkpoint_info = kwargs["checkpoint_info"]
        self.do_prediction_helper(checkpoint_info)

        # If saved instance is available, load it before calculating it again
        transition_probability_matrix = self.load_prediction_matrix("npy", checkpoint_info)
        if transition_probability_matrix is not None:
            return transition_probability_matrix
        else:
            model = kwargs["model"]
            predicted_outputs = model.predict(self.training_data_x)
            shape = (len(self.cognitive_room), len(self.cognitive_room))
            transition_probability_matrix = np.zeros(shape)
            step_size = 200
            # Iterate over all predictions piecewise to reduce memory consumption (?)
            for i in tqdm(range(0, len(predicted_outputs), step_size)):
                if i + step_size <= len(predicted_outputs):  # end of first dimension of <predicted_outputs> not surpassed
                    self._helper_fct(transition_probability_matrix,
                                     predicted_outputs[i:i+step_size])
                else:
                    x = len(predicted_outputs) - i  # end of first dimension of <predicted_outputs> surpassed
                    self._helper_fct(transition_probability_matrix,
                                     predicted_outputs[i:i+x])
            # Save calculated <transition_probability_matrix>
            matrix_file_name = f"{prediction_matrices_dir}/{self.full_name_no_time}/{checkpoint_info}/{self.full_name_no_time}.npy"
            np.save(matrix_file_name, transition_probability_matrix)
        return transition_probability_matrix

