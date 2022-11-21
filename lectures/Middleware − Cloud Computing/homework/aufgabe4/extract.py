import re

file = open('stripped.dump', 'r')
lines = file.readlines()


text = 'gfgfdAAA1234ZZZuijjk'

m = re.search('AAA(.+?)ZZZ', text)
if m:
    found = m.group(1)

# found: 1234



id1 = ""
for line in lines:
    if line.startswith("<title>"):
        continue
    elif line.startswith("<input"):
        id1 = re.search("/(.+?)\" autocomplete", line).group(1).split("/")[-1]
    elif line.startswith("<a class"):
        id2 = re.search("/(.+?)\" rel=\"friend\"", line).group(1).split("/")[-1]
        print(id1 + "\t" + id2)
        print(id2 + "\t" + id1)

