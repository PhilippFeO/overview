# cron
Angelegt Freitag 18 März 2022


* [cron – UbuntuUsers](https://wiki.ubuntuusers.de/Cron/)
* [crontab-Generator – crontab-generator.org (TryHackMe)](https://crontab-generator.org/)
* [crontab-Guru – crontab.guru (TryHackMe)](https://crontab.guru/)


Verwendung
----------
	[sudo] crontab -e

``[sudo]`` für die ``crontab`` von ``sudo``.

GUI
---

* Für die Verwendung eines GUI (bspw. ``zenity``) muss man der ``cron``-Zeile ein

	export DISPLAY=:0

voranstellen:
``*/2 * * * * export DISPLAY=:0 && <BEFEHEL`` (bspw. Pfad zu Skript)``>``

* Für die Verwendung von Benachrichtigungen

	zenity --notification --text="Benachrichtigung"
	notify-send "Benachrichtigung"

muss man
	export DISPLAY=:0
	export DBUS_SESSION_BUS_ADDRESS=unix:path=/run/user/1000/bus

dem Skript voranstellen und kann dann auf die explizite Angabe von
	export DISPLAY=:0

in der ``crontab`` verzichten.

Logs
----
Logs der ``crontab`` finden sich in [/var/log/syslog](file:///var/log/syslog). Filtern per [grep – MeinWiki](./grep.md): ``grep CRON /var/log/syslog``.

