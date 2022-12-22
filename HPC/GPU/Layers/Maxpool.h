#pragma once

#include <array>
#include <iostream>

#include "../Network_Config.h"

#define INPUT_IMAGE_SIZE (INPUT_IMG_DIM * INPUT_IMG_DIM)
#define OUTPUT_IMAGE_SIZE (OUTPUT_IMG_DIM * OUTPUT_IMG_DIM)

using std::array;
using std::cerr;

void forwardWrapper_Maxpool(float*, float*, int*, unsigned, unsigned);
void backwardWrapper_Maxpool(float*, float*, int*, unsigned);

template<unsigned INPUT_IMG_DIM, unsigned OUTPUT_IMG_DIM, unsigned NMB_FILTERS>
class Maxpool {
	int* max_pool_idx;

public:
	Maxpool() {
		// Speicher für max_pool_idx auf der GPU reservieren
		cudaError_t cudaStatus;
		cudaStatus = cudaMalloc((void**) &max_pool_idx, OUTPUT_IMAGE_SIZE * BATCH_SIZE * NMB_FILTERS * sizeof(int));
		if (cudaStatus != cudaSuccess) {
			cerr << "cudaMalloc failed! (Maxpool.h -> max_pool_idx)";
			exit(EXIT_FAILURE);
		}
	}

	~Maxpool(){
		cudaFree(max_pool_idx);
	}

	void forward(float* input, float* output) {
		forwardWrapper_Maxpool(input, output, max_pool_idx, INPUT_IMG_DIM, INPUT_IMAGE_SIZE);
	}

	void backward(float* input, float* output) {
		backwardWrapper_Maxpool(input, output, max_pool_idx, OUTPUT_IMAGE_SIZE);
	}
};