import numpy as np

from Hippocampus_Language_Framework.models.W2V_W2V import W2V_W2V


class MostFrequentWords(W2V_W2V):
    def __init__(self,
                 model_name="MostFrequentWords",
                 epochs=20,
                 batch_size=100,
                 nmb_hidden_layers=1,
                 nmb_concatenations=1,
                 nmb_of_retrieved_words=10,
                 period=0,
                 pages=5,
                 book_name=0,
                 nmb_tokens=0):
        super().__init__(model_name=model_name,
                         epochs=epochs,
                         batch_size=batch_size,
                         nmb_hidden_layers=nmb_hidden_layers,
                         nmb_concatenations=nmb_concatenations,
                         nmb_of_retrieved_words=nmb_of_retrieved_words,
                         period=period,
                         pages=pages,
                         book_name=book_name,
                         nmb_tokens=nmb_tokens)
        self.nmb_most_frequent_words = 40
        self.most_frequent_words = []
        self.find_most_frequent_words()

    def find_most_frequent_words(self):
        for key_pair in self.ground_truth_dict:
            # count how often word W (key_pair[0]) appears as predecessor by counting all successors
            freq = sum(self.ground_truth_dict[key_pair][0].values())
            # save pair
            self.most_frequent_words.append((key_pair[0], freq, key_pair[1]))
        # sort list according to <freq>/index 1 of the tuple entries
        self.most_frequent_words = sorted(self.most_frequent_words,
                                          key=lambda p: p[1],
                                          reverse=True)[:self.nmb_most_frequent_words]

    def do_prediction(self, **kwargs):
        shape = (self.nmb_most_frequent_words, len(self.cognitive_room))
        inputs = np.zeros(shape)
        for row, (mfw, _, _) in zip(inputs, self.most_frequent_words):  # mfw = Most Frequent Word
            idx = self.cognitive_room.index(mfw)
            row[idx] = 1
        transition_probability_matrix = super().do_prediction(**kwargs)
        idx_of_mfw = [self.cognitive_room.index(word) for word, _, _ in self.most_frequent_words]
        return transition_probability_matrix[idx_of_mfw]
