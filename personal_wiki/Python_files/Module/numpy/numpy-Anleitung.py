#!/usr/bin/env python
# coding: utf-8

# In[2]:


import numpy as np


# # Struktur-verwandte Dinge

# ## ``axis``-Attribut bei Funktionsaufrufen
# * s. ``zimwiki/MeinWiki → Python → Module → numpy`` für weitere Informationen
# * Die Anzahl der ``axes`` (Plural!) kann per ``ndim``-Attribut abgefragt werden und entspricht der Länge des ``shape``-Attributs (``len(array.shape)``). Eine ``axis`` (Achse) eines Arrays entspricht der Stufe bei einem Tensor, dh. eine Achse = Vektor, zwei Achsen = Matrix, usw.
# * Eine Achse kann für sich dann eine beliebige, von den anderen Achsen unterschiedliche **Dimension** aufweisen.
# * Ein ``m``-dimensionales ``numpy``-Array ist also einfach ein Tensor ``m``-ter Stufe.

# In[13]:


a = np.arange(4).reshape((2,2))
b = np.arange(4, 8).reshape((2,2))
a.ndim == len(a.shape)


# ``axis=0``: „Entlang der ersten Achse“, dh. der ``0``-te Index wird entlang gelaufen

# In[11]:


c = np.concatenate([a, b], axis=0)
c


# ``axis=1``: „Entlang der zweiten Achse“, dh. der ``1``-te Index wird entlang gelaufen

# In[5]:


d = np.concatenate([a, b], axis=1)
d


# # Diverses

# ## Mehrfachzugriffe
# Listen oder andere ``numpy``-Arrays können als Argument in ``array[...]`` verwendet werden, um auf mehrere Elemente gleichzeitig zugreifen zu können:

# In[20]:


a = np.arange(5) * 2
a


# In[30]:


l = [2, 0, 3]
a[l]


# In[29]:


indices = np.array(l)
a[indices]


# ## Arrays speichern & laden

# In[18]:


array = np.arange(5)
array


# In[16]:


# np.save(<PFAD>, <ARRAY>)
np.save("./numpy_array.npy", array)


# In[17]:


array = np.load("./numpy_array.npy")
array


# ## ``numpy``-Arrays printen
# (Demonstrationen funktioniert im ``Jupyter Notebook`` leider nicht.)

# ### Ohne Auslassungen (``...``)

# In[14]:


np.set_printoptions(threshold=np.inf)


# ### Ohne Umbrüche

# In[ ]:


m = array.tolist()
print(*m, sep="\n")


# ## Kleinste & größte betragsmäßige Zahl
# Die Kleinste kann bspw. dann verwendet werden, wenn man sicher gehen möchte, dass nicht durch ``0`` geteilt wird.

# In[4]:


np.finfo(float).eps


# In[5]:


np.finfo(float).max

