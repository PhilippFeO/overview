import subprocess
import threading
import json
import queue
import time


class ExecuteNetworks(threading.Thread):
    '''
    Diese Thread-Klasse sorgt dafür, dass die Kompilate parallel ausgeführt werden, indem sie speziell konfigurierte Skripte startet.
    Der Prozess funktioniert per Skript, da ich nicht herausgefunden habe, wie man mit python-Systemaufrufen/Prozessaufrufen davor das
    Verzeichnis wechselt, was essentiell ist, da von diesem Skript aus sowohl die GPU- als auch die CPU-Werte gesammelt werden.
    '''
    def __init__(self, executable, queue):
        threading.Thread.__init__(self)
        self.executable = executable
        self.queue = queue
        self.terminate = False

    def run(self):
        while(not self.terminate):
            executable = self.queue.get_nowait()
            subprocess.run(["./executeGPU.sh", self.executable])
            self.queue.task_done()



def compile_configuration(iterations, batch_size, filters, executables):
    def compile_configuration_helper(iterationsH, batch_sizeH, filtersH, executables):
        # Lege die Konfiguration in der .h-Helferdatei fest
        time_measurement_configuration = ("// Diese Datei wurde automatisch generiert, für mehr Informationen s. \"collect_data.py\".\n\n" +
                                          "#pragma once\n" +
                                          "const int ITERATIONS = {};\n" +
                                          "const int BATCH_SIZE = {};\n" +
                                          "const int NMB_FILTERS = {};").format(iterationsH, batch_sizeH, filtersH)
        # Ausführbare Datei muss eindeutig sein, damit sich die "Ausführ-Threads" nicht in die Quere kommen
        # bzw. das Kompilat nicht dauernd überschrieben wird
        executable_with_markerGPU = f"Network_GPU_{iterationsH}-Iter_{batch_sizeH}-Batchsize_{filtersH}-Filter.out"
        executable_with_markerCPU = f"Network_CPU_{iterationsH}-Iter_{batch_sizeH}-Batchsize_{filtersH}-Filter.out"
        # Lege die Konfiguration in eigener .h-Datei fest (per Kommandozeilenargument nicht möglich,
        # da wegen Templates die Werte schon zur Compile-Zeit und nicht erst zur Laufzeit bekannt sein müssen
        files = ["./GPU/time_measurement_helper.h", "./CPU/time_measurement_helper.h"]
        for file in files:
            with open(file, "w") as f:
                f.write(time_measurement_configuration)
            f.close()
        # Kompilieren 
        # Da Konfigurationsdoppelungen auftreten können, muss Liste eindeutig gehalten werden
        if executable_with_markerGPU not in executables:
            print(f"Kompiliere GPU-Version: {iterationsH} Iterationen, Batchsize {batch_sizeH}, {filtersH} Filtern... ")
            subprocess.run(["./compile_helperGPU.sh", executable_with_markerGPU])
            executables += [executable_with_markerGPU]
        if executable_with_markerCPU not in executables:
            print(f"Kompiliere CPU-Version: {iterationsH} Iterationen, Batchsize {batch_sizeH}, {filtersH} Filtern... ")
            subprocess.run(["./compile_helperCPU.sh", executable_with_markerCPU])
            executables += [executable_with_markerCPU]

    if isinstance(iterations, list):
        for nmb_iteration in iterations:
            compile_configuration_helper(nmb_iteration, batch_size, filters, executables)
    if isinstance(batch_size, list):
        for bs in batch_size:
            compile_configuration_helper(iterations, bs, filters, executables)
    if isinstance(filters, list):
        for nmb_filters in filters:
            compile_configuration_helper(iterations, batch_size, nmb_filters, executables)




queueGPU = queue.Queue()
queueCPU = queue.Queue()

def execute_networkGPU():
    while True:
        executable = queueGPU.get()
        subprocess.run(["./executeGPU.sh", executable])
        queueGPU.task_done()

def execute_networkCPU():
    while True:
        executable = queueCPU.get()
        subprocess.run(["./executeCPU.sh", executable])
        queueCPU.task_done()


if __name__ == '__main__':
    # TODO Kann überarbeitet werden, da das explizite Abspeichern der Konfigurationen nicht notwendig ist
    with open("configurations.json", 'r') as c:
        configurations = json.loads(c.read())["configurations"]
    iterations_varying = configurations["iterations_varying"]
    batchsize_varying = configurations["batchsize_varying"]
    filters_varying = configurations["filter_varying"]
    executables = []

    # for config in [iterations_varying, batchsize_varying, filters_varying]:
    #     batchsize = config["batch_size"]
    #     filters = config["filters"]
    #     iterations = config["iterations"]
    #     # Kompilieren muss sequentiell passieren, damit sich die Threads die "[helper].h" nicht gegenseitig umschreiben...
    #     compile_configuration(iterations, batchsize, filters, executables)
    # with open("executables.txt", "a") as f:
    #     for e in executables:
    #         f.write("./" + e + "\n")
 
    # ... Die ausführbaren Dateien können aber parallel gestartet werden
    with open("executables.txt", "r") as f:
        executables = f.readlines()
    MAX_THREADS = len(executables) if len(executables) < 16 else 16
    for i in range(MAX_THREADS):
        if i % 2 == 0:
            # threading.Thread(target=execute_networkGPU, daemon=True).start()
            pass
        else:
            threading.Thread(target=execute_networkCPU, daemon=True).start()
            # pass
    for e in executables:
        # e = "./" + e
        e = e[:-1]
        if "CPU" in e:
            queueCPU.put(e)
            # pass
        if "GPU" in e:
            # queueGPU.put(e)
            pass
    #queueGPU.join()    # Blockiert bis alle Aufgaben in der Queue bearbeitet wurden. Dann kann man die Threads joinen.
    queueCPU.join()
    

    # threads = []
    # for e in executables:
    #     thread = ExecuteNetworks("./" + e)
    #     threads += [thread]
    #     thread.start()
    # for thread in threads:
    #     thread.join()
