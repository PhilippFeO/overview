# Python
Angelegt Freitag 14 Januar 2022


* [Python-Tutorial – python-Doku](https://docs.python.org/3/tutorial/)
* [Match-Statement (Pattern Matching, switch-case) – python-Doku](https://docs.python.org/3/tutorial/controlflow.html#match-statements)
	* [PEP 636: Match-Statement – python-Doku](https://peps.python.org/pep-0636/)


Interessant
-----------

* <https://automatetheboringstuff.com/#toc>
	* <https://automatetheboringstuff.com/2e/chapter12/>
	* Es gibt auch andere Bücher („Hack secret ciphers with python“ oder so ähnlich)
* [Python Popular Recepies – code.activestate.com](https://code.activestate.com/recipes/langs/python/)


Optionen
--------
``-c "<CODE>"`` Führt ``<CODE>``, bspw. ``"a = 5 + 7; print(a)"`` aus
``-m`` Startet Skript als Modul (TODO Was genau ein Modul ist, weiß ich nicht)
[id: python-http-server]Starte einfachen ``HTTP``-Server in/auf einem Verzeichnis.
	cd DIR
	sudo python3 -m http.server 80
	
Man kann nun per [:Linux:**wget**]() oder [:Linux:**curl**]() ``HTTP``-Anfragen stellen, bspw. Dateien aus diesem Verzeichnis herunterladen. Hilfreich, wenn man eine Datei auf Zielrechner hochladen möchte, wie bei [Linux:TryHackMe:**Reverse & Bind Shells#socat**]().
@itsicherheit

pip
---
@pip @python @virtualenv

* **Im Folgenden wird angenommen, dass alle Befehle innerhalb einer (aktiven) „Virtual Enviornment“ aufgerufen werden**
* auch „Virtual Enviornment“
* Ist eine **virtualenv** aktiv, werden die per ``pip`` installierten Module in den Ordner der **virtualenv** installiert bzw. dort gespeichert


### Installation
``sudo apt-get install python3-pip``
Wird, glaube ich, automatisch mit
``sudo apt-get install python3-venv``
(s. „Virutal Enviornment“) installiert.

### Verwendung

* Modul installieren: ``pip3 install [MODUL ...]``
* Modul deinstallieren: ``pip3 uninstall [MODUL ...]``
* Alle installiereten Module auflisten: ``pip3 list``
* Infos über ein Modul anzeigen: ``pip3 show MODUL``
* ``pip freeze > requirements.txt``
	* ``pip freeze | grep MODUL >> requirements.txt``
* [id: pip-install-from-file]``pip install -r DATEI`` installiert alle in ``DATEI`` gelisteten Module


#### Weitere Verwendungen

* Modul direkt aus **gitlab** oder **github** installieren: ``pip3 install --user https://github.com/pallets/jinja/archive/master.zip``


### Quellen

* [UbuntuUsers/pip](https://wiki.ubuntuusers.de/pip/)


Virtual Enviornment
-------------------
@virtualenv @python @pip

* [venv – UbuntuUser](https://wiki.ubuntuusers.de/venv/)
* Um Module Projekt-abhängig zu installieren und zu verwalten, bieten sich **virtual enviornments** an. Diese können auch direkt in PyCharm verwendet werden. Alle virtualenvs sollten im Ordner [~/.virtualenvs/](file:///home/philipp/.virtualenvs) gespeichert werden


Sicherung, Backup
-----------------
@backup

* Die Sicherungen der ``venv``s ([~/.virtualenvs/](file:///home/philipp/.virtualenvs)) liegen in [~/programmieren/python/venv-requirements.tar.gz](file:///home/philipp/programmieren/python/venv-requirements.tar.gz) und werden mit [~/programmieren/skripte/backup-venvs.sh](file:///home/philipp/programmieren/skripte/backup-venvs.sh) erstellt.

Genauer gesagt: Für jede ``venv`` wird
``pip freeze > REQUIREMENTS.txt``
ausgeführt und dann alle Dateien archiviert und in [~/programmieren/skripte/backup-venvs.sh](file:///home/philipp/programmieren/skripte/backup-venvs.sh) gespeichert.

* Sie werden leider nicht in [~/.virtualenvs/](file:///home/philipp/.virtualenvs) gespeichert, weil ich diesen Ordner mit [:Einrichtung meines PCs:**grsync**]() ausschließe und es nicht geschafft habe einzelne Dateien trotzdem zu kopieren.

Für Widerherstellung der ``venv``s, s. [#pip-install-from-file](#Python)

### Installation
``sudo apt-get install python3-venv``

### Verwendung

* Anlegen: ``python3 -m venv ~/.virtualenvs/<VIRTUAL_ENV>``
* Aktivieren: ``source ~/.virtualenvs/<VIRTUAL_ENV>/bin/activate``
* Deaktivieren: ``deactivate`` (egal wo, geht immer) oder Terminal mit ``exit`` beenden
* Löschen: ``[sudo] rm -r ~/.virtualenvs/<VIRTUAL_ENV>/``


