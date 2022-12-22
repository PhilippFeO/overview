import matplotlib.pyplot as plt
import sys
import subprocess
import json
import threading

# This is a sample Python script.

# Press Umschalt+F10 to execute it or replace it with your code.
# Press Double Shift to search everywhere for classes, files, tool windows, actions, and settings.

def configure_plot(x, y_GPU, y_CPU, ax, xlabel, title):
    ax.set_ylabel("time [s]")
    ax.set_xlabel(xlabel)
    ax.plot(x, y_GPU, "o", label='GPU')
    ax.plot(x, y_CPU, "o", label='CPU')
    y_spacing_for_label = 0.25
    fractions = [cpu / gpu for gpu, cpu in zip(y_GPU, y_CPU)]
    labelsGPU = [f"{fraction:.0f}x faster\n{y:.2f}s" for fraction, y in zip(fractions, y_GPU)]
    for label, xx, yy in zip(labelsGPU, x, y_GPU):
        ax.annotate(label, xy=(xx, yy+y_spacing_for_label))
    labelsCPU = [f"{y:.2f}s" for y in y_CPU]
    for label, xx, yy in zip(labelsCPU, x, y_CPU):
        ax.annotate(label, xy=(xx, yy+y_spacing_for_label))
    avg_fraction = sum(fractions) / len(fractions)
    ax.set_title(title + f'\nOn avg. {avg_fraction:.0f}x faster')
    ax.legend(loc='upper left')  # fontsize='x-large'



def helper(iterations, batch_size, filters):
    '''
    Je nachdem, welche der drei Datenkonstruktionen eine Liste ist, wird die entsprechende Verarbeitung zur Berechnung
    der gesammelten Werte angestoßen. Diese Funktion verteilt sozusagen nur den Datenfluss.
    Berechnet schließlich die Durchschnittszeiten für die jeweilige Konstellation (also, welche Größe variiert bzw. welche konstant sind,
    s. "constellations.json").
    :param iterations: Anzahl der Durchläufe, die das Netzwerk macht (möglicherweise eine Liste)
    :param batch_size: Anzahl der Bilder, die pro Durchlauf bearbeitet werden (möglicherweise eine Liste)
    :param filters: Anzahl der Filter für die Faltungs-Stufe (möglicherweise eine Liste)
    :return: Zwei Listen, je mit GPU- und CPU-Zeit, die die Durchschnittszeiten für die jeweilige Konstellation enthalten.
    '''
    def calc_times(GPU_file, CPU_file=None):
        '''
        Berechnet die Durchschnittszeiten für eine Kostellation. Es werden mehrere Durchläufe berechnet,
        um Ausreißer in den Messwerten abzufedern
        :param GPU_file: Dateiname für eine GPU-Konstellation
        :param CPU_file: Dateiname für eine GPU-Konstellation
        :return: Durchschnittszeiten für die GPU-Version und ihr CPU-äquivalent
        '''
        times_needed = []
        # TODO Zweites "GPU_file" zu "CPU_file" ändern. Hier nur als Platzhalte für die CPU-Version, damit man die
        # Implementierung mit der GPU-Variante testen kann
        for file in [GPU_file, CPU_file]:
            # Alle Zeilen werden als Liste gespeichert
            with open(file) as f:
                lines = f.readlines()
            # Umwandlung in Integer und Aufsummieren der Werte für Gesamtzeit der Durchläufe
            time_needed = sum([int(line) for line in lines])
            # Durchschnittswert berechnen
            time_needed /= len(lines)
            # von Nanosekunden in Sekunden umrechnen
            time_needed /= 10 ** 9
            times_needed.append(time_needed)
        if CPU_file is None:
            times_needed[1] = 0
        return times_needed

    def construct_file_names(iterations, batch_size, filters):
        '''
        Konstruiert die Dateinamen für die GPU- und CPU-Version der gestoppten Zeiten für die jeweilige Netzwerkkonfiguration
        :param iterations: Anzahl der Durchläufe, die das Netzwerk macht
        :param batch_size: Anzahl der Bilder, die pro Durchlauf bearbeitet werden
        :param filters: Anzahl der Filter für die Faltungs-Stufe
        :return: Dateinamen
        '''
        GPU_file = f"./time_measurements/GPU_Par_{iterations}-Iter_{batch_size}-Batchsize_{filters}-Filters.txt"
        CPU_file = f"./time_measurements/CPU_Seq_{iterations}-Iter_{batch_size}-Batchsize_{filters}-Filters.txt"
        return GPU_file, CPU_file

    GPU_time, CPU_time = [], []
    if isinstance(iterations, list):
        for nmb_iteration in iterations:
            GPU_file, CPU_file = construct_file_names(nmb_iteration, batch_size, filters)
            tmp = calc_times(GPU_file, CPU_file)
            GPU_time += [tmp[0]]
            CPU_time += [tmp[1]]
    if isinstance(batch_size, list):
        for bs in batch_size:
            GPU_file, CPU_file = construct_file_names(iterations, bs, filters)
            tmp = calc_times(GPU_file, CPU_file)
            GPU_time += [tmp[0]]
            CPU_time += [tmp[1]]
    if isinstance(filters, list):
        for nmb_filters in filters:
            GPU_file, CPU_file = construct_file_names(iterations, batch_size, nmb_filters)
            tmp = calc_times(GPU_file, CPU_file)
            GPU_time += [tmp[0]]
            CPU_time += [tmp[1]]
    return GPU_time, CPU_time


# Press the green button in the gutter to run the script.
# See PyCharm help at https://www.jetbrains.com/help/pycharm/
if __name__ == '__main__':
    with open("configurations.json", 'r') as c:
        configurations = json.loads(c.read())["configurations"]
    iterations_varying = configurations["iterations_varying"]   # für ax1
    batchsize_varying = configurations["batchsize_varying"]     # für ax2
    filters_varying = configurations["filter_varying"]          # für ax3

    fig, (ax1, ax2, ax3) = plt.subplots(1, 3, figsize=(15, 6))
    fig.suptitle("Comparison of a CNN on GPU and CPU with different parameters")

    # ax1: Iterationen variabel, Batchsize & Filter konstant
    batch_size = iterations_varying["batch_size"]
    filters = iterations_varying["filters"]
    iterations = iterations_varying["iterations"]
    GPU_time, CPU_time = helper(iterations, batch_size, filters)
    configure_plot(x=iterations,
                   y_GPU=GPU_time,
                   y_CPU=CPU_time,
                   ax=ax1,
                   xlabel='n_iterations',
                   title=f'Batchsize = {batch_size}, Filter = {filters}')

    # ax2: Batchsize variabel, Filter & Iterationen konstant
    batch_size = batchsize_varying["batch_size"]
    filters = batchsize_varying["filters"]
    iterations = batchsize_varying["iterations"]
    GPU_time, CPU_time = helper(iterations, batch_size, filters)
    configure_plot(x=batch_size,
                   y_GPU=GPU_time,
                   y_CPU=CPU_time,
                   ax=ax2,
                   xlabel='Batchsize',
                   title=f'Filter = {filters}, Iteration = {iterations}')

    # ax3: Filter variabel, Iterationen & Batchsize konstant
    batch_size = filters_varying["batch_size"]
    filters = filters_varying["filters"]
    iterations = filters_varying["iterations"]
    GPU_time, CPU_time = helper(iterations, batch_size, filters)
    configure_plot(x=filters,
                   y_GPU=GPU_time,
                   y_CPU=CPU_time,
                   ax=ax3,
                   xlabel='n_filter',
                   title=f'Iteration = {iterations}, Batchsize = {batch_size}')

    fig.tight_layout()
    plt.savefig("plots.png")
