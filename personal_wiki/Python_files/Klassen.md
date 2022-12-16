# Klassen
Angelegt Dienstag 15 März 2022


* Mit ``__getattribute__(self, name: str)`` kann per String auf Attribute zugegriffen werden

	class A:
	    def __init__(self, nmb: int):
	        self.attr = nmb
	
	a = A(4)
	a_nmb = a.__getattribute__("attr")
	print(a_nmb)  # 4
	
	
Das Gegenstück heißt ``__setattr__(self, name, value)``. Mit dieser Methode kann man einer Instanz neue Attribute hinzufügen und einen Wert festlegen.


