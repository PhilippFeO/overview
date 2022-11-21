import subprocess
from collections import Counter
import os
import pickle
from glob import glob
from os.path import isdir
from os.path import isfile
from time import strftime

import numpy as np
from matplotlib import pyplot as plt

from Hippocampus_Language_Framework.math_functions import normalize_rows
from Hippocampus_Language_Framework.models.BaseModel import BaseModel
from Hippocampus_Language_Framework.paths import ground_truth_dir
from Hippocampus_Language_Framework.paths import plots_dir
from Hippocampus_Language_Framework.paths import prediction_matrices_dir
from Hippocampus_Language_Framework.process_data.PdfParser import PdfParser


class BaseTextModel(BaseModel):
    def __init__(self,
                 model_name,
                 epochs,
                 batch_size,
                 nmb_hidden_layers,
                 nmb_concatenations=1,
                 period=0,
                 pages=10,
                 resume_training="",
                 at_epoch=0,
                 nmb_tokens=0,
                 book_name=0):
        super().__init__(model_name=model_name,
                         epochs=epochs,
                         batch_size=batch_size,
                         nmb_hidden_layers=nmb_hidden_layers,
                         nmb_concatenations=nmb_concatenations)
        self.period: int = period  # How often a model is checkpointed during training; -1: no checkpoints; n: every n-th epoch checkpointed
        self.pages: int = pages
        self.resume_training = resume_training
        self.at_epoch = at_epoch
        self.nmb_tokens = nmb_tokens
        assert 0 <= book_name <= 1, f"<book_name> has to be 0 or 1, was {book_name}"
        if book_name == 0:
            self.book_name = "Daniel_Glattauer_Gut_gegen_Nordwind.pdf"
        elif book_name == 1:
            self.book_name = "Jostein Gaarder - Sophie's World_ A Novel about the History of Philosophy-Berkley (1996).pdf"
        self.full_name_no_time = self.full_name_no_time + f"_{self.pages}P_{self.nmb_tokens}T_{self.book_name[0]}"
        timestamp = strftime("%Y.%m.%d-%H:%M:%S")
        self.full_name = f"{self.full_name_no_time}_{timestamp}"
        self.lt = PdfParser(
            self.book_name,
            with_vectors=True,
            german=True if book_name == 0 else False,
            nmb_pages=pages)
        # define and declare attributes according to UD-pos-tags
        self.ud_pos_tags = ["ADJ", "ADV", "AUX", "NOUN", "PRON", "VERB",
                            "DET", "ADP", "PART"]
        for ud_pos_tag in self.ud_pos_tags:
            self.__setattr__(ud_pos_tag, [])
        # Sometimes, I want to iterate over all ud_pos_tags and REST
        self.ud_pos_tags_REST = self.ud_pos_tags + ["REST"]
        self.REST = []  # for remaining (usually "small") words
        self.skipped_ud_pos_tags = {"NUM", "PUNCT", "SYM", "X", "SPACE", "PROPN"}
        self._ground_truth = None
        self.ground_truth_dict = {}  # For calculating the ground_truth/statistical = []
        self.ground_truth_type = ""
        self.ground_truth_file_name = ""
        self.counted = {}
        self.counted_wordclasses = {}
        self.matrix_file_name = f"{prediction_matrices_dir}/{self.full_name_no_time}/{self.full_name_no_time}"

    @property
    def ground_truth(self):
        if self._ground_truth is not None:
            return self._ground_truth
        else:
            reference_structure, idx = \
                (self.cognitive_room, 0) if self.ground_truth_type == "words" \
                    else (self.ud_pos_tags_REST, 1)  # self.ground_truth_type == "tags"
            ground_truth_file = f"{ground_truth_dir}/{self.ground_truth_file_name}.npy"
            # If the npy-file doesn't exist, the corresponding <ground_truth> hasn't been calculated yet
            if not isfile(ground_truth_file):
                shape = (len(reference_structure), len(reference_structure))
                ground_truth = np.zeros(shape)
                for key_pair in self.ground_truth_dict:
                    try:
                        idx_key = reference_structure.index(key_pair[idx])
                    except ValueError:
                        idx_key = reference_structure.index("REST")
                    for inner_key in self.ground_truth_dict[key_pair][idx]:
                        try:
                            idx_inner_key = reference_structure.index(inner_key)
                        except ValueError:
                            idx_inner_key = reference_structure.index("REST")
                        ground_truth[idx_key][idx_inner_key] += self.ground_truth_dict[key_pair][idx][inner_key]
                normalize_rows(ground_truth)
                if not isfile(ground_truth_file):
                    np.save(ground_truth_file, ground_truth)
            self._ground_truth = np.load(ground_truth_file)
            return self._ground_truth

    @ground_truth.setter
    def ground_truth(self, ground_truth):
        self._ground_truth = ground_truth

    def extract_tokenpairs(self, lt):
        # Used universal dependencies, s. https://universaldependencies.org/u/pos/ for more information
        token_pairs = []
        ground_truth: dict[tuple[dict, dict]] = {}  # For calculating the ground_truth/statistical
        # evaluation. Successor/Neighbor words are counted in a dict
        ground_truth_npy = f"{ground_truth_dir}/{self.book_name[0]}_{self.pages}pages_{self.ground_truth_type}.npy"
        save_ground_truth = not isfile(ground_truth_npy)  # Perform check once and not in each iteration
        # Crafting rules by iterating over the <spacy doc>.
        i = 0
        for token in lt.doc:
            # Last token has no neighbor (.nbor()) token
            if token == lt.doc[-1]:
                break
            token_nbor = token.nbor()  # neighbor/successor token
            # Use only "real words" for crafting <Rule>: <token.pos_> and <token_nbor.pos_>
            # both mustn't be part of <skip_pos>
            # <token_nbor.pos_> is checked later because at full stops like "The buy goes."
            # "goes" has to be added to the list, since the rule before relies on this word
            # being part of <self.cognitive_room>.
            # It will be in <self.cognitive_room> if it was added to <self.{admissible_ud_pos_tag}>
            if token.pos_ in self.skipped_ud_pos_tags:
                continue
            # Build datasets for learning by inserting <token.lemma_> into the appropriate attribute
            # like <self.ADJ>, <self.VERB>, ...
            # Using lemmata (token.lemma_) to cover conjugation, Numerus, ...
            x = False
            if token.pos_ in self.ud_pos_tags:
                # Inserting <token.lemma_> into <self.<token.pos_>
                if token.lemma_ not in self.__getattribute__(token.pos_):
                    self.__getattribute__(token.pos_).append(token.lemma_)
                    x = True
            elif token.lemma_ not in self.REST:
                self.REST.append(token.lemma_)
                x = True
            if x:
                if 0 < self.nmb_tokens == i:
                    break
                i += 1

            # Now we can check <token_nbor> because <token> is processed, meaning the word was
            # collect.
            # In case we have
            #   ... beautiful scenery.
            # There would be these tokens: token_beautiful, token_scenery, token_.
            # When constructing the rule <beautiful -> scenery> then only token_beautiful/beautiful
            # was collected. If we check the following condition right at the beginning, token_scenery
            # would be missed and we weren't able to craft <self.cognitive_room>, <self.training_data_x> and
            # <self.training_data_y> correctly.
            if token_nbor.pos_ in self.skipped_ud_pos_tags:
                continue

            if save_ground_truth:
                key_pair = (token.lemma_, token.pos_)
                if key_pair in ground_truth:
                    tmp = ground_truth[key_pair]
                    if token_nbor.lemma_ in tmp[0]:
                        tmp[0][token_nbor.lemma_] += 1
                    else:
                        tmp[0][token_nbor.lemma_] = 1
                    if token_nbor.pos_ in tmp[1]:
                        tmp[1][token_nbor.pos_] += 1
                    else:
                        tmp[1][token_nbor.pos_] = 1
                else:
                    ground_truth[key_pair] = ({}, {})
                    ground_truth[key_pair][0][token_nbor.lemma_] = 1
                    ground_truth[key_pair][1][token_nbor.pos_] = 1

            token_pair = (token, token_nbor)
            if token_pair not in token_pairs:
                token_pairs.append(token_pair)

        # Save <ground_truth>
        self.ground_truth_dict = ground_truth if ground_truth != dict() else self.ground_truth_dict
        if 0 < self.nmb_tokens == i:
            return token_pairs[:-1]
        else:
            return token_pairs

    def build_rules(self):
        pass

    def configure(self):
        for ud_pos_tag in self.ud_pos_tags_REST:
            self.cognitive_room += self.__getattribute__(ud_pos_tag)
            self.words.append(self.__getattribute__(ud_pos_tag))
            # self.axes_labels += [f"{word} {ud_pos_tag}" for word in self.__getattribute__(ud_pos_tag)]  # "f[f"{word} {NOUN} for word in self.NOUN]"
            self.axes_labels += [f"{word}" for word in self.__getattribute__(ud_pos_tag)]  # "f[f"{word} {NOUN} for word in self.NOUN]"

    def do_prediction_helper(self, checkpoint_info=""):
        full_path = f"{prediction_matrices_dir}/{self.full_name_no_time}/{checkpoint_info}"
        if not isdir(full_path):
            os.makedirs(full_path)

    # TODO Diese Methode ist bissl chaotisch, vor allem wegen <self.matrix_file_name> aber sie funktioniert
    #  und zwar so, dass ich es nachvollziehen kann. Für eine saubere Version bin ich gerade zu faul.
    def load_prediction_matrix(self, file_extension, checkpoint_info=""):
        npy, pkl = "npy", "pkl"
        assert file_extension in {npy, pkl}, f"<file_extension> has to be in {{{npy}, {pkl}}}"
        # insert <checkpoint_info> into path
        file = f"{prediction_matrices_dir}/{self.full_name_no_time}/{checkpoint_info}/{self.full_name_no_time}.{file_extension}"
        if file_extension == npy:
            if isfile(file):
                print(f"Load prediction matrix:\n\t{file}")
                return np.load(file)
        elif file_extension == pkl:
            if isfile(file):
                print(f"Load pickle-file:\n\t{file}")
                with open(file, "rb") as pickle_file:
                    return pickle.load(pickle_file)
        return None

    def average(self, col_average, ud_pos_tag):
        # Berechnen der Indizes der <nmb_avg_values> Wörter mit den größten Durchschnittswerten
        nmb_avg_values = 10
        max_indices = np.argpartition(col_average, -nmb_avg_values)[-nmb_avg_values:]
        # Extracting the corresponding words, which are the most frequent ones
        most_freq_words = (self.cognitive_room[idx] for idx in max_indices)
        pred_wordclasses = []
        # Finding the appropriate word class of the most frequent words
        for word in most_freq_words:
            for wordclass, ud_pos_tag_wc in zip(self.words, self.ud_pos_tags_REST):
                if word in wordclass:  # word class found
                    pred_wordclasses.append(ud_pos_tag_wc)  # save word class
        # Count all predicted word classes and save the result
        self.counted_wordclasses[ud_pos_tag] = Counter(pred_wordclasses)

    def calc_tpm_from_CW(self, counted_wordclasses):
        dim = len(self.ud_pos_tags_REST)
        transition_probability_matrix = np.zeros((dim, dim))
        for ud_pos_tag_key, trans_prob_mat_row in zip(counted_wordclasses,
                                                      transition_probability_matrix):
            counter = counted_wordclasses[ud_pos_tag_key]
            cnt = counter.items()
            total = sum(counter.values())
            for elem, nmb in cnt:
                idx = self.ud_pos_tags_REST.index(elem)
                trans_prob_mat_row[idx] = nmb / total
        return transition_probability_matrix

    def barplot(self, checkpoint_info="", images_dir=None):
        # definitely available because <do_prediction()> was called before
        print("\nBARPLOT")
        self.counted_wordclasses = self.load_prediction_matrix("pkl", checkpoint_info)

        """ Bar plot """
        plt.rcParams.update({'font.size': 15})
        path = f"{plots_dir}/{self.full_name}/{checkpoint_info}"  # Used for concatenation of the barplots (below for-loop)
        dim = len(self.ud_pos_tags_REST)
        from Hippocampus_Language_Framework.visualize_variables import fs_length
        transition_probability_matrix = self.load_prediction_matrix("npy", checkpoint_info)
        for i, (ud_pos_tag, trans_prob_mat_row) in enumerate(zip(self.ud_pos_tags_REST,
                                                                 transition_probability_matrix)):
            # """ Single barplots of learned probabilities """
            # Nur Werte, für die Groudn Truth und TPM != 0 sind, werden geplottet
            fig_bar, ax = plt.subplots(figsize=(fs_length, fs_length))

            ind_non_0 = np.where(trans_prob_mat_row != 0)[0]
            x = [i for i in range(len(ind_non_0))]

            ax.set_title(f"{ud_pos_tag} → ___")
            ax.bar(x, trans_prob_mat_row[ind_non_0])
            ax.tick_params(labelsize=16)
            ax.set_xticks(x)
            ax.set_xticklabels([self.ud_pos_tags_REST[i] for i in ind_non_0], rotation=60, ha='right')
            ax.set_ylabel("Probability", fontsize=16)

            """ Combined barplots"""
            fig_bar2, ax2 = plt.subplots(figsize=(fs_length, fs_length))
            height_gt_row_i = self.ground_truth[i].copy()
            ind_non_0_gt = np.where(height_gt_row_i != 0)[0]
            # Filter in both rows fot the indices with elements != 0
            indices = list(set(ind_non_0) | set(ind_non_0_gt))
            x = np.asarray([xx for xx in range(len(indices))])

            ax2.set_title(f"{ud_pos_tag} → ___")
            ax2.set_ylabel("Probability")
            ax2.set_xticks(x)
            ax2.set_xticklabels([self.ud_pos_tags_REST[i] for i in indices], rotation=60, ha='right')

            heights_adjusted = trans_prob_mat_row[indices]
            height_gt_row_i = height_gt_row_i[indices]
            width = 0.35
            ax2.bar(x - width/2, heights_adjusted, width=width, color="deepskyblue", align="center", label="learned")
            ax2.bar(x + width/2, height_gt_row_i, width=width, color="green", align="center", label="ground truth")
            ax2.legend(loc="upper right", fancybox=False, shadow=False)

            """ Concatenate intermediate barplots """
            marker = "F" if i <= 4 else "S"
            from Hippocampus_Language_Framework.visualize_functions import save_image
            save_image(self, fig_bar, f"Single_Barplot_{ud_pos_tag}_{marker}", checkpoint_info=checkpoint_info)
            save_image(self, fig_bar2, f"Combined_Barplot_{ud_pos_tag}_{marker}", checkpoint_info=checkpoint_info)
        # if (i+1) % 5 == 0:
        #     if i <= 4:
        files = glob(f"{path}/Single_Barplot*F.png")
        subprocess.run(["gm", "convert"] + files + ["+append", f"{path}/Single_First_Barplot.png"])
        files = glob(f"{path}/Combined_Barplot*F.png")
        subprocess.run(["gm", "convert"] + files + ["+append", f"{path}/Combined_First_Barplot.png"])
        # else:
        files = glob(f"{path}/Single_Barplot*S.png")
        subprocess.run(["gm", "convert"] + files + ["+append", f"{path}/Single_Second_Barplot.png"])
        files = glob(f"{path}/Combined_Barplot*S.png")
        subprocess.run(["gm", "convert"] + files + ["+append", f"{path}/Combined_Second_Barplot.png"])

        # np.save(f"{images_dir}/{checkpoint_info}/barplot_heights.npy", heights)
        """ Concatenate full Barplots """
        print("\nCONCATENATE BARPLOTS")
        subprocess.run(["gm", "convert", f"{path}/Single_First_Barplot.png", f"{path}/Single_Second_Barplot.png", "-append", f"{path}/Single_Barplots.png"])
        subprocess.run(["gm", "convert", f"{path}/Combined_First_Barplot.png", f"{path}/Combined_Second_Barplot.png", "-append", f"{path}/Combined_Barplots.png"])
        single_files = glob(f"{path}/Single*Barplot.png")
        combined_files = glob(f"{path}/Combined*Barplot.png")
        subprocess.run(["rm", "-v"] + single_files + combined_files)

    def cleanup(self):
        # TODO diese Methode fertig schreiben
        for UD_pos_tag in self.ud_pos_tags_REST:
            del self.__dict__[UD_pos_tag]
        del self.training_data_y


