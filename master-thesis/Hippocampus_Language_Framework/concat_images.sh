#!/bin/bash

# $1: model_config.full_name (complete name of the model including time to specify the directory where the images are located
# $2: checkpoint_info (if checkpointed model was used for visualization the directory marker is passed via $2, if it is empty then nothing happens)
# $3: model_config.ground_truth_full_name (name of the ground truth distribution)
# $4: d_f (discount factor for the concatenation. In case multiple discount factors are tested, it is necessary to concatenate all generated images)
# $5: pca_or_mds (marker whether "PCA" or "MDS" plots were generated and shall be concatenated)

full_path="$(pwd)/Hippocampus_Language_Framework/saved/plots/$1/$2"
# Mir ist nicht ganz klar, warum man hier absolute Pfade benötigt.
ln -sf $(pwd)/Hippocampus_Language_Framework/saved/ground_truths/$3.png $full_path
ln -sf $(pwd)/Hippocampus_Language_Framework/saved/ground_truths/$5_$3.png $full_path
cd $full_path

# Concatenate Matrix plots
gm convert \
  $3.png \
  Transition_Probability_Matrix*$4*.png \
  SR*$4*.png \
    +append SR_matrices.png

# Concatenate $5 plots (<gm> sorts arguments alphabetically, so TPM plot has to come first
gm convert \
  $5_$3.png \
  $5_of_Transition_Probability_Matrix*$4*.png \
  $5_of_SR*$4*.png \
    +append $5_plots.png

final_img="complete_plot.png"
if [ -f $final_img ]; then  # Check if "complete_plot.png" exists, if so append the new rows
  gm convert $final_img SR_matrices.png $5_plots.png -append $final_img
else  # if not, initialize "complete_plot.png"
  gm convert SR_matrices.png $5_plots.png -append $final_img
fi

rm -v SR_matrices.png $5_plots.png
