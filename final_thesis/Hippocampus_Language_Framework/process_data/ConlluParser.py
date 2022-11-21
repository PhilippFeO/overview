"""
With this class you can parse a .conllu-file [1]. For every UD-POS-tag [2],
there is a corresponding attribute containing the classified word.

[1]: https://universaldependencies.org/format.html
[2]: https://universaldependencies.org/u/pos/
"""
import pyconll


class ConlluParser:
    # s. down below for explanation of abbreviation
    # source: https://universaldependencies.org/u/pos/
    ud_pos_tags = [
        # Open class words
        "ADJ", "ADV", "INTJ", "NOUN", "PROPN", "VERB",
        # Closed class words
        "ADP", "AUX", "CCONJ", "DET", "NUM", "PART", "PRON", "SCONJ",
        # Other
        "PUNCT", "SYM", "X"]

    def __init__(self, nmb_sentences: int = 10):
        """
        Load a coNLL-U-file up to `nmb_sentences` sentences and parses it.
        Automatically generates attributes with __setattr__ according to the <ud_pos_tags>. Finally
        you have an instance with field like <self.ADJ>, <self.AUX>, ... filled with the corresponding
        words from the coNLL-U-file.
        :param nmb_sentences: number of sentences to extract.
        """
        self.nmb_sentences = nmb_sentences
        for UD_pos_tag in ConlluParser.ud_pos_tags:
            self.__setattr__(UD_pos_tag, [])
        # parse coNLL-U file: sort every token in its proper bucket by the token.upos property
        corpus = pyconll.load_from_file("./Hippocampus_Language_Framework/dataset/en_pud-ud-test.conllu")
        sentences = corpus[:nmb_sentences]
        for sentence in sentences:
            for token in sentence:
                # TODO Evtl. zusammenlegen, bspw NOUN und PROPN, VERB & AUX, und SYM, NUM, PUNCT, X, etc. entfernen
                # TODO_ Kann man evtl. mit __getattribute__ kürzen
                for UD_pos_tag in ConlluParser.ud_pos_tags:
                    if token.upos == UD_pos_tag and token.lemma not in self.__getattribute__(UD_pos_tag):
                        self.__getattribute__(UD_pos_tag).append(token.lemma)
                        break
                    """
                    Sieht im Prinzip so aus:
                        if token.upos == "ADJ" and token.lemma not in self.ADJ:
                            self.ADJ.append(token.lemma)
                            break
                    """
