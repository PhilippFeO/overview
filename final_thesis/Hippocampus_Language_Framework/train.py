import json
from os import makedirs
from os.path import isdir
import socket

import keras.callbacks
import tensorflow as tf
from tensorflow.python.keras.callbacks import TensorBoard

from Hippocampus_Language_Framework.models.BaseModel import BaseModel
from Hippocampus_Language_Framework.git_auto import git
from Hippocampus_Language_Framework.models.BaseTextModel import BaseTextModel
from Hippocampus_Language_Framework.paths import saved_models_dir
from Hippocampus_Language_Framework.paths import tensorboard_dir


def main(model_config: BaseModel, tensorboard_log=False):
    is_BaseTextModel = isinstance(model_config, BaseTextModel)

    if is_BaseTextModel and model_config.resume_training:
        model_dir = f"{saved_models_dir}/{model_config.resume_training}"
    else:
        model_dir = f"{saved_models_dir}/{model_config.full_name}"
    if not isdir(model_dir):
        makedirs(model_dir)

    # Bei ÄNDERUNG AUCH DIE ENTSPRECHENDE STELLE IN <visualize.py> anpassen!
    checkpoint_model_name = f"model_epoch-{{epoch}}.h5"
    if is_BaseTextModel and model_config.period > 0:
        checkpoint = keras.callbacks.ModelCheckpoint(f"{model_dir}/{checkpoint_model_name}",
                                                     period=model_config.period)
    else:
        checkpoint = None
    # CIP complains if no list is provided in the <callbacks>-field of <model.fit>
    if checkpoint and "cip" in socket.gethostname():
        checkpoint = [checkpoint]

    # save parameters (before training, so in case of abortion the params are already saved)
    params = {"input_size": model_config.input_size}
    params_json = json.dumps(params)
    with open(f"{model_dir}/params.json", "w") as params_file:
        params_file.write(params_json)

    """ Resume or start Training """
    # Resume training
    if is_BaseTextModel and model_config.resume_training:
        model = keras.models.load_model(f"{model_dir}/model_epoch-{model_config.at_epoch}.h5")
        print("\nCheckpointed Model loaded!\n")
        # pass parameters
        model.compile(optimizer=tf.keras.optimizers.Adam(lr=0.01),
                      loss=model_config.loss_fct,
                      metrics=['accuracy'])
        model.fit(model_config.training_data_x,
                  model_config.training_data_y,
                  initial_epoch=model_config.at_epoch,
                  epochs=model_config.epochs,
                  verbose=1,  # Zeigt Fortschrittsanzeige (s. unter diesen Funktionsaufruf)
                  batch_size=model_config.batch_size,
                  callbacks=checkpoint)
    # start training
    else:
        model = model_config.build_model()

        # pass parameters
        model.compile(optimizer=tf.keras.optimizers.Adam(lr=0.01),
                      loss=model_config.loss_fct,
                      metrics=['accuracy'])

        # config tensorboard
        tb = None
        tb_folder = model_config.full_name
        if tensorboard_log:
            tb = TensorBoard(log_dir=f"{tensorboard_dir}/{tb_folder}")

        # train
        model.fit(model_config.training_data_x,
                  model_config.training_data_y,
                  epochs=model_config.epochs,
                  verbose=1,  # Zeigt Fortschrittsanzeige (s. unter diesen Funktionsaufruf)
                  batch_size=model_config.batch_size,
                  callbacks=checkpoint)  # TODO_ Wofür ist dieser Parameter? Antwort: Für Statistiken, s. „MeinWiki“ (unter Python -> keras, Stand: 10.3.)
        # verbose 0: no progress indication
        # verbose 1: progress bar for every epoch
        # verbose 2: only one line per epoch

    model.save(f"{model_dir}/model.h5")

    # my_model_class.cleanup()

    # Do some git operations based on executing on the CIP or at home
    git(f"{model_dir}")
