* Größe leerer Nachricht: 131 Bytes

* Null wird durch byte mit 0x70 signalisiert [0]

* Einfluss int: kein Einfluss, selbe Größe bei 0, MIN_VALUE oder MAX_VALUE

* Einfluss 1 Buchstabe: je nach Codierung, Ascii 1 byte, bspw. € 3 byte [1]

* Einfluss Array Größe: Pro Object 2 extra byte + Object Größe [2]

------------

* Leere Message: 
	
59 bytes mit read() und write()

62 bytes mit readInt() und writeInt()
  		  		
leerer String: 64

leeres Object array: 101

* Weitere Minimierung der Datenmenge z.B. durch Kompression








[0]: 
Leere Nachricht (0, null, null): 
ac ed 0 5 73 72 0 20 76 73 75 65 2e 63 6f 6d 6d 75 6e 69 63 61 74 69 6f 6e 2e 56 53 54 65 73 74 4d 65 73 73 61 67 65 5e df f5 6a fb 7d f3 c 2 0 3 49 0 7 69 6e 74 65 67 65 72 5b 0 7 6f 62 6a 65 63 74 73 74 0 13 5b 4c 6a 61 76 61 2f 6c 61 6e 67 2f 4f 62 6a 65 63 74 3b 4c 0 6 73 74 72 69 6e 67 74 0 12 4c 6a 61 76 61 2f 6c 61 6e 67 2f 53 74 72 69 6e 67 3b 78 70 0 0 0 0 70 70 

Leere Nachricht mit leerem String (0, "", null"):
ac ed 0 5 73 72 0 20 76 73 75 65 2e 63 6f 6d 6d 75 6e 69 63 61 74 69 6f 6e 2e 56 53 54 65 73 74 4d 65 73 73 61 67 65 5e df f5 6a fb 7d f3 c 2 0 3 49 0 7 69 6e 74 65 67 65 72 5b 0 7 6f 62 6a 65 63 74 73 74 0 13 5b 4c 6a 61 76 61 2f 6c 61 6e 67 2f 4f 62 6a 65 63 74 3b 4c 0 6 73 74 72 69 6e 67 74 0 12 4c 6a 61 76 61 2f 6c 61 6e 67 2f 53 74 72 69 6e 67 3b 78 70 0 0 0 0 70 74 0 0

[1]:
null: 131, leerer String: 133, 1 buchstabe: 134, 2 Buchstaben: 135, nur Euro Zeichen: 136, nur ä: 135

[2]:
 leere Nachricht: 131, leeres Object Array: 170, Object[] = {null}: 171; Object[] = {""}: 173; Object[] = {"", ""}: 178; Object[] = {2.5}: 253, Object[] = {2.5, ""}: 250

[3]:
Größe von String 3 byte, null 1 byte