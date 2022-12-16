# Bytes
Angelegt Mittwoch 23 Februar 2022


* Byte-Strings sind ``immutable``, man kann sie also nicht in situ verändern. Eine modifizierbare Variante bietet [bytearray – Python-Dokumentation](https://docs.python.org/3/library/stdtypes.html#bytearray-objects).



Byte/Hex zu int
---------------

* Iteriert man über ein ``b"…"``-Objekt erhält man ein Integer, bzw. speichert man sich ein einzelnes Element ab (s. unten), ist es ein Integer

	padded_string: bytes = b"ICE ICE BABY\x04\x04\x04\x04"
	last: int = padded_string[-1]  # last = 4

Möchte man dagegen das ``Byte``–Objekt, kann man mit ``Slices`` arbeiten, dh.
	last_as_byte = padded_string[i:i+1]
.


