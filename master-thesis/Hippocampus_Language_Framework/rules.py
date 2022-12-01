from enum import IntEnum

list_wordclasses = ("adjectives", "verbs", "nouns", "personal_pronouns", "question_words", "adverbs", "possessive_pronouns")


class WordClasses(IntEnum):
    adjectives = 0
    verbs = 1
    nouns = 2
    personal_pronouns = 3
    question_words = 4
    adverbs = 5
    possessive_pronouns = 6


class Rule:
    def __init__(self, first, second):
        self.first = first
        self.second = second
    
    def __str__(self):
        return f"{self.first} -> {self.second}"
    
    def __eq__(self, other):
        return self.first == other.first and self.second == other.second
    
    def __getitem__(self, item):
        """
        Gibt den Index der Wortart zurück
        """
        assert 0 <= item <= 1, "Only indices 0 and 1 available"
        if item == 0:
            return self.first
        if item == 1:
            return self.second


class PredefinedRule(Rule):
    def __init__(self, first_wordclass, second_wordclass, lw=list_wordclasses):
        super().__init__(lw.index(first_wordclass), lw.index(second_wordclass))
        self.first_wordclass = first_wordclass
        self.second_wordclass = second_wordclass

    def __str__(self):
        return f"{self.first_wordclass} -> {self.second_wordclass}"


# Wird nicht in „if __name__ == "__main__"“ geschachtelt, da es bei einem Import ausgeführt werden soll
rules = [
    PredefinedRule("question_words", "personal_pronouns"),
    PredefinedRule("adjectives", "nouns"),
    PredefinedRule("personal_pronouns", "verbs"),
    PredefinedRule("verbs", "adjectives"),

    PredefinedRule("question_words", "verbs"),
    PredefinedRule("nouns", "personal_pronouns"),
    PredefinedRule("adverbs", "possessive_pronouns"),
    PredefinedRule("personal_pronouns", "adverbs")]
