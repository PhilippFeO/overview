import subprocess

import matplotlib.pyplot as plt
from Hippocampus_Language_Framework.visualize_variables import fs_length, fs_height_matrix

ger_ohe = [0.15, 0.20, 0.27, 0.19, 0.18, 0.22, 0.32, 0.12, 0.31, 0.25]
ger_w2v = [0.79, 0.38, 0.38, 0.40, 0.35, 0.49, 0.69, 0.58, 0.47, 0.50]
eng_w2v = [0.44, 0.21, 0.28, 0.24, 0.36, 0.15, 0.52, 0.29, 0.46, 0.16]
eng_ohe = [0.48, 0.16, 0.15, 0.13, 0.18, 0.12, 0.40, 0.30, 0.41, 0.19]
x = list(range(10))
y_ticks = [i/10 for i in x + [10]]

label = ["Ger OHE", "Ger WV", "Eng OHE", "Eng WV"]

x_labels = ["ADJ", "ADV", "AUX", "NOUN", "PRON", "VERB", "DET", "ADP", "PART", "REST"]

fontsize = 16
dir_plots = "/home/philipp/Universität/Informatik/Master_Informatik/masterarbeit/Arbeit/code"

fig, ax = plt.subplots(1, 1, figsize=(fs_length, fs_height_matrix))
ax.plot(x, ger_ohe, color="b", label="Ger. OHE")
ax.scatter(x, ger_ohe, color="b")
ax.plot(x, eng_ohe, color="g", label="Eng. OHE")
ax.scatter(x, eng_ohe, color="g")
ax.set_xticks(x)
ax.set_xticklabels(x_labels, rotation=60, ha='right')
ax.set_yticks(y_ticks)
ax.set_ylabel("RMSE (row wise)", fontsize=fontsize)
ax.tick_params(labelsize=fontsize)
ax.legend(fontsize=fontsize)
ger_plot = f"{dir_plots}/avg_table_ger.png"
fig.savefig(ger_plot)


fig, ax = plt.subplots(1, 1, figsize=(7.5, 7.5))
ax.plot(x, ger_w2v, color="deepskyblue", label="Ger. WV")
ax.scatter(x, ger_w2v, color="deepskyblue")
ax.plot(x, eng_w2v, color="limegreen", label="Eng. WV")
ax.scatter(x, eng_w2v, color="limegreen")
ax.set_xticks(x)
ax.set_xticklabels(x_labels, rotation=60, ha='right')
ax.set_yticks(y_ticks)
ax.set_ylabel("RMSE (row wise)", fontsize=fontsize)
ax.tick_params(labelsize=fontsize)
ax.legend(fontsize=fontsize)
eng_plot = f"{dir_plots}/avg_table_eng.png"
fig.savefig(eng_plot)

gm_cmd = f"gm convert {ger_plot} {eng_plot} +append {dir_plots}/plots.png"
subprocess.run(gm_cmd.split(" "))
