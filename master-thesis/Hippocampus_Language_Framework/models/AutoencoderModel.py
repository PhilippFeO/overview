import keras
from keras import layers

from Hippocampus_Language_Framework.models.FirstModel import FirstModel
from Hippocampus_Language_Framework.rules import rules


class AutoencoderModel(FirstModel):
    def __init__(self,
                 model_name="All Words AE",
                 epochs=15,  # TODO
                 batch_size=40,  # TODO
                 nmb_hidden_layers=0,
                 nmb_samples=500,  # > 5e5
                 nmb_words_per_class=100,
                 nmb_rules=len(rules)):  # Alle Regeln da alle Wortarten verwendet werden
        super().__init__(model_name=model_name,
                         epochs=epochs,
                         batch_size=batch_size,
                         nmb_hidden_layers=nmb_hidden_layers,
                         nmb_samples=nmb_samples,
                         nmb_words_per_class=nmb_words_per_class,
                         nmb_rules=nmb_rules)

    def build_model(self):
        encoding_dims = [self.input_size // 2, 100, 32]
        # Encoder
        input_vector = keras.Input(shape=(self.input_size, ))
        enc = layers.Dense(encoding_dims[0],
                           activation="relu")(input_vector)
        enc = layers.Dense(encoding_dims[1],
                           activation="relu")(enc)
        encoded = layers.Dense(encoding_dims[2],
                               activation="relu")(enc)
        # Decoder
        dec = layers.Dense(encoding_dims[2],
                           activation="softmax")(encoded)
        dec = layers.Dense(encoding_dims[1],
                           activation="softmax")(dec)
        dec = layers.Dense(encoding_dims[0],
                           activation="softmax")(dec)
        decoded = layers.Dense(self.input_size,
                               activation="relu")(dec)
        # Autoencoder
        pseudo_autoencoder = keras.Model(input_vector, decoded)
        return pseudo_autoencoder
