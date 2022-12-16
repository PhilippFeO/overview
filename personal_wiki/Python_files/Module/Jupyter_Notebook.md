# Jupyter Notebook
Angelegt Montag 07 März 2022
@python @deeplearnin @tensorflow @keras

<https://jupyter.org/>
Doku: <https://jupyter-notebook.readthedocs.io/en/latest/>

Mit Jupyter Notebooks kann interaktiv ein Python-Programm entwickelt werden, da man Code problemlos abschnittsweise ausführen kann.

In Text-Fenstern kann [Markdown – Notizen]() verwendet werden.

Shell-Befehle beginnen mit ``!``, bspw.
``!pip install <PACKAGE>``.

Verwendung
----------

* Notebook starten per

``jupyter notebook NOTEBOOK.ipynb``

### Tastenkürzel
Durch die Tastenkürzel wird die Verwendung erst effizient

#### Befehlsmodus
``ESC`` aktiviert Befehlsmodus (ggfl. Standard-mäßig aktiviert)

* ``ENTER`` Bearbeitungsmodus aktivieren
* ``STRG + ENTER`` Zelle ausführen
* ``SHIFT + ENTER`` Zelle ausführen und Untere auswählen (wird ggfl. hinzugefügt)



* ``F`` Suche & Ersetze
* ``A`` Zelle oberhalb einfügen
* ``B`` Zelle unterhalb einfügen
* ``X`` Zelle ausschneiden
* ``C`` Zelle kopieren
* ``V`` (Kopierte) Zelle unterhalb einfügen
* ``SHIFT + V`` (Kopierte) Zelle oberhalb einfügen
* ``Z`` Zelllöschung rückgängig machen
* ``D, D`` Zelle löschen
* ``L`` Zeilennummerierung ein- & ausschalten
	* ``SHIFT + L`` Zeilennummerierung in allen Zellen ein- & ausschalten

 

* ``M`` Markdown-Zelle
* ``1-6`` Zelle wird jeweilige Überschrift
* ``Y`` Code-Zelle
* ``R`` „Raw“-Zelle (Rahmen und Text bleiben, wie sie sind)



#### Bearbeitungsmodus

* ``STRG + M`` Befehlsmodus aktivieren
	* ``ESC`` Befehlsmodus aktivieren
* ``SHIFT + ENTER`` Zelle ausführen und Untere auswählen (wird ggfl. hinzugefügt)
* ``ALT + ENTER`` Zelle ausführen und unterhalb Neue einfügen
* ``STRG + SHIFT + -`` Teile Zelle beim Cursor auf
* ``STRG + /`` Kommentar
* ``STRG + D`` Löscht Zeile
* ``STRG + ↑`` Gehe zu Zellanfang
* ``STRG + ↓`` Gehe zu Zellende


Config
------

* In [~/.jupyter/jupyter_notebook_config.py](file:///home/philipp/.jupyter/jupyter_notebook_config.py) habe ich

``# c.NotebookApp.use_redirect_file = True``
zu
``c.NotebookApp.use_redirect_file = False``
geändert, da Firefox irgendwie nicht auf das gestartet Jupyter Notebook zugreifen konnte (<https://stackoverflow.com/questions/70753768/jupyter-notebook-access-to-the-file-was-denied>)

Collaboratory
-------------

* [Colaboratory](https://colab.research.google.com) ist eine Internetseite von Google, auf der man direkt in Jupyter Notebooks programmieren kann. Es läuft vollständig in der Cloud ist nach [Deep Learning with Python]() gut geeignet, um mit [Keras – MeinWiki.Python](./keras.md) und [TensorFlow – MeinWiki.Python](./TensorFlow.md) zu programmieren.
* Man muss GPU-Ausführung explizit erwähnen.


