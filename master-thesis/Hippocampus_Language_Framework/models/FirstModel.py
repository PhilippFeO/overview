from Hippocampus_Language_Framework.models.BasePredefinedModel import BasePredefinedModel
from Hippocampus_Language_Framework.rules import list_wordclasses
from Hippocampus_Language_Framework.rules import rules
from Hippocampus_Language_Framework.rules import WordClasses as wc


class FirstModel(BasePredefinedModel):
    def __init__(self,
                 model_name="First Model",
                 epochs=10,
                 batch_size=50,
                 nmb_hidden_layers=1,
                 nmb_samples=750,
                 nmb_words_per_class=8,
                 nmb_rules=4):
        super().__init__(model_name,
                         epochs,
                         batch_size,
                         nmb_hidden_layers,
                         nmb_samples=nmb_samples,
                         nmb_words_per_class=nmb_words_per_class)
        self.nmb_rules = nmb_rules
        x = len(rules) if nmb_rules < 0 else nmb_rules
        self.rules = rules[:x]
        self.all_words_from_dataset = []
        self.adjectives = []
        self.verbs = []
        self.nouns = []
        self.personal_pronouns = []
        self.question_words = []
        self.pronouns = []
        self.possessive_pronouns = []
        self.adverbs = []
        self.tick_position_helper = []
        self.configure()
        self.prepare_data()

    @staticmethod
    def load_datasets():
        directory = "./Hippocampus_Language_Framework/dataset/"
        words = [[] for _ in range(len(list_wordclasses))]
        for idx, wordclass in enumerate(list_wordclasses):
            with open(directory + wordclass + ".txt", "r") as file:
                lines = file.read().splitlines()  # readlines() liest auch die Zeilenumbrücke am Ende ein
                # Kommentare (Zeilen die mit „#“ beginnen und IMMER am Anfang stehen müssen) herausfiltern
                i = 0
                while lines[i][0] == "#":
                    i += 1
                # Wörter an der richtigen Stelle in der Liste speichern
                words[idx] = lines[i:]
        return words

    def configure(self):
        self.all_words_from_dataset = FirstModel.load_datasets()

        self.adjectives = BasePredefinedModel.retrieve_subset(self.all_words_from_dataset[wc.adjectives],
                                                              self.nmb_words_per_class)
        self.verbs = BasePredefinedModel.retrieve_subset(self.all_words_from_dataset[wc.verbs], self.nmb_words_per_class)
        self.nouns = BasePredefinedModel.retrieve_subset(self.all_words_from_dataset[wc.nouns], self.nmb_words_per_class)
        self.personal_pronouns = BasePredefinedModel.retrieve_subset(self.all_words_from_dataset[wc.personal_pronouns],
                                                                     self.nmb_words_per_class)
        self.question_words = BasePredefinedModel.retrieve_subset(self.all_words_from_dataset[wc.question_words],
                                                                  self.nmb_words_per_class)
        self.pronouns = self.possessive_pronouns + self.personal_pronouns

        self.words = [self.adjectives, self.verbs, self.nouns, self.personal_pronouns, self.question_words]
        self.cognitive_room = self.adjectives + self.verbs + self.nouns + self.personal_pronouns + self.question_words
        self.input_size = len(self.cognitive_room)

        self.tick_position_helper = ["Adjective" if i == len(self.adjectives)//2 else "" for i in range(len(self.adjectives))] + \
                                    ["Verb" if i == len(self.verbs)//2 else "" for i in range(len(self.verbs))] + \
                                    ["Noun" if i == len(self.nouns)//2 else "" for i in range(len(self.nouns))] + \
                                    ["Pronoun" if i == len(self.personal_pronouns)//2 else "" for i in range(len(self.personal_pronouns))] + \
                                    ["Question" if i == len(self.question_words)//2 else "" for i in range(len(self.question_words))]
        self.axes_labels = ["Adjective", "Verb", "Noun", "Pers. Pr.", "Question"]

        if self.nmb_rules == 8:
            self.possessive_pronouns = BasePredefinedModel.retrieve_subset(
                self.all_words_from_dataset[wc.possessive_pronouns], self.nmb_words_per_class)
            self.adverbs = BasePredefinedModel.retrieve_subset(self.all_words_from_dataset[wc.adverbs],
                                                               self.nmb_words_per_class)

            self.words += [self. adverbs, self.possessive_pronouns]
            self.cognitive_room += self.adverbs + self.possessive_pronouns
            self.input_size = len(self.cognitive_room)

            self.tick_position_helper += ["Adverb" if i == len(self.adverbs)//2 else "" for i in range(len(self.adverbs))] + \
                                         ["Pos. Pronoun" if i == len(self.possessive_pronouns)//2 else "" for i in range(len(self.personal_pronouns))]
            self.axes_labels += ["Adverb", "Pos. Pr."]
