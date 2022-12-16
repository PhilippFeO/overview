# Matplotlib
Angelegt Freitag 14 Januar 2022

Farben
------
Vordefinierte Farben findet man [hier.](https://matplotlib.org/stable/gallery/color/named_colors.html)

Bilder anzeigen oder speichern
------------------------------

* Anzeigen: Bilder in eigenem Fenster anzeigen: ``Settings – Tools – Python Scientific – Show plots in tool window`` [[StackOverflow](https://stackoverflow.com/questions/48334853/using-pycharm-i-want-to-show-plot-extra-figure-windows)] @pycharm
* Anzeigen: Um Fehlermeldung ``Matplotlib is currently using agg, which is a non-GUI backend, so cannot show the figure.``, die auftritt, wenn man`` matplotlib`` per ``pip`` in einer virtualenv installiert, zu beheben muss man ein Modul installieren, damit ``matplotlib`` zugriff auf das GUI hat [[StackOverflow](https://stackoverflow.com/questions/56656777/userwarning-matplotlib-is-currently-using-agg-which-is-a-non-gui-backend-so)]:

``sudo apt-get install python3-tk``

* Speichern:

``fig.savefig(PATH)``

Punkte annotieren / mit Text versehen
-------------------------------------
	ax.text(x * (1 + 0.01), y * (1 + 0.01), text[i], fontsize=12, color="red")

@Labels @Scatterplot @Beschriftung

	axs.plot(x, y, "o", c=colors[idx], label=list_wordclasses[idx])


* ``"o" ``(hier: Punkt; auch möglich: +, *, ...)
* ``c ``Farbe für Punkt
* ``label ``Label für das „Objekt“ (hier ein einzelner Punkt, kann aber auch Linie oder ähnliches sein)
	* Die ``label``s werden in der [Legende](https://matplotlib.org/stable/api/_as_gen/matplotlib.pyplot.legend.html) verwendet


Achsenbeschriftungsfarben ändern
--------------------------------
<https://stackoverflow.com/questions/21936014/set-color-for-xticklabels-individually-in-matplotlib>
@Labels

Legende
-------

* [Legend Guide – matplotlib-Doku](https://matplotlib.org/stable/tutorials/intermediate/legend_guide.html)
* Es gibt verschiedene Möglichkeiten eine [Legende](https://matplotlib.org/stable/api/_as_gen/matplotlib.pyplot.legend.html) zu bearbeiten.
* [How to put the legend outside the plot – Stackoverflow](https://stackoverflow.com/questions/4700614/how-to-put-the-legend-outside-the-plot)


