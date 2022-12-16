import numpy as np

x = np.arange(12).reshape((3, 4))
a = np.concatenate([x] * 2, axis=0)  # (6, 5)
b = np.concatenate([x] * 2, axis=1)  # (4, 8)

q = np.arange(8).reshape((2, 2, 2))
w = np.arange(8).reshape((2, 2, 2)) * 2
e = np.concatenate([q, w], axis=1)  # # (2, 2, 4)
print(e)

X = np.random.random((32, 10))
y = np.random.random((10,))
y = np.expand_dims(y, axis=0)  # (1, 10)
Y = np.concatenate([y] * 32, axis=0)  # 32x (1, 10) |-> (32, 10)
Y = np.concatenate([y] * 32, axis=1)  # 32x (1, 10) |-> (1, 320)

y = np.arange(15).reshape((3, 5))
# Funktioniert nicht, weil sich x, y auf axis=0 nicht gleichen
# c = np.concatenate([x, y], axis=0)

"""
    axis = 0:
        Schreibe alles, was sich auf der ersten Achse (diese kann jede beliebige Dimension haben) befindet, hintereinander
            Erste Achse, alles, was man mit einer [] erreichen kann
        Aber: Alles muss dieselbe Länge haben, dh. (3,4) & (3,5) kann NICHT konkateniert werden
    axis = 1:
        Schreibe alles, was sich auf der zweiten Achse findet, hintereinander
            zweite Achse: Alles, was man mit [i][] erreichen kann für festes i
        Aber: Alles muss auf axis=0 dieselbe Dimension haben, dh. (3,4) & (4,6) kann NICHT konkateniert werden.
    
    Wenn man zwei Arrays A & B der Form (x1, x2, ..., xn) per axis=k, 0<=k<=n-1, konkateniert, muss sich die Form bis
    Achse k-1, dh von x1 bis xk-1, gleichen. Ruft man im neuen Array dann [i1]...[ik-1] auf, finden sich „hinter
    dieser Türe“ alle Elemente, die sich bei A & B hinter diesen Koordinaten befinden.
"""