# Alles mögliche
Angelegt Dienstag 22 März 2022

Matrix zeilenweise Normieren
----------------------------
	for row_idx in range(matrix.shape[0]):
		matrix[row_idx, :] /= np.sum(matrix[row_idx, :])

### Mit Broadcasting
Beim zeilenweisen Normieren wird Broadcasting verwendet (s. auch :
	a = np.arange(9).reshape((3, 3))
	row_sums = a.sum(axis=1)
	new_matrix = a / row_sums[:, np.newaxis]


Arrays konktenieren
-------------------

* ``np.vstack()``
* ``np.hstack()``
* ``np.concatenate()``
* ``np.tile()``


### np.tile()
Ähnlich zu ``np.concatenate()`` aber man ein Array mehrfach konkatenieren
	a = np.arange(0, 6).reshape(2, 3)
		# array([[0, 1, 2],
	    #	     [3, 4, 5]])
	b = np.tile(a, 3)
		# array([[0, 1, 2, 0, 1, 2, 0, 1, 2],
	    #   	 [3, 4, 5, 3, 4, 5, 3, 4, 5]])

### np.vstack()
Konkateniert ein (oder vllt mehrere verschiedene Arrays) vertikal, also entlang ``æxis=1``. Entspricht sozusagen ``np.tile(a, 3, axis=1)`` aber ``np.tile()`` kennt kein Argument ``axis``.
	a = np.arange(0, 6).reshape(2, 3)
		# array([[0, 1, 2],
	    #	     [3, 4, 5]])
	np.vstack([a] * 3)
		# array([[0, 1, 2],
	    #	     [3, 4, 5],
	    #	     [0, 1, 2],
	    #	     [3, 4, 5],
	    #	     [0, 1, 2],
	    #	     [3, 4, 5]])


Array partitionieren
--------------------
Mit [np.argpartition – numpy-Doku](https://numpy.org/doc/stable/reference/generated/numpy.argpartition.html) kann man ein Array so partitionieren, dass man ein einen Index ``k`` angibt, an dem partitioniert werden soll. "Partitioniert" heißt: Nach der Partitionieren ist das Element an Stelle ``k`` richtig sortiert, dh. alle kleineren (kleiner gleich) stehen vor ihm, alle größeren (größergleich) nach ihm. Die dortige **Reihenfolge** ist aber **zufällig **(dh. sie kann nur bedingt zum sortieren verwendet werden).
``np.argpartition(<ARRAY>, <INDEX DES ELEMENTS AN DEM PARTITIONIERT WERDEN SOLL>, ...)``
	x = array([9, 4, 4, 3, 3, 9, 0, 4, 6, 0])
	ind = [np.argpartition(a, 4)]  # [6, 9, 4, 3, 7, 2, 1, 5, 8, 0]
	x[ind]  # [0, 0, 3, 3, 4, 4, 4, 9, 6, 9]; 9, 6, 9 nicht in richtiger Reihenfolge

Bspw. kann man diese Funktion dazu verwenden die Indizes der ``k`` größten Elemente zu extrahieren ohne das ganze Array zuvor sortieren zu müssen (Laufzeit bestenfalls: ``O(n + log(n))``). Dazu ruft man auf:
	tmp = np.argpartition(x, -k)
	indices = tmp[-k:]  # Die Indizes der k größten Elemente abtrennen
	x[indices] 

Dadurch wird das ``k``-te größte Element an die ``k``-te Stelle gesetzt, davor sind alle kleiner, danach alle größer.

Laufzeit: ``O(n)`` bei ``n`` Elementen im Array 

