import pickle

import numpy as np

from Hippocampus_Language_Framework.models.W2V_W2V import W2V_W2V
from Hippocampus_Language_Framework.paths import prediction_matrices_dir


class Avg_W2V_W2V(W2V_W2V):
    def __init__(self,
                 model_name="Avg_W2V_W2V",
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
        matrix_file_name = f"{prediction_matrices_dir}/{self.full_name_no_time}/{checkpoint_info}/{self.full_name_no_time}"
        # If saved instance is available, load it before calculating it again
        self.counted_wordclasses = self.load_prediction_matrix("pkl", checkpoint_info)
        if self.counted_wordclasses is not None:
            pass  # Empty <if> to keep coherence with the other models, which als inherit from <BaseTextModel>
        else:
            self.counted_wordclasses = {}
            # Matrix mit bereits zurückgerechneten Wörtern/Einträgen
            predictions = super().do_prediction(**kwargs)
            # counted_wordclasses = self.average(predictions, checkpoint_info, self.idx_in_tdx)
            for ud_pos_tag, indices in self.idx_in_tdx.items():
                indices = list(indices)
                # Sum all predictions to examine most frequent words
                # Word which appear regularly achive a higher score
                col_average = np.sum(predictions[indices], axis=0) / len(indices)
                self.average(col_average, ud_pos_tag)
            # save <counted_wordclasses>
            with open(f"{matrix_file_name}.pkl", "wb") as pickle_file:
                pickle.dump(self.counted_wordclasses, pickle_file)

        # print information
        for ud_pos_tag in self.counted_wordclasses:
            print(f"{ud_pos_tag} -> {self.counted_wordclasses[ud_pos_tag]}")

        # Calculate transition probability matrix
        transition_probability_matrix = self.calc_tpm_from_CW(self.counted_wordclasses)
        np.save(f"{matrix_file_name}.npy", transition_probability_matrix)
        return transition_probability_matrix
