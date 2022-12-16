# Logging
Angelegt Sonntag 10 Juli 2022

Literatur
---------

* [Logging – python-Doku](https://docs.python.org/3/howto/logging.html)
* [Logging Cookbook – python-Doku](https://docs.python.org/3/howto/logging-cookbook.html#logging-cookbook) (sehr umfangreich mit Code, der sich in der Vergangenheit als sinnvoll herausgestellt hat)
	* Logging mit mehreren Threads
	* Logging mit mehreren Modulen
	* ...


Praxis
------

* Am Anfang ``logging.basicConfig(...)`` aufrufen, um bspw. [Log-Level – python-Doku](https://docs.python.org/3/library/logging.html#levels) zu konfigurieren (alle Argumente optional)
	* ``filename=<FILENAME.Log>`` Datei, in die der Log geschrieben werden soll
	* ``filemode="w"`` Log-Datei wird jedes Mal überschrieben und es wird nicht angehängt, was der Standard ist (wasl. ist auch ``filename="a"`` möglich aber überflüssig)
	* ``encoding=utf-8``
	* ``level=<LOGLEVEL>`` Welches [Log-Level – python-Doku](https://docs.python.org/3/library/logging.html#levels) verwendet werden soll (Standard ist ``logging.WARNING``), alles über dem angegebenen [Log-Level – python-Doku](https://docs.python.org/3/library/logging.html#levels) wird geloggt 
* Standard-[Log-Level – python-Doku](https://docs.python.org/3/library/logging.html#levels) ist ``WARNING``, dh.

	import logging
	logging.warning('Watch out!')  # will print a message to the console
	logging.info('I told you so')  # will not print anything

wird lediglich
	WARNING:root:Watch out!

printen. Alles **ab** dem spezifizierten [Log-Level – python-Doku](https://docs.python.org/3/library/logging.html#levels) wird geloggt.

* Level ändern:

	logging.basicConfig(level=logging.<LOGLEVEL>)


* Log-Level per Kommandozeile: ``--log=<LOGLEVEL>``, bspw. ``--log=INFO``. Im Code dann

	numeric_level = getattr(logging, loglevel.upper(), None)  # upper(), damit man „INFO“ und „info“ verwenden kann
	# Prüfen, ob Loglevel valide ist
	if not isinstance(numeric_level, int):
	    raise ValueError('Invalid log level: %s' % loglevel)
	logging.basicConfig(level=numeric_level, ...)
	



* [Logging to a file – python-Doku](https://docs.python.org/3/howto/logging.html#logging-to-a-file)

	import logging
	logging.basicConfig(filename='example.log', encoding='utf-8', filemode='w', level=logging.DEBUG)  # logging.DEBUG ist niedrigstes Log-Level, dh. alles wird angezeigt
	logging.debug('This message should go to the log file')
	logging.info('So should this')
	logging.warning('And this, too')
	logging.error('And non-ASCII stuff, too, like Øresund and Malmö')



* [Logging from multiple modules – python-Doku](https://docs.python.org/3/howto/logging.html#logging-from-multiple-modules)
* [Eigenes Log-Format – python-Doku](https://docs.python.org/3/howto/logging.html#changing-the-format-of-displayed-messages)
	* [Zeit und Datum in Log-Nachricht – python-Doku](https://docs.python.org/3/howto/logging.html#displaying-the-date-time-in-messages)


