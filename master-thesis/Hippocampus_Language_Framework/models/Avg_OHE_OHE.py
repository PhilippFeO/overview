import pickle

import numpy as np

from Hippocampus_Language_Framework.models.OHE_OHE import OHE_OHE
from Hippocampus_Language_Framework.paths import prediction_matrices_dir


class Avg_OHE_OHE(OHE_OHE):
    """
    Mit diesem Modell sollen die Vorhersagen gemittelt werden. Das bedeutet: Man pickt sich alle
    Vorhersagen einer Wortart, bspw. der Verben, heraus, gibt sie für Vorhersagen dem Netzwerk,
    berechnet dann aus allen Vorhersagen einen Durchschnittsvektor, schaut sich dann von diesem
    die Positionen (Worten) mit den größten Werten an und bestimmt von diesen wiederum die
    Wortart. Vielleicht ergibt sich so ein Muster
    """
    def __init__(self,
                 model_name="Avg_OHE_OHE",
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
        self.axes_labels = self.ud_pos_tags_REST
        self.ground_truth_type = "tags"
        self.ground_truth_file_name = f"{self.book_name[0]}_{self.pages}pages_{self.nmb_tokens}T_{self.ground_truth_type}"

    def do_prediction(self, **kwargs):
        checkpoint_info = kwargs["checkpoint_info"]
        self.do_prediction_helper(checkpoint_info)

        matrix_file_name = f"{prediction_matrices_dir}/{self.full_name_no_time}/{checkpoint_info}/{self.full_name_no_time}"

        # If saved instance is available, load it before calculating it again
        self.counted_wordclasses = self.load_prediction_matrix("pkl", checkpoint_info)
        if self.counted_wordclasses is not None:
            pass  # Empty <if> to keep coherence with the other models, which als inherit from <BaseTextModel>
        else:
            model = kwargs["model"]
            predictions = []
            for ud_pos_tag in self.ud_pos_tags_REST:
                nmb_words = len(self.__getattribute__(ud_pos_tag))
                # prediction for all <ud_pos_tag>-words piecewise
                dia_predict_vectors = np.zeros((nmb_words, len(self.cognitive_room)))
                for idx_word, word in enumerate(self.__getattribute__(ud_pos_tag)):
                    idx_cr = self.cognitive_room.index(word)
                    dia_predict_vectors[idx_word][idx_cr] = 1
                predictions.append(model.predict(dia_predict_vectors))
            """ Calculate bar plot and <transition_probability_matrix> """
            self.counted_wordclasses = {}
            # Iterate over all predictions which are ordered by word class
            # or, better, by their appearance in <ud_pos_tags_REST>
            for ud_pos_tag, prediction in zip(self.ud_pos_tags_REST, predictions):
                # Sum all predictions to examine most frequent words
                # Word which appear regularly achive a higher score
                col_average = np.sum(prediction, axis=0) / len(prediction)
                self.average(col_average, ud_pos_tag)
            # save <self.counted_wordclasses>
            with open(f"{matrix_file_name}.pkl", "wb") as pickle_file:
                pickle.dump(self.counted_wordclasses, pickle_file)

        # print information
        for ud_pos_tag in self.counted_wordclasses:
            print(f"{ud_pos_tag} -> {self.counted_wordclasses[ud_pos_tag]}")

        # Calculate transition probability matrix
        transition_probability_matrix = self.calc_tpm_from_CW(self.counted_wordclasses)
        np.save(f"{matrix_file_name}.npy", transition_probability_matrix)
        return transition_probability_matrix
