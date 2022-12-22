#pragma once

#include <array>
#include <random>
#include<string>
#include<iostream>

#include "../Network_Config.h"
#include "../Hilfsdateien/CudaHilfsfunktionen.h"
#include "../Hilfsdateien/MatrixOperations.h"

#include "cuda_runtime.h"
#include "device_launch_parameters.h"

using std::array;
using std::string;
using std::cout;
using std::endl;


void forwardWrapper_Fully(float* layer_input, float* W, float* output, const int NEURONS_IN, const int NEURONS_OUT);

void backwardWrapper_Fully(float* error_tensor,
	float* W,
	float* d_input,
	float* layer_input,
	const int NEURONS_IN,
	const int NEURONS_OUT);

/*
	Für ERSTE FC-Schicht:
	neurons_in = image_dim * image_dim * #filter

	Die gefilterten Bilder werden also alle eine Zeile geschrieben

	Das muss deswegen so angegeben, da dann die Zeilenanzahl wieder BATCH_SIZE lautet und nicht NMB_FILTERED_ELEMENTS.
	Würde die FC-Schicht NMB_FILTERED_IMAGES Zeilen zulassen, gäbe es am Ende in der Loss-Schicht Probleme, da dort auch „b_y = labels“
	eingesetzt wird und die hat BATCH_SIZE x OUTPUT_LAYER viele Elemente (nicht NMB_FILTERED_ELEMENTS x OUTPUT_LAYER viele)
*/
template<unsigned neurons_in, unsigned neurons_out>
class Fully {
public:
	float* W;
	float* W_tmp;
	
	/* 	Für Gradientenabstieg in backward benötigt man die Eingabe der Schicht, da
			d_W[ X * W ] = X.T * _
	*/
	float* layer_input;
	const int nmbBytes_layer_input = BATCH_SIZE * neurons_in * sizeof(float);

	Fully();
	Fully(float *);

	~Fully();
	
	void forward(float*, float*);

	void backward(float*, float*);

	float* getW();
};

template<unsigned neurons_in, unsigned neurons_out>
Fully<neurons_in, neurons_out>::~Fully(){
	cudaFree(W);
}

template<unsigned neurons_in, unsigned neurons_out>
Fully<neurons_in, neurons_out>::Fully() {
	// array<float, neurons_in * neurons_out> tmp;
	W_tmp = (float *) malloc(neurons_in * neurons_out * sizeof(float));
	std::normal_distribution<float> distribution(0, sqrt(2.0 / (neurons_in + neurons_out)));
	static std::default_random_engine generator;
	for (unsigned i = 0; i < neurons_in * neurons_out; ++i) {
		W_tmp[i] = distribution(generator);
		// W_tmp[i] = i+1; // Zum Testen
	}
	// Speicher für Gewichte auf GPU reservieren
	const int nmbElementsForGPU = neurons_in * neurons_out * sizeof(float);
	cudaError_t cudaStatus;
	cudaStatus = cudaMalloc((void **) &W, nmbElementsForGPU);
	cudaHelper(cudaStatus, true, "W");
	// Gewichte auf GPU kopieren
	cudaStatus = cudaMemcpy(W, W_tmp, nmbElementsForGPU, cudaMemcpyHostToDevice);
	cudaHelper(cudaStatus, false, "W");
}

// Konstruktor zum testen
template<unsigned neurons_in, unsigned neurons_out>
Fully<neurons_in, neurons_out>::Fully(float *W_init) {
	// Speicher für Gewichte auf GPU reservieren
	const int nmbElementsForGPU = neurons_in * neurons_out * sizeof(float);
	cudaError_t cudaStatus;
	cudaStatus = cudaMalloc((void **) &W, nmbElementsForGPU);
	cudaHelper(cudaStatus, true, "W");
	// Gewichte auf GPU kopieren
	cudaStatus = cudaMemcpy(W, W_init, nmbElementsForGPU, cudaMemcpyHostToDevice);
	cudaHelper(cudaStatus, false, "W");
}

/*
	Fully Connected-Schicht f�r Vorw�rtspropagation.
	Bildet Eingabe X der Schicht i per Gewichtsmatrix W_ij auf Schicht i+1 ab.

	Matrixprodukt der Matrizen X und W_ij.

	Template-Parameter:
	BATCH_SIZEOfX: 		Anzahl der Zeilen von X == Anzahl der Bilder, die in einem Durchgang bearbeitet werden sollen
	columnsOfX: 	Anzahl der Spalten von X == Anzahl der Zeilen von B == Anzahl der Neuronen von Schicht i
	columnsOfW_ij: 	Anzahl der Spalten von W_ij == Anzahl der Neuronen von Schicht i+1

	Funktionsparameter
	X:			Eingabe f�r die Schicht
	W_ij:			Gewichte der Schicht

	R�ckgabe:
	Matrixprodukt von X und W_ij.
*/
template<unsigned neurons_in, unsigned neurons_out>
void Fully<neurons_in, neurons_out>::forward(float* input, float* output) {
	// Eingabe speichern für „backward()“
	layer_input = input;
	
	forwardWrapper_Fully(input, W, output, neurons_in, neurons_out);
}


/*
	Fully Connected-Schicht f�r R�ckw�rtspropagation.

		d_W [ X * W ] = X.T * _
		d_X [ X * W ] = _ * W.T

	Matrixprodukt der Matrizen E_n und W_ij.T

	Template-Parameter:
	BATCH_SIZEOfE_n: 		Anzahl der Zeilen von E_n == Anzahl der Bilder, die in einem Durchgang bearbeitet werden sollen
	columnsOfE_n: 	Anzahl der Spalten von E_n == Anzahl der Zeilen von E_n == Anzahl der Neuronen von Schicht i
	columnsOfW_ij: 	Anzahl der Spalten von W_ij == Anzahl der Neuronen von Schicht i+1

	Funktionsparameter
	E_n:			Eingabe f�r die Schicht
	W_ij:			Gewichte der Schicht

	R�ckgabe:
	Matrixprodukt von E_n und W_ij.T
*/
template<unsigned neurons_in, unsigned neurons_out>
void Fully<neurons_in, neurons_out>::backward(float* error_tensor, float* d_input){
	backwardWrapper_Fully(error_tensor, W, d_input, layer_input, neurons_in, neurons_out);
}
