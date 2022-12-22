#pragma once

#include "time_measurement_helper.h"

// === Allgemeine Größen ===
// unsigned BATCH_SIZE = 80;			// Anzahl der Bilder, die einem Durchgang bearbeitet werden sollen (Zeilen der Matrix)
const float learning_rate = 0.01f/BATCH_SIZE;
// 
const int TRAIN_IMAGE_DIM = 28;
const int TRAIN_IMAGE_SIZE = TRAIN_IMAGE_DIM * TRAIN_IMAGE_DIM;	// = 28 x 28; Anzahl der Pixel der Trainingsbilder 

const int MAX_THREADS = 1024;

// === Filter- und Maxpool-Schicht ===
// int NMB_FILTERS = 5;	// Anzahl der zu verwendeten Filter in der Conv-Schicht
const int FILTER_DIM = 3;	// „Dimension“ der Filter, also werden 3x3-Filter verwendet

const int NMB_FILTERED_IMAGES = BATCH_SIZE * NMB_FILTERS;	// Jedes Bild wird mit jedem Filter bearbeitet, also ergeben sich BATCH_SIZE * NMB_FILTERS neue Bilder

const int CONV_OUTPUT_IMG_DIM = TRAIN_IMAGE_DIM - FILTER_DIM + 1;	// Für TRAIN_IMAGE_DIM = 3, FILTER_DIM = 2 ergäbe sich ein Ausgabebilder der Größe (3-2+1)x(3-2+1) = 2x2
const int CONV_OUTPUT_IMG_SIZE = CONV_OUTPUT_IMG_DIM * CONV_OUTPUT_IMG_DIM;	// Anzahl der Pixel der Bilder, die die Conv-Schicht produziert

const int MAXPOOL_OUTPUT_IMG_DIM = 13;	// Maxpooling generiert durch 2x2-Pooling 13x13-Bilder
const int MAXPOOL_OUTPUT_IMG_SIZE = MAXPOOL_OUTPUT_IMG_DIM * MAXPOOL_OUTPUT_IMG_DIM;	// = 13 x 13; Anzahl der Pixel der Trainingsbilder



// === FC-Schicht ===
const int NEURONS_LAYER1 = MAXPOOL_OUTPUT_IMG_SIZE * NMB_FILTERS;
const int NEURONS_LAYER2 = 64;
// TODO In „OUTPUT_LAYER“ umbenennen
const int NEURONS_LAYER3 = 10; // Ausgabe-Schicht (10 Zahlen sollen klassifiziert werden)
