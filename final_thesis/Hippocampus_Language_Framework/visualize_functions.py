from os import makedirs
from os.path import isdir
from os.path import isfile

import numpy as np
from matplotlib import font_manager
from matplotlib import pyplot as plt

import Hippocampus_Language_Framework.math_functions as helper_functions
from Hippocampus_Language_Framework.models.Avg_W2V_W2V import Avg_W2V_W2V
from Hippocampus_Language_Framework.models.Avg_OHE_OHE import Avg_OHE_OHE
from Hippocampus_Language_Framework.models.BaseTextModel import BaseTextModel
from Hippocampus_Language_Framework.models.FirstModel import FirstModel
from Hippocampus_Language_Framework.models.MostFrequentWords import MostFrequentWords
from Hippocampus_Language_Framework.paths import pca_and_mds_matrices_dir
from Hippocampus_Language_Framework.paths import plots_dir
from Hippocampus_Language_Framework.visualize_variables import *


# simple function to generate proper MDS- or PCA-titles
def gen_title(title): return f"{pca_or_mds} of {title}"


def annotate_axes(ax, model_config):
    # Annotate axes is number of labes is <= 30 to avoid cluttering
    if len(model_config.axes_labels) <= 40:
        alx = model_config.axes_labels
        aly = model_config.axes_labels
    else:
        alx = aly = []

    if isinstance(model_config, FirstModel):
        tick_positions = [i for i in range(len(model_config.tick_position_helper))
                          if model_config.tick_position_helper[i] != ""]
        # x
        ax.set_xticks(tick_positions)
        ax.set_xticklabels(model_config.axes_labels, rotation=60, ha='right')
        # y
        ax.set_yticks(tick_positions)
        ax.set_yticklabels(model_config.axes_labels)
        pass
    else:
        # x
        ax.set_xticks(range(len(alx)))
        ax.set_xticklabels(alx, rotation=60, ha='right')
        # y
        ax.set_yticks(range(len(aly)))
        ax.set_yticklabels(aly, rotation='horizontal')


def single_matrix_plot(matrix, title, model_config):
    """
    Renders a single matrix plot to have the plot as stand alone and not only
    in the compendium with all other plots. With single images I have a higher
    flexibility in the thesis.
    :param checkpoint_info: if a checkpointed model is visualized, this parameter contains additional information
    :param matrix: matrix to plot
    :param title: title of the plot
    :param model_config: model instance, mainly for the call of <handly_rendered_image>
    :return:
    """
    fig, ax = plt.subplots(figsize=(fs_length, fs_height_matrix))
    ax.imshow(matrix)
    # ax.set_title(title)
    annotate_axes(ax, model_config)
    if isinstance(model_config, (Avg_OHE_OHE, Avg_W2V_W2V)):
        plt.tick_params(labelsize=16)
    elif isinstance(model_config, FirstModel):
        plt.tick_params(labelsize=12)
    else:
        plt.tick_params(labelsize=11.5)
    return fig, ax


def save_image(model_config, fig, title, checkpoint_info="", prefix=""):
    """  Zeigen und speichern des generierten Bildes bzw. der generierten Graphen """
    if show_image:
        plt.show()
    if save_image:
        prefix = prefix if not prefix else prefix + "_"
        images_dir = f"{plots_dir}/{prefix}{model_config.full_name}"
        if not isdir(images_dir):
            makedirs(images_dir)
        if checkpoint_info:
            checkpoint_dir = f"{images_dir}/{checkpoint_info}"
            if not isdir(checkpoint_dir):
                makedirs(checkpoint_dir)
        title = title.replace(" ", "_")
        fig.savefig(f"{images_dir}/{checkpoint_info}/{title}.png")  # <checkpoint_info> may be empty, then
        # two / are added, which is on Linux no problem
        plt.close(fig)


def calculate_or_load(matrix, title, model_config, checkpoint_info=""):
    saved_matrices_dir = f"{pca_and_mds_matrices_dir}/{model_config.full_name_no_time}/{checkpoint_info}"
    if not isdir(saved_matrices_dir):
        makedirs(saved_matrices_dir)
    title2 = title.replace(" ", "_")
    saved_matrices_file = f"{saved_matrices_dir}/{title2}.npy"
    if not isfile(saved_matrices_file):
        result = helper_functions.create_pca(matrix) if pca_or_mds == "PCA" \
            else helper_functions.create_mds_matrix(matrix)
        np.save(saved_matrices_file, result)
    else:
        print(f"Load {pca_or_mds}-Matrix:\n\t{saved_matrices_file}")
        result = np.load(saved_matrices_file)
    return result


def cluster_plot(model_config, matrix, title, checkpoint_info=""):
    def annotate_helper(axes, row_idx, idx_color, text):
        """ Small and simple helper function """
        x = mds_pca_matrix[row_idx][0]
        y = mds_pca_matrix[row_idx][1]
        dist = 0.05
        factor_x = 1 + (dist if x > 0 else -dist)
        factor_y = 1 + (dist if y > 0 else -dist)
        axes.text(x * factor_x,
                  y * factor_y,
                  text,
                  fontsize=fontsize_annotation,
                  color=colors_for_wordclasses[idx_color] if idx_color >= 0 else "black")

    # MDS or PCA matrix
    mds_pca_matrix = calculate_or_load(matrix, title, model_config, checkpoint_info)
    fig, ax = plt.subplots(figsize=(fs_length, fs_height_cluster))
    # ax.set_title(title)

    """ Generate scatter plot """
    # By this if-(else-if-else)-construction is a call to <ax.legend()> avoided if <model_config> == <MostFrequentWords>,
    # because then, no labels (thus no legend) is configured.
    if isinstance(model_config, MostFrequentWords):
        fig2, ax2 = plt.subplots(figsize=(fs_length, fs_height_cluster))
        ax2.set_xticks([])
        ax2.set_yticks([])
        for row_idx, (word, _, ud_pos_tag) in enumerate(model_config.most_frequent_words):
            ax.scatter(mds_pca_matrix[row_idx][0],
                       mds_pca_matrix[row_idx][1],
                       c="black")
            annotate_helper(ax, row_idx, -1, word)
            try:
                i = model_config.ud_pos_tags_REST.index(ud_pos_tag)
            except ValueError:
                i = model_config.ud_pos_tags_REST.index("REST")
            ax2.scatter(mds_pca_matrix[row_idx][0],
                        mds_pca_matrix[row_idx][1],
                        c=colors_for_wordclasses[i])
            annotate_helper(ax2, row_idx, i, ud_pos_tag)
        save_image(model_config, fig2, "ud_pos_tag_annotated")
    else:  # call to <ax.legend()> only if no <MostFrequentWords>-model
        if isinstance(model_config, (Avg_OHE_OHE, Avg_W2V_W2V)):
            for idx, ud_pos_tag in enumerate(model_config.axes_labels):
                ax.scatter(mds_pca_matrix[idx, 0],
                           mds_pca_matrix[idx, 1],
                           c=colors_for_wordclasses[idx],
                           label=ud_pos_tag,
                           s=marker_size)
        else:
            # Wortart für Wortart plotten, damit man am Ende eine schöne Legende hat.
            # Die Legende bedient sich den spezifizierten Labels. Plottet man bspw. jeden Punkt einzeln (vorherige Version)
            # kann man keine schöne Legende generieren und müsste sich irgendwelcher Workarounds bedienen.
            start_wordclass, end_wordclass = 0, 0
            for idx_wordclass, wordclass in enumerate(model_config.words):
                end_wordclass += len(wordclass)
                ax.scatter(mds_pca_matrix[start_wordclass:end_wordclass, 0],
                           mds_pca_matrix[start_wordclass:end_wordclass, 1],
                           c=colors_for_wordclasses[idx_wordclass],
                           label=
                           model_config.ud_pos_tags_REST[idx_wordclass] if isinstance(model_config, BaseTextModel)
                           else model_config.axes_labels[idx_wordclass],
                           s=marker_size)
                start_wordclass = end_wordclass
        # Place legend below plot
        ncol = \
            5 if isinstance(model_config, BaseTextModel) \
                else 3 if (isinstance(model_config, FirstModel) and model_config.nmb_rules == 4) \
                else 4
        # elif (isinstance(model_config, FirstModel) and model_config.nmb_rules == 4) \
        # else 4
        if not isinstance(model_config, (Avg_OHE_OHE, Avg_W2V_W2V)):
            font = font_manager.FontProperties(size=13)
            ax.legend(loc='upper center', bbox_to_anchor=(0.5, -0.025),
                      fancybox=False, shadow=False, ncol=ncol, prop=font)
    ax.set_xticks([])
    ax.set_yticks([])

    """ Annotate each dot with the word it represents """
    # Differentiate between <AverageModel> and <!AverageModel> because of the for-loop(s)
    if isinstance(model_config, (Avg_OHE_OHE, Avg_W2V_W2V)):
        for row_idx, ud_pos_tag in enumerate(model_config.ud_pos_tags_REST):
            annotate_helper(ax, row_idx, row_idx, ud_pos_tag)
    # TODO Wird evtl. gelöscht, nur zur Kontrolle da
    # Zu voll, selbst mit den <ud_pos_tag>-Annotationen
    # else:
    #     # Iteration über alle Wörter. Die Wörter gemäß ihrer Wortart, die man per Index in my_model_class.words-Liste erhält, einfärben
    #     i = 0
    #     for idx_wordclass, wordclass in enumerate(model_config.words):
    #         # idx   in Liste "my_model_class.words" beschreibt der Index die Wortart
    #         #       Dieser wird verwendet, um in "config#colors" die Farbe auszuwählen, sodass alle Wortarten in derselben Farbe dargestellt werden
    #         for word in wordclass:
    #             text = word
    #             if isinstance(model_config, BaseTextModel):  # Muss not be executed for <AverageModel> but doesn't happen due to
    #                 # outermost if-else
    #                 # text = f"{word} {model_config.ud_pos_tags_REST[idx_wordclass]}"
    #                 text = model_config.ud_pos_tags_REST[idx_wordclass]
    #             annotate_helper(i, idx_wordclass, text)
    #             i += 1
    return fig, ax
