# Zertifikate
Angelegt Sonntag 13 November 2022

Generierung
-----------

1. ``openssl`` aufrufen

	openssl req --newkey rsa:2048 -nodes -keyout shell.key -x509 -days 362 -out ZERTIFIKAT.crt


* Generiert zwei Dateien: ``ZERTIFIKAT.{crt, key}``
* ``rsa:2048`` Verschlüsselungsverfahren und Schlüssellänge
* ``-days 362`` Gültigkeit


2. Fragt anschließend nach Infos über das Zertifikat: Leer lassen oder Quatsch angeben
3. ``ZERTIFIKAT.{crt, key}`` in eine ``.pem``-Datei speichern:

	cat ZERTIFIKAT.crt ZERTIFIKAT.key > ZERTIFIKAT.pem




