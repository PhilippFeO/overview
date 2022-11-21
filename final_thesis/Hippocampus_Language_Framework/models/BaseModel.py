from time import strftime

import keras.losses
import numpy as np
import tensorflow as tf
from keras import Sequential
from keras.layers import Dense


class BaseModel:
    def __init__(self,
                 model_name,
                 epochs,
                 batch_size,
                 nmb_hidden_layers,
                 nmb_concatenations=1):
        self.model_name = model_name
        timestamp = strftime("%Y.%m.%d-%H:%M:%S")
        self.full_name = f"{model_name}_{epochs}E_{batch_size}BS_{nmb_hidden_layers}L_{nmb_concatenations}C_{timestamp}"
        self.full_name_no_time = f"{model_name}_{epochs}E_{batch_size}BS_{nmb_hidden_layers}L_{nmb_concatenations}C"
        self.epochs = epochs
        self.batch_size = batch_size
        self.nmb_hidden_layers = nmb_hidden_layers
        self.nmb_concatenations = nmb_concatenations

        self.last_activation = "softmax"
        self.loss_fct = keras.losses.categorical_crossentropy

        self.input_size = 0

        self.cognitive_room = []
        self.rules = []
        self.axes_labels = []
        self.training_data_x = []
        self.training_data_y = []
        self.words = []

    def prepare_date(self):
        pass

    def configure(self):
        pass

    def build_model(self):
        model = Sequential()
        # build model   Bis dato nur FC-Schichten
        # input layer
        model.add(Dense(self.input_size, activation=tf.nn.relu))
        for _ in range(self.nmb_hidden_layers):
            # fully connected layer
            model.add(Dense(int(self.input_size), activation=tf.nn.relu))
        # output layer
        model.add(Dense(self.input_size, activation=self.last_activation))
        return model

    def do_prediction(self, **kwargs):
        pass

    def cleanup(self):
        pass
