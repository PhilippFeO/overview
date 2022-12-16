# Code-Beispiele
Angelegt Dienstag 01 März 2022

Diverses
--------

* Mittleren 14x14-Pixelausschnitt wählen

	my_slice = train_images[:, 7:-7, 7:-7]


Einfaches neuronales Netz
-------------------------

### 1. Schritt: Model-Definition
	from tensorflow import keras
	from tensorflow.keras import layers
	model = keras.Sequential([
		layers.Dense(512, activation="relu"),
		layers.Dense(10, activation="softmax")
	])


### 2. Schritt: Model kompilieren (zusammensetzen)
	model.compile(
		optimizer="rmsprop",
		loss="sparse_categorical_crossentropy",
		metrics=["accuracy"])


### 3. Schritt: Model trainineren („fit to its training data“)
	model.fit(
		train_images,
		train_labels,
		epochs=5,
		batch_size=128)


### 4. Schritt: Model auf Testdaten anwenden
	model.predict(test_data)


### 5. Schritt: Model evaluieren
Durchschnittliche Genauigkeit „accuracy“ (s. [Lexikon: Metrics – MeinWiki]()) auf den Testdaten berechnen
	test_loss, test_acc = model.evaluate(test_images, test_labels)

