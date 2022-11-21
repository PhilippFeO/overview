import subprocess

import numpy as np
from matplotlib import pyplot as plt

from Hippocampus_Language_Framework.paths import ground_truth_dir
from Hippocampus_Language_Framework.paths import prediction_matrices_dir
from Hippocampus_Language_Framework.visualize_variables import fs_length

ger_ohe = f"{prediction_matrices_dir}/Avg_OHE_OHE_5000E_100BS_1L_1C_200P_1500T_D/_epoch-4000/Avg_OHE_OHE_5000E_100BS_1L_1C_200P_1500T_D.npy"
ger_w2v = f"{prediction_matrices_dir}/Avg_W2V_W2V_5000E_100BS_1L_1C_200P_1500T_D/_epoch-4000/Avg_W2V_W2V_5000E_100BS_1L_1C_200P_1500T_D.npy"
eng_ohe = f"{prediction_matrices_dir}/Avg_OHE_OHE_4000E_100BS_1L_1C_200P_1500T_J/Avg_OHE_OHE_4000E_100BS_1L_1C_200P_1500T_J.npy"
eng_w2v = f"{prediction_matrices_dir}/Avg_W2V_W2V_5000E_100BS_1L_1C_200P_1500T_J/_epoch-4000/Avg_W2V_W2V_5000E_100BS_1L_1C_200P_1500T_J.npy"
ger_tag_gt = f"{ground_truth_dir}/D_200pages_1500T_tags.npy"
eng_tag_gt = f"{ground_truth_dir}/J_200pages_1500T_tags.npy"

ger_ohe = np.load(ger_ohe)
ger_w2v = np.load(ger_w2v)
eng_ohe = np.load(eng_ohe)
eng_w2v = np.load(eng_w2v)
ger_tag_gt = np.load(ger_tag_gt)
eng_tag_gt = np.load(eng_tag_gt)

ud_pos_tags_REST = ["ADJ", "ADV", "AUX", "NOUN", "PRON",
                    "VERB", "DET", "ADP", "PART", "REST"]
fontsize = 16
dir_plots = "/home/philipp/Universität/Informatik/Master_Informatik/masterarbeit/Arbeit/code"
ohe_plot = "error_ohe.png"
w2v_plot = "error_w2v.png"

with open(f"{dir_plots}/mean_std_dev.txt", "w") as file:
    for j, (ger, eng) in enumerate(((ger_ohe, eng_ohe), (ger_w2v, eng_w2v))):
        fig, ax = plt.subplots(figsize=(3*fs_length, 1.5*fs_length))
        # x = np.asarray([i + (-1)**(i+1) * 0.1 for i in range(10)])  # für ger -1**2 = 1 für eng -1**3 = -1
        ax.set_xticks(range(10))
        y_tick_end, y_step_size, ymax = (31, 5, 0.33) if j == 0 else (45, 10, 0.52)
        ax.set_yticks([i/100 for i in range(0, y_tick_end, y_step_size)])
        ax.set_ylim(ymin=0, ymax=ymax)
        ax.tick_params(labelsize=fontsize)

        x_labels = ["ADJ → __", "ADV → __", "AUX → __", "NOUN → __", "PRON → __", "VERB → __", "DET → __", "ADP → __", "PART → __", "REST → __"]
        ax.set_xticklabels(x_labels, rotation=60, ha='right')

        width = 2
        lines = []

        ger_text, eng_text = ("Ger. OHE", "Eng. OHE") if j == 0 else ("Ger. WV", "Eng. WV")

        for i, (ohe_or_w2v, gt) in enumerate([(ger, ger_tag_gt), (eng, eng_tag_gt)]):
            diff = np.abs(ohe_or_w2v - gt)
            mean = np.mean(diff, axis=1)
            std_dev = np.std(diff, axis=1)
            # corr = np.correlate(ohe_or_w2v, gt, axis=1)
            # corr_mean = np.mean(corr)

            text = None
            if i == 0:
                text = f"{ger_text}: mean {np.mean(mean):.3f}, std_def: {np.std(mean):.3f}\n"
            else:
                text = f"{eng_text}: mean {np.mean(mean):.3f}, std_def: {np.std(mean):.3f}\n"
            file.write(text)

            below = [m - 0.001 if m - sd < 0 else sd for m, sd in zip(mean, std_dev)]
            color = "deepskyblue" if i == 0 else "green"
            label = ger_text if i == 0 else eng_text
            x = np.asarray(range(10))
            x = x + (-1)**(i+1) * 0.1
            ax.errorbar(x, mean, [below, std_dev], linewidth=width, linestyle="None",
                        elinewidth=width, capsize=10, capthick=width,
                        marker="o", markersize=10, color=color)
            line, = ax.plot([], [], color=color, label=label)
            lines.append(line)
        ax.legend(fontsize=fontsize)
        file_name = ohe_plot if j == 0 else w2v_plot
        fig.savefig(f"{dir_plots}/{file_name}")


gm_cmd = f"gm convert {dir_plots}/{ohe_plot} {dir_plots}/{w2v_plot} -append {dir_plots}/error_com.png"
subprocess.run(gm_cmd.split(" "))
