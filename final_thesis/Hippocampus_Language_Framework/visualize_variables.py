"""
Different variables for controlling the visualization process in <visualize.py>.
Extra file to have a faster access and overview.
"""

# Schalter, für die zu erstellenden Plots
plot_ground_truth: bool = True  # Tritt immer mit <isinstance(model_config, BaseTextModel)>,
# weil nur für diese Modelle eine Ground Truth-Verteilung berechnet wird
plot_TPM: bool = True
plot_SR_matrix: bool = True
mds: bool = True
pca: bool = False
graph: bool = False

# Bedingung, ob Bild am Ende angezeigt und/oder gespeichert werden soll
show_image: bool = False
save_image: bool = True

# Dimensions of the plots
fs_length: float = 7.5
fs_height_matrix: float = 7.5
fs_height_cluster: float = 8.5

pca_or_mds: str = "MDS"  # <PCA> or <MDS>

# m_matrix/Successor Representation
# TODO_ Mehr Farben hinzufügen, da die coNLL-U-Implementierungen viele Wortarten nutzen
# Weitere Farben unter https://matplotlib.org/stable/gallery/color/named_colors.html
colors_for_wordclasses = ['b', 'magenta', 'green',
                          'r', 'tab:cyan', 'orange',
                          'lime', 'k', 'slategray',
                          'olive']
marker_size = 8**2
fontsize_annotation = 16

ddff = list((d/10 for d in range(5, 10, 10)))
tt = list(range(2, 3, 1))
