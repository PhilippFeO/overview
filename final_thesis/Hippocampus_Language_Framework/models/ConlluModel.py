"""
    In this file I work a lot with
        __setattr__(self, name, value)
    and
        __getattribute__(self, name)
    because I use predefined names/tags of the Universal Dependencies convention [1].
    Typing all by hand is extremely tedious and hampers readability. My first approach
    with using code generation worked finde but long repeating passages reduce
    readability. By the time I discovered the __setattr__- and __getattribute__-mechanics
    and started using it out of my intuition. I can't exclude having the methods implemented
    outside their meaning/context. But it works and the loops look neat.

    [1]: https://universaldependencies.org/u/pos/

    Further Readings:
        Universal Dependencies
          - https://universaldependencies.org/
          - https://universaldependencies.org/format.html

        pyconll
          - Github: https://github.com/pyconll/pyconll
          - Doku: https://pyconll.readthedocs.io/en/stable/
"""


import pyconll

from Hippocampus_Language_Framework.models.BasePredefinedModel import BasePredefinedModel
from Hippocampus_Language_Framework.models.FirstModel import FirstModel
from Hippocampus_Language_Framework.process_data.ConlluParser import ConlluParser


"""
    Dieses Modell leitet sich sozusagen selbst die Regeln her.
    Die .conllu-Datei ist mit UD-Pos-Tags versehen.
    Modell liest Datei ein (Paket pyconllu) und geht dann Wortpaar für Wortpaar durch.
    Die UD-Pos-Tags der beiden werden als Regel interpretiert und gespeichert.
    Jede Regel wird genau einmal gespeichert.
    Die beiden Wörter werden in ihre jeweilige Datenstruktur aufgenommen, bspw ein
    Adjektiv in <self.ADJ>. Diese Datenstrukturen werden automatisch angelegt bzw. Attribute
    werden automatisch angelegt.
    
    Letztendlich wird der Regel- & Datensatz zusammengebaut mit dem dann auch trainiert wird.
"""


class Rule_ConlluModel:
    def __init__(self, token_1, token_2):
        # self.word_1 = token_1.lemma
        # self.word_2 = token_2.lemma
        self.UD_pos_1 = token_1.upos
        self.UD_pos_2 = token_2.upos

    def __str__(self):
        return f"{self.UD_pos_1} -> {self.UD_pos_2}"

    def __repr__(self):
        return self.__str__() + " [__repr__]"

    def __eq__(self, other):
        return self.UD_pos_1 == other.UD_pos_1 and self.UD_pos_2 == other.UD_pos_2

    def __getitem__(self, item: int) -> str:
        assert 0 <= item <= 1
        if item == 0:
            return self.UD_pos_1
        if item == 1:
            return self.UD_pos_2


class ConlluModel(BasePredefinedModel):
    """
    This class works based on a parsed .conllu-file by the class <coNLLU>.
    Furthermore it will derive rules automatically according to the .coNLLU-file.

    """
    def __init__(self, model_name="ConlluModel",
                 nmb_samples=2000,
                 epochs=20,
                 batch_size=100,
                 nmb_hidden_layers=1,
                 nmb_words_per_class=10):
        super().__init__(model_name,
                         epochs,
                         batch_size,
                         nmb_hidden_layers,
                         nmb_samples=nmb_samples,
                         nmb_words_per_class=nmb_words_per_class)
        # Transfer coNLLU-attributes into this class
        #   There has to be a simpler solution. Multiple inheritance didnt work
        #   because I wasnt able to call the constructor of <coNLLU>
        self.coNLLU_file = ConlluParser()
        for UD_pos_tag in ConlluParser.ud_pos_tags:
            name = UD_pos_tag
            value = FirstModel.retrieve_subset(
                self.coNLLU_file.__getattribute__(UD_pos_tag),
                self.nmb_words_per_class)
            self.__setattr__(name, value)
            """
            Sieht im Prinzip so aus:
                self.VERB = MainModel.retrieve_subset(
                    self.coNLLU_file.VERB,
                    self.nmb_words_per_class)
            """
        self.rules = self.build_rules()
        self.configure()
        self.prepare_data()

    def extract_wordclasses(self, rule: Rule_ConlluModel):
        UD_pos_1_words = self.__getattribute__(rule[0])
        UD_pos_2_words = self.__getattribute__(rule[1])
        return UD_pos_1_words, UD_pos_2_words

    # TODO Es wird 2x <pyconll.load_from_file("./dataset/en_pud-ud-test.conllu")> aufgerufen.
    #   Einmal hier und einmal im Konstruktor von <coNLLU>. Das kann man sicherlicher
    #   anders strukturieren, sodass nur noch ein Aufruf gemacht wird.
    def build_rules(self):
        # data structure for bookkeeping: Generate every Rule once.
        rules = [] # rules_as_tuples = []  # TODO_ Kann evtl. durch <Rule_coNLL.__eq__> ersetzt werden
        # if <token> or <token_nbor> is in <skip_ud_pos_tags> then no rule will be built.
        # Admissible rule contain only "real word classes", s. [1].
        # [1]: https://universaldependencies.org/u/pos/
        skip_ud_pos_tags = ["PUNCT", "SYM", "X"]

        # Load .conllu-file
        corpus = pyconll.load_from_file("./Hippocampus_Language_Framework/dataset/en_pud-ud-test.conllu")
        sentences = corpus[:10]
        # Iterate over all sentences in file
        for sentence in sentences:
            # iterate over all tokens; a token bears all the information of a single "word"
            for i, token in enumerate(sentence):
                # Skip rule if token is the last token (no successor)
                if token == sentence[-1]:
                    continue
                token_nbor = sentence[i+1]
                # SKip rule if one of the elected tokens is part of the skip-list
                if token.upos in skip_ud_pos_tags or token_nbor.upos in skip_ud_pos_tags:
                    continue

                # Build rules #
                # Check if rule was already collected, if so => continue
                rule = Rule_ConlluModel(token, token_nbor)
                if rule in rules:
                    continue
                rules.append(rule)
        return rules

    def configure(self):
        for UD_pos_tag in ConlluParser.ud_pos_tags:
            # Mit __getattribute__ kann man sich die Code-Generierung sparen
            self.cognitive_room += self.__getattribute__(UD_pos_tag)  # = self.ADJ + self.ADV + ...
            self.words.append(self.__getattribute__(UD_pos_tag))  # = [self.ADJ, self.ADV, ...]
            self.axes_labels += [f"{word} {UD_pos_tag}" for word in self.__getattribute__(UD_pos_tag)]  # "f[f"{word} {NOUN} for word in self.NOUN]"
        self.input_size = len(self.cognitive_room)
