import math

import keras
import numpy as np
import tensorflow as tf
from keras.layers import Dense
from keras.models import Sequential

from Hippocampus_Language_Framework.math_functions import normalize_rows
from Hippocampus_Language_Framework.models.BaseTextModel import BaseTextModel
from Hippocampus_Language_Framework.paths import prediction_matrices_dir
from Hippocampus_Language_Framework.rules import Rule

"""
Eingabe: Wortvektoren
Ausgabe: 1-hot-encoded Vektoren

Für die Visualisierung muss bissl rumgerechnet werden, da man eine
quadratische Matrix benötigt, die alle Zustände beinhaltet, um die
SR zu berechnen.

Verfahren ähnlich zu Word2Vec_Model_2.
"""


class W2V_OHE(BaseTextModel):  # W2V_OHE = Word2Vec to 1-hot-encoded
    def __init__(self,
                 model_name="W2V_OHE",
                 epochs=20,
                 batch_size=100,
                 nmb_hidden_layers=1,
                 period=0,
                 pages=10,
                 resume_training="",
                 at_epoch=0,
                 nmb_tokens=0,
                 book_name="Daniel_Glattauer_Gut_gegen_Nordwind.pdf"):
        super().__init__(model_name=model_name,
                         epochs=epochs,
                         batch_size=batch_size,
                         nmb_hidden_layers=nmb_hidden_layers,
                         period=period,
                         pages=pages,
                         resume_training=resume_training,
                         at_epoch=at_epoch,
                         nmb_tokens=nmb_tokens,
                         book_name=book_name)
        self.length_wordvector = math.inf
        self.rules = self.build_rules()
        self.axes_labels_y = []
        self.lemma_to_idx = {}
        self.idx_to_lemma = {}
        self.ground_truth_type = "words"
        self.ground_truth_file_name = f"{self.book_name[0]}_{self.pages}pages_{self.nmb_tokens}T_{self.ground_truth_type}"
        self.configure()
        self.prepare_data()

# TODO <rules> evtl. mit dem von Word2Vec_Model2 anpassen. Dieses wird dort in <prepare_data>
#   durch eine Tupel-Version ersetzt, um weniger Speicher zu verbrauchen

    def build_rules(self):
        words = set()
        rules = []
        token_pairs = super().extract_tokenpairs(self.lt)
        for token, token_nbor in token_pairs:
            self.length_wordvector = len(token.vector) if len(token.vector) < self.length_wordvector else self.length_wordvector
            words.add(token.lemma_)
            words.add(token_nbor.lemma_)
            rule = Rule(token, token_nbor)
            rules.append(rule)
        return rules

    def prepare_data(self):
        """
        <training_data_{x,y}> are basically the rule. This method will just transform <self.rules>.
        """
        shape = (len(self.rules), self.length_wordvector)
        shape_y = (len(self.rules), len(self.cognitive_room))
        training_data_x = np.empty(shape)
        training_data_y = np.empty(shape_y)

        for i, rule in enumerate(self.rules):
            training_data_x[i] = rule.first.vector[:self.length_wordvector]

            self.idx_to_lemma[i] = rule.first.lemma_
            if rule.first.lemma_ not in self.lemma_to_idx:
                self.lemma_to_idx[rule.first.lemma_] = [i]
            else:
                self.lemma_to_idx[rule.first.lemma_].append(i)

            hot_enc_output = np.zeros((len(self.cognitive_room), ))
            idx = self.cognitive_room.index(rule.second.lemma_)
            hot_enc_output[idx] = 1
            training_data_y[i] = hot_enc_output
            # Different labeling for word2vec-Model, only y-axis. Currently, I dont know
            # how to handle the x-axis
            self.axes_labels_y.append(f"{rule.first.text} {rule.first.pos_}")
        self.training_data_x = training_data_x
        self.training_data_y = training_data_y

    def configure(self):
        super().configure()
        self.input_size = self.length_wordvector

    def do_prediction(self, **kwargs):
        # If saved instance is available, load it before calculating it again
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
            # Goal: constructing the transition probability matrix
            # Match every row in <predicted_outputs> with its row in the target matrix.
            #   f.i. two rules: Peter -> goes, Peter -> buys (in TOKEN form! No Words are saved directly)
            #   first rule corresponds to row i in <self.training_data_x>
            #   second rule corresponds to row j in <self.training_data_y>
            #       => Predictions in row i & j of <predicted_outputs>
            # For the transition probability matrix, which is squared in the dimensions of
            # <self.cognitive_room> (== the number of states), because this decodes
            # all states, has only one entry for "Peter", we have to match both predictions
            # with this one entry/state:
            #   Row i in <predicted_outputs> is associated with rules[i]
            #   From rules[i] we get the word, i.e. lemma by <rules[i].first.lemma_>
            #   With the lemma we retrieve the <idx> in <self.cognitive_room>
            #   We add <predicted_outputs[idx]> to <transition_probability_matrix[i]>
            for i, row in enumerate(predicted_outputs):
                # row i is the prediction of vector i which resembles a token with
                # a lemma which has an index in <self.cognitive_room>
                rule = self.rules[i]                          # row i is based on rules[i]
                lemma = rule.first.lemma_                             # rules[i] is associated to a lemma
                idx_in_cr = self.cognitive_room.index(lemma)  # this lemma has an index
                transition_probability_matrix[idx_in_cr] += row         # add row of prediction to corresponding row in <transition_probability_matrix>
            # Normalize <transition_probability_matrix> row-wise
            normalize_rows(transition_probability_matrix)
            # nmb_most_frequent_words = 30
            # tpm_trimmed = np.empty((nmb_most_frequent_words, nmb_most_frequent_words))
            # self.most_frequent_words = []
            # for row_idx, mfw in enumerate(self.most_frequent_words):
            #     idx_in_cr = self.cognitive_room.index(mfw)
            #     row = transition_probability_matrix[idx_in_cr]
            #     indices = np.argpartition(row, -nmb_most_frequent_words)[-nmb_most_frequent_words:]
            #     tpm_trimmed[row_idx] = transition_probability_matrix[idx_in_cr]
            matrix_file_name = f"{prediction_matrices_dir}/{self.full_name_no_time}/{checkpoint_info}/{self.full_name_no_time}.npy"
            np.save(matrix_file_name, transition_probability_matrix)
            return transition_probability_matrix

    def build_model(self) -> keras.engine.sequential.Sequential:
        """
        Dense model with
            input dim = length of wordvector
            output dim = length of cognitive_room (nmb of all states/words/lemmata)
        """
        # in total <self.nmb_hidden_layers + 2> layers: input + self.nmb_hidden_layers + output
        nmb_layers = 1 + self.nmb_hidden_layers  # "+ 1" due to the input layer
        model = Sequential()
        diff = len(self.cognitive_room) - self.input_size
        assert diff > 0, f"<self.cognitive_room> zu klein, diff={diff} (negativ), muss aber > 0 sein. " \
                         f"Um Fehler zu beheben <self.cognitive_room> vergrößern, indem mehr Seiten verwendet werden."
        stepsize = diff // nmb_layers
        # remainder = diff % nmb_layers
        # input layer and hidden layers
        for i in range(1, nmb_layers + 1):
            model.add(Dense(self.input_size + i * stepsize, activation=tf.nn.relu))
        # output layer
        model.add(Dense(self.input_size + diff, activation="softmax"))
        return model

"""
3 Layers (INSGESAMT)
14 / 3 == 4 R 2
0 4
4 8
8 12
12 14

3 Hidden Layers:
14 / 4 = 3 R 2
0 3
3 6
6 9
9 12
12 14
"""
