import json
import os
import subprocess
from glob import glob
import keras.models

from Hippocampus_Language_Framework.math_functions import calc_dist
from Hippocampus_Language_Framework.math_functions import row_dist
from Hippocampus_Language_Framework.models.Avg_W2V_W2V import Avg_W2V_W2V
from Hippocampus_Language_Framework.models.BaseModel import BaseModel
from Hippocampus_Language_Framework.models.MostFrequentWords import MostFrequentWords
from Hippocampus_Language_Framework.paths import ground_truth_dir
from Hippocampus_Language_Framework.paths import saved_models_dir
from Hippocampus_Language_Framework.visualize_functions import *


def main(model_config: BaseModel):
    """
    Calculates different plots and diagrams for each model
    """
    # assert (mds and not pca) or (pca and not mds), f"<mds = {mds}> and <pcs = {pca}> but only one must be <True>"

    is_BaseTextModel = isinstance(model_config, BaseTextModel)

    if isinstance(model_config, BaseTextModel) and model_config.resume_training:
        model_dir = f"{saved_models_dir}/{model_config.resume_training}"
    else:
        model_dir = f"{saved_models_dir}/{model_config.full_name}"
    # In case there are checkpoints of the model, the visualization procedure is repeated for them
    model_dir_enc = os.fsencode(model_dir)
    for file in os.listdir(model_dir_enc):
        filename = os.fsdecode(file)
        if filename.endswith(".h5"):
            print(f"\n==== VISUALIZATION FOR {filename} ====")
            # Extract checkpoint marker: _epoch-\d* or "" if <model.h5> is processed
            checkpoint_info = filename[5:-3]

            """ Load Model and parameters """
            path = f"{model_dir}/model{checkpoint_info}.h5"
            print(f"Load Model:\n\t{path}")
            loaded_model = keras.models.load_model(path, compile=False)
            with open(f"{model_dir}/params.json") as params_json:
                params = json.load(params_json)

            """ Predict """
            print("\nPREDICT")
            transition_probability_matrix = \
                model_config.do_prediction(model=loaded_model,
                                           input_size=params["input_size"],
                                           checkpoint_info=checkpoint_info)

            """ Generate plots """
            """ Ground Truth """
            # ground truth works only for <BaseTextModel> but without <MostFrequentWords> (because then no sqaured <transition_probability_matrix>)
            if plot_ground_truth and is_BaseTextModel and not isinstance(model_config, MostFrequentWords):
                print("\nGROUND TRUTH")
                title = f"Ground Truth for {model_config.pages} pages"
                ground_truth_path = f"{ground_truth_dir}/{model_config.ground_truth_file_name}.png"
                if not isfile(ground_truth_path):
                    fig_gt, _ = single_matrix_plot(model_config.ground_truth, title, model_config)
                    fig_gt.savefig(ground_truth_path)

                ground_truth_cluster_path = f"{ground_truth_dir}/{pca_or_mds}_{model_config.ground_truth_file_name}.png"
                if not isfile(ground_truth_cluster_path):
                    cluster_plot_title = gen_title(title)
                    fig_mds, _ = cluster_plot(model_config, model_config.ground_truth, cluster_plot_title, checkpoint_info)
                    fig_mds.savefig(ground_truth_cluster_path)

                if isinstance(model_config, (Avg_OHE_OHE, Avg_W2V_W2V)):
                    model_config.barplot(checkpoint_info=checkpoint_info)

            for d_f in ddff:
                print(f"\n ---- d_f = {d_f} ----")
                """ Transition Probability Matrix """
                if plot_TPM:
                    print("\nTRANSITION PROBABILITY MATRIX")
                    title = f"Transition Probability Matrix; t={1}, DF={d_f}"
                    fig2, ax = single_matrix_plot(transition_probability_matrix, title, model_config)
                    save_image(model_config, fig2, title, checkpoint_info)

                    cluster_plot_title = gen_title(title)
                    fig_mds, _ = cluster_plot(model_config, transition_probability_matrix, cluster_plot_title, checkpoint_info)
                    save_image(model_config, fig_mds, cluster_plot_title, checkpoint_info)

                    if is_BaseTextModel and not isinstance(model_config, MostFrequentWords):
                        calc_dist(model_config.ground_truth, transition_probability_matrix, title, checkpoint_info, model_config.full_name)
                    if isinstance(model_config, (Avg_OHE_OHE, Avg_W2V_W2V)):
                        row_dist(model_config.ground_truth, transition_probability_matrix, checkpoint_info, model_config.full_name)

                    # in this case all other calculations don't make sense because the <transition_probability_matrix> is not
                    # squared but has the shape of <model_config.nmb_most_frequent_words> × <len(model_config.cognitive_room>
                    if isinstance(model_config, MostFrequentWords):
                        break

                """ Successor Representation """
                # Calculate all graphs according to settings in <visualize_variables.py>
                if plot_SR_matrix:
                    print("\nSR-MATRIX")
                    # TODO Achsenbeschriftungen färben
                    #  https://stackoverflow.com/questions/21936014/set-color-for-xticklabels-individually-in-matplotlib
                    for i in tt:
                        title = f"SR, t={i}, DF={d_f}"
                        print(f"\t{title}")
                        SR_matrix = helper_functions.create_SR(transition_probability_matrix,
                                                               discount_factor=d_f,
                                                               sequence_length=i)
                        fig2, ax = single_matrix_plot(SR_matrix, title, model_config)
                        save_image(model_config, fig2, title, checkpoint_info)

                        print(f"\nSR-MATRIX – CLUSTER\n\t{title}")
                        cluster_plot_title = gen_title(title)
                        fig3, ax = cluster_plot(model_config, SR_matrix, cluster_plot_title, checkpoint_info)
                        save_image(model_config, fig3, cluster_plot_title, checkpoint_info)

                        if is_BaseTextModel:
                            calc_dist(model_config.ground_truth, SR_matrix, title, checkpoint_info, model_config.full_name)
                    # print("\n")
                print("\nCONCATENATE IMAGES")
                if is_BaseTextModel:
                    subprocess.run(["./Hippocampus_Language_Framework/concat_images.sh",
                                    model_config.full_name,
                                    checkpoint_info,
                                    model_config.ground_truth_file_name,
                                    str(d_f),
                                    pca_or_mds])
                else:  # <model_config> == <FirstModel>
                    path = f"{plots_dir}/{model_config.full_name}/{checkpoint_info}"  # Used for concatenation of the barplots (below for-loop)
                    sr_matrices = f"{path}/SR_matrices.png"
                    cluster_plots = f"{path}/{pca_or_mds}s.png"
                    file_TPM = glob(f"{path}/Transition_Probability_Matrix*.png")
                    files_SR = sorted(glob(f"{path}/SR*.png"))  # sorted, because columns should be aligned, otherwise not guaranteed
                    subprocess.run(["gm", "convert"] + file_TPM + files_SR + ["+append", sr_matrices])
                    file_TPM = glob(f"{path}/{pca_or_mds}_of_Transition_Probability_Matrix*.png")
                    files_SR = sorted(glob(f"{path}/{pca_or_mds}_of_SR*.png"))  # sorted, because columns should be aligned, otherwise not guaranteed
                    subprocess.run(["gm", "convert"] + file_TPM + files_SR + ["+append", cluster_plots])
                    subprocess.run(["gm", "convert", sr_matrices, cluster_plots, "-append", f"{path}/complete_plot.png"])
                    subprocess.run(["rm", "-v", sr_matrices, cluster_plots])
