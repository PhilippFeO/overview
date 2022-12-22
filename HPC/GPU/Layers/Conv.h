#pragma once

//Cuda
#include "cuda_runtime.h"
#include "device_launch_parameters.h"

#include <vector>
#include <iostream>
#include <random>

#include "../Network_Config.h"

#define FILTER_SIZE FILTER_DIM * FILTER_DIM

using std::vector;

void forwardWrapper(float*, float*, float*, unsigned, unsigned, unsigned);
void backwardWrapper(float* error_tensor, float* d_input, float* filters, float* d_filters, float* layer_input,	unsigned FILTERS,
	unsigned FILTER_DIM, unsigned INPUT_IMG_DIM, float* g_odata);

template<unsigned FILTERS, unsigned FILTER_DIM, unsigned INPUT_IMG_DIM>
class Conv {
	// Gewichte der Schicht
	float* filters; //FILTERS * FILTER_SIZE
	float*  d_filters; //FILTERS * FILTER_SIZE
	// Eingabe der Schicht (wird in „backward()“ benötigt)
	float* layer_input;
	// Speicher für Parallel Reduktion
	float* g_odata;

public:
	Conv() {
		//Filter initalisieren
		vector<float> tmp;
		std::default_random_engine gen;
		std::normal_distribution<float> dis(0, sqrt(2.0f / (2.0f * FILTER_SIZE)));
		for (unsigned i = 0; i < FILTERS; ++i) {
			for (unsigned j = 0; j < FILTER_SIZE; ++j) {
				tmp.push_back(dis(gen));
			}
		}
		//Speicher für Filter auf der GPU reservieren
		cudaError_t cudaStatus;
		cudaStatus = cudaMalloc((void**)&filters, FILTERS * FILTER_SIZE * sizeof(float));
		if (cudaStatus != cudaSuccess) {
			cerr << "cudaMalloc failed!";
			exit(EXIT_FAILURE);
		}
		//Filter auf GPU kopieren
		cudaStatus = cudaMemcpy(filters, &tmp[0], tmp.size() * sizeof(float), cudaMemcpyHostToDevice);
		if (cudaStatus != cudaSuccess) {
			cerr << "cudaMemcpy failed!";
			exit(EXIT_FAILURE);
		}
		//Speicher für d_filter auf der GPU reservieren
		cudaStatus = cudaMalloc((void**)&d_filters, FILTERS * FILTER_SIZE * sizeof(float));
		if (cudaStatus != cudaSuccess) {
			cerr << "cudaMalloc failed!";
			exit(EXIT_FAILURE);
		}
		//Speicher für Parallel Reduktion auf der GPU reservieren
		cudaStatus = cudaMalloc((void**)&g_odata, BATCH_SIZE * FILTERS * FILTER_SIZE * sizeof(float));
		if (cudaStatus != cudaSuccess) {
			cerr << "cudaMalloc failed!";
			exit(EXIT_FAILURE);
		}
	}

	~Conv() {
		cudaFree(filters);
		cudaFree(d_filters);
		cudaFree(g_odata);
	}

	void forward(float* input, float* output) {
		// Eingabe für „backward“ speichern
		layer_input = input;
		forwardWrapper(input, output, filters, FILTERS, FILTER_DIM, INPUT_IMG_DIM);
	}


	void backward(float* error_tensor, float* d_input) {
		backwardWrapper(error_tensor, d_input, filters, d_filters, layer_input, FILTERS, FILTER_DIM, INPUT_IMG_DIM, g_odata);		
	}
};
