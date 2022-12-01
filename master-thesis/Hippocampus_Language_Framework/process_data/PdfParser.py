import fitz
import numpy as np
from tqdm import tqdm

# TODO Vielleicht kann man diesen Wert auch dem Model-Config-Konstruktor geben
# nmb_pages_of_pdf: int = 50  # Outside because value is used in <BaseTextModel.py> to identify saved objects via <pickle>


class PdfParser:
    """
    This class will load or process a spacy-Doc instance giving a text file and the option whether
    the larger spacy-pipeline/model is used for calculating a new object.
    If there is already one, it gets loaded.
    """

    def __init__(self, name_of_textfile, with_vectors, german, nmb_pages=10):
        """
        Processes text extracted from <name_of_textfile> with spacy.
        :param name_of_textfile: name of the file to extract text from. .txt or .pdf possible
        :param with_vectors: if True load large ("lg") spacy-model, elsewise small ("sm") version
        :param german: if True spacy will use a german database
        :param nmb_pages: number of pages to extrat from a .pdf-file; if negative, parse all pages
        """
        print("Reading pdf-file...")
        # source: https://universaldependencies.org/u/pos/
        self.ud_pos_tags = [
            # Open class words
            "ADJ", "ADV", "INTJ", "NOUN", "PROPN", "VERB",
            # Closed class words
            "ADP", "AUX", "CCONJ", "DET", "NUM", "PART", "PRON", "SCONJ",
            # Other
            "PUNCT", "SYM", "X"]
        dataset_dir = "./Hippocampus_Language_Framework/dataset/"
        # Falls Text aus PDF-Datei stammt
        if name_of_textfile.split(".")[-1].casefold() == "pdf".casefold():
            with fitz.open(dataset_dir + name_of_textfile) as pdf_doc:
                text = ""
                for page_nmb, page in tqdm(enumerate(pdf_doc)):
                    # Skip first pages. The are either empty or contain metainfo about
                    # the book, the author and other unnecessary stuff
                    if page_nmb < 7:
                        continue
                    if nmb_pages > 0 and page_nmb == 7 + nmb_pages:
                        break
                    text += page.get_text()
        # Ansonsten, bzw. wenn es eine .txt-Datei ist
        else:
            assert name_of_textfile.split(".")[-1].casefold() == "txt".casefold(), "Can only parse .pdf- or .txt-files"
            with open(dataset_dir + name_of_textfile, "r") as file:
                text = file.read()

        if with_vectors:
            if german:
                import de_core_news_lg
                self.nlp = de_core_news_lg.load()
            else:
                import en_core_web_lg
                self.nlp = en_core_web_lg.load()
        else:
            print("spacy ohne Vektoren")
            import en_core_web_sm
            self.nlp = en_core_web_sm.load()

        self.doc = self.nlp(text)
        self.lemmata = []


if __name__ == "__main__":
    # lt = LoadText("The_little_red_hen_and_the_grain_of_wheat.txt")
    # print(lt.doc[0].vector)
    # print(f"{len(lt.doc[0].vector) = }")  # 96
    # print(f"{len(lt.doc[1].vector) = }")  # 96
    # print(f"{len(lt.doc[2].vector) = }")  # 96
    lt = PdfParser("Daniel_Glattauer_Gut_gegen_Nordwind.pdf", with_vectors=True, german=True)
    your_word = "king"

    # https://spacy.io/api/vectors#most_similar
    # per <strings> kann man den Hash oder das Wort abfragen.
    hashh = lt.nlp.vocab.strings[your_word]
    # Vektor des Wortes extrahieren (eigentlich geht es hierum, dh. man
    # benötigt einen Wort-Vektor, diesen zu finden wird mit den vielen List-Abfragen gemacht)
    v = lt.nlp.vocab.vectors[ hashh ]
    ms = lt.nlp.vocab.vectors.most_similar(
        np.asarray([v]),
        # Die 10 ähnlichsten Resultate zurückgeben
        n=10,
        sort=True)  # Standwardwert ist <True> aber ich finde es gut, sie trotzdem explizit zu verwenden, damit ich weiß, welche Optionen es alles gibt.
    # In ms[0][0] stehen die Hashes der Wörter
    # über die Abfrage <nlp.vocab.strings[w]> erhält man das Wort (Abfrage klappt per String oder Hash, vgl. oben)
    words = [lt.nlp.vocab.strings[w] for w in ms[0][0]]
    distances = ms[2]
    print(words, distances, sep="\n")
