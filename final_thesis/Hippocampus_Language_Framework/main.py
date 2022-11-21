import socket
import subprocess

import Hippocampus_Language_Framework.train as train
import Hippocampus_Language_Framework.visualize as visualize
from Hippocampus_Language_Framework.git_auto import git
from Hippocampus_Language_Framework.models.BaseTextModel import BaseTextModel
from Hippocampus_Language_Framework.paths import plots_dir

if __name__ == '__main__':
    # from Hippocampus_Language_Framework.models.FirstModel import FirstModel
    # model_setup = FirstModel()

    # from Hippocampus_Language_Framework.models.FirstModel import FirstModel
    # model_setup = FirstModel(model_name="First Model + More Rules",
    #                          epochs=100,
    #                          batch_size=100,
    #                          nmb_hidden_layers=1,
    #                          nmb_samples=2000,
    #                          nmb_words_per_class=15,
    #                          nmb_rules=4)

    # from Hippocampus_Language_Framework.models.first_model import FirstModel
    # model_setup = FirstModel(model_name="More Words",
    #                             epochs=12,
    #                             batch_size=100,
    #                             nmb_hidden_layers=1,
    #                             nmb_samples=2000,  # 2000
    #                             nmb_words_per_class=20,
    #                             nmb_rules=4)

    # TODO Testen, da Klassen umstrukturiert
    # from Hippocampus_Language_Framework.models.first_model import FirstModel
    # model_setup = FirstModel(model_name="All Words",
    #                             epochs=15,  # TODO anpassen
    #                             batch_size=50,  # TODO anpassen
    #                             nmb_hidden_layers=1,
    #                             nmb_samples=5000,  # TODO anpassen auf >5e5
    #                             nmb_words_per_class=100,
    #                             nmb_rules=-1)

    # from Hippocampus_Language_Framework.models.ConlluModel import ConlluModel
    # model_setup = ConlluModel()

    # TODO Testen, da Klassen umstrukturiert
    # from Hippocampus_Language_Framework.models.AutoencoderModel import AutoencoderModel
    # model_setup = AutoencoderModel()

    # from Hippocampus_Language_Framework.models.OHE_OHE import OHE_OHE
    # model_setup = OHE_OHE(model_name="OHE_OHE",
    #                       epochs=500,
    #                       pages=5,
    #                       nmb_tokens=30,
    #                       book_name=1,
    #                       resume_training="OHE_OHE_500E_100BS_1L_1C_5P_30T_J_2022.06.24-17:44:50",
    #                       at_epoch=-1)

    # from Hippocampus_Language_Framework.models.W2V_OHE import W2V_OHE
    # model_setup = W2V_OHE(epochs=10,
    #                       pages=10,
    #                       book_name=0)

    # from Hippocampus_Language_Framework.models.W2V_W2V import W2V_W2V
    # model_setup = W2V_W2V(model_name="W2V_W2V",
    #                       epochs=50000,
    #                       pages=350,
    #                       nmb_tokens=0,
    #                       period=2000,
    #                       book_name=0,
    #                       resume_training="W2V_W2V_50000E_100BS_1L_1C_350P_0T_D_2022.06.11-01:37:15",
    #                       at_epoch=-1)

    # from Hippocampus_Language_Framework.models.Avg_OHE_OHE import Avg_OHE_OHE
    # model_setup = Avg_OHE_OHE(epochs=5000,
    #                           pages=200,
    #                           book_name=0,
    #                           nmb_tokens=1500,
    #                           resume_training="Avg_OHE_OHE_5000E_100BS_1L_1C_200P_1500T_D_2022.06.22-02:29:37",
    #                           at_epoch=-1)

    from Hippocampus_Language_Framework.models.Avg_W2V_W2V import Avg_W2V_W2V
    model_setup = Avg_W2V_W2V(epochs=10,
                              pages=5,
                              book_name=0)
                              # nmb_tokens=1500,
                              # resume_training="",
                              # at_epoch=-1)

    # from Hippocampus_Language_Framework.models.MostFrequentWords import MostFrequentWords
    # model_setup = MostFrequentWords(epochs=10,
    #                                 pages=5)

    cip = True
    do_training = \
        False if isinstance(model_setup, BaseTextModel) and model_setup.resume_training and model_setup.at_epoch < 0 \
        else True

    if do_training:
        train.main(model_setup, tensorboard_log=False)
        # pass

    # Nur visualisieren, wenn man nicht im CIP ist
    # bzw. CIP nur für's Training nutzen
    if cip or "cip" not in socket.gethostname():
        # pass
        visualize.main(model_setup)
        # Concatenate generated images
        # print("\nCONCATENATE IMAGES\n")
        # subprocess.run(["./Hippocampus_Language_Framework/concat_images.sh", model_setup.full_name, model_setup.ground_truth_file_name])
        # git(f"{plots_dir}")
        pass
