# bash
Angelegt Freitag 28 Oktober 2022

``for``-Schleife
----------------

* Java-Syntax für for-Schleifen  (müsste auch bei if & while funktionieren)

	for ((i=0; i<10; i++)); do COMMAND; done

	for i in {0..10}; do COMMAND; done


* ``for``-Schleife für Verzeichnisse oder bestimmte Dateien

	for file in *; do COMMAND; done
			#führt COMMAND für alle Dateien im aktuellen Verzeichnis aus
	for file in "*".pdf; do COMMAND; done
		# führt COMMAND für alle Dateien, die auf ".pdf" enden, aus
		#(Formatierung ggfl wie für >find<)
	
			

