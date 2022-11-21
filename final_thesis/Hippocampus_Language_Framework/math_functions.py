import json

import networkx as nx
import numpy as np
import tensorflow as tf
from sklearn.decomposition import PCA
from sklearn.manifold import MDS
from tqdm import tqdm
import time

from Hippocampus_Language_Framework.paths import plots_dir


def load_model_params(filename):
    #Identitfy
    filename_model_js = filename + "/model.json"
    filename_model_h5 = filename + "/weights.h5"
    filename_params = filename + "/params.json"

    # load model's parameters
    with open(filename_params) as params_json:
        params = json.load(params_json)

    # load and create model
    with open(filename_model_js, 'r') as json_file:
        loaded_model_json = json_file.read()
    loaded_model = tf.keras.models.model_from_json(loaded_model_json)

    # load weights into new model
    loaded_model.load_weights(filename_model_h5)
    print("Loaded model from disk: " + str(params))

    # evaluate loaded model on test data
    loaded_model.compile(optimizer='adam',
                         loss='categorical_crossentropy',
                         metrics=['accuracy'])

    return params, loaded_model


def row_dist(matrixA, matrixB, checkpoint_info, full_name):
    checkpoint_info = f"{checkpoint_info}:" if checkpoint_info else "model:"
    with open(f"{plots_dir}/{full_name}/row_dist.txt", "a") as f:
        for row_idx in range(len(matrixA)):
            d = np.linalg.norm(matrixA[row_idx] - matrixB[row_idx]) / np.sqrt(2)
            f.write(f"{checkpoint_info} - {row_idx}: {d}\n")


def normalize_rows(matrix):
    # m_matrix_norm = m_matrix / np.linalg.norm(m_matrix)
    for row_idx in range(matrix.shape[0]):
        row_sum = np.sum(matrix[row_idx, :])
        matrix[row_idx, :] /= row_sum if row_sum != 0 else 1  # Because row_sum == 0 and all entries >= 0,
        # we can devide by whatever we want to achive normalized row


def calc_dist(matrixA, matrixB, title, checkpoint_info, full_name):
    d = np.linalg.norm(matrixA - matrixB) / (np.sqrt(2 * len(matrixA)))
    checkpoint_info = f"{checkpoint_info}:" if checkpoint_info else "model:"
    with open(f"{plots_dir}/{full_name}/Distances.txt", "a") as f:
        f.write(f"{checkpoint_info} {d} (Distance between <Ground Truth> and <{title}>)\n")


def create_SR(prob_matrix, discount_factor: float = 1., sequence_length: int = 1):
    """
    Calculates the Successor Representation (up to a time step k)
        SR_k := \sum_{t=0}^k {\gamma^t T^t}
        SR := \sum_{t=0}^\infty {\gamma^t T^t} = (I_n - \gamma T)^{-1}
    where
        k is in N
        \gamma in (0, 1]
        T is n×n-probability/transition matrix
        I_n is the n×n-identity matrix.

    :param prob_matrix: n×n-Matrix, T in formula above
    :param discount_factor: value from (0, 1], \gamma in formula above
    :param sequence_length: steps to calculate, k in formula above
    :return: n×n-Matrix
    """
    # Berechnung per Summe, da man Zeitschritte hat
    if sequence_length != -1:
        m_matrix = np.zeros(prob_matrix.shape)
        for i in tqdm(range(1, sequence_length+1)):
            # TODO Umbauen, sodass mit tensorflow gerechnet wird, ggfl Zeit messen, um Unterschiede bestimmen zu können.
            start_time = time.time()
            m_matrix += np.power(discount_factor, i) * np.linalg.matrix_power(prob_matrix, i)
            end_time = time.time()
            # print(f"Zeit für {len(prob_matrix)}×{len(prob_matrix)}: {end_time - start_time} s")
    # Berechnung per Formel für geometrische Reihe
    else:
        diag = np.zeros(prob_matrix.shape)
        np.fill_diagonal(diag, 1)
        m_matrix = np.linalg.inv(diag - discount_factor * prob_matrix)
                                                           # we can devide by whatever we want to achive normalized row
    normalize_rows(m_matrix)
    return m_matrix


def create_mds_matrix(SR, number_components=2):
    # dis = create_dissimilarity_matrix(SL)
    """
    inspired by
    https://scikit-learn.org/stable/modules/generated/sklearn.manifold.MDS.html
    """
    # print("creating mds-matrix with" + str(number_components) + " components...", end="")
    mds = MDS(n_components=number_components, n_init=20, max_iter=10000, eps=1e-5)
    components = mds.fit_transform(SR.astype(np.float64))
    # print("done.")
    return components


def create_pca(SR, number_components=2):
    """
    <3 Inspired by:
    https://stats.stackexchange.com/questions/229092/how-to-reverse-pca-and-reconstruct-original-variables-from-several-principal-com

    and by

    https://plotly.com/python/pca-visualization/
    """

    pca = PCA(n_components=number_components)
    return pca.fit_transform(SR)


def show_state_prob_graph(transition_probability_matrix, plot):
    #------------------------------------------------------------------------------------------
    # for other styles: check https://networkx.org/documentation/stable/reference/drawing.html

    # maybe make nodes with node2vec
    # found in "Madjiheurem&Toni//State2vec: Off-Policy Successor Features Approximators"
    #------------------------------------------------------------------------------------------
    #!!!!!Still very specialized to the language Model -- Needs adapation!!!!!
    #------------------------------------------------------------------------------------------

    #Graph, Plot, Edges and weights init
    #figure = plt.figure(num="State Probability Graph")
    weights = []
    graph = nx.Graph()
    nodes_no = transition_probability_matrix.shape[0]

    adjectives = ['big', 'small', 'close', 'fast', 'slow', 'brave', 'difficult', 'good', 'bad', 'real']
    verbs = ['go', 'eat', 'walk', 'lift', 'drive', 'come', 'develope', 'think', 'hear', 'order']
    nouns = ['house', 'car', 'tree', 'rain', 'river', 'lamp', 'tent', 'dinner', 'night', 'day']
    pronouns = ['I', 'you', 'they', 'we']
    questions = ['do', 'can', 'why do', 'what do', 'will', 'must']

    word_groups = []
    word_groups.append(adjectives)
    word_groups.append(verbs)
    word_groups.append(nouns)
    word_groups.append(pronouns)
    word_groups.append(questions)

    word_label_coulors = [
                             'blue', 'blue', 'blue', 'blue', 'blue', 'blue', 'blue', 'blue', 'blue', 'blue',
                              'red', 'red', 'red', 'red', 'red', 'red', 'red', 'red', 'red', 'red',
                                'green', 'green', 'green', 'green', 'green', 'green', 'green', 'green', 'green', 'green',
                              'orange', 'orange', 'orange', 'orange',
                              'yellow', 'yellow', 'yellow', 'yellow', 'yellow', 'yellow']
    labels_l = word_groups
    labels = {}
    i = 0
    for l in labels_l:
        labels[i] = l
        i += 1
    for i in range(nodes_no):
        graph.add_node(i, color = word_label_coulors[i])
        #nodes.append(i)
        for j in range(nodes_no):
            if transition_probability_matrix[i,j] > 0:#(and i < j )
                #edges.append((i, j))
                graph.add_edge(i,j, weights= transition_probability_matrix[i,j])
                weights.append(transition_probability_matrix[i,j])

    # Directed Graph which allows multiply edgeds between nodes

    #graph.add_nodes_from(nodes)
    #graph.add_edges_from(edges)
    my_pos = nx.spring_layout(graph, seed=100, iterations=1000)#k=1./np.sqrt(np.max(visual.params.room_shape)))
    #my_pos = nx.kamada_kawai_layout(graph)

    # Plot erstellen und Graph anzeigen
    #figure_graph = plt.figure('State Probability Graph')
    node_color_dict = (graph.nodes(data='color'))
    node_color_dict = np.array(node_color_dict)[:,1]
    nx.draw(graph, pos=my_pos, node_size=50, width = weights, node_color = node_color_dict, ax=plot)#, with_labels=True)
    nx.draw_networkx_labels(graph, my_pos, labels, font_size=16, ax=plot)
    #figure.show()
    #plt.show()
    return graph
