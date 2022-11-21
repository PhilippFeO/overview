#include "cuda_runtime.h"
#include "device_launch_parameters.h"

#include <stdio.h>
#include <iostream>

// Zum Berechnen des zeilenweisen Extrema
#include <thrust/extrema.h>

using std::cerr;
using std::cout;
using std::endl;

#include "../Network_Config.h"


__global__ void forwardKernel_SoftMax(float* input, float* output){
	const int row = blockIdx.x;
	const int column = threadIdx.x;

	if (row < BATCH_SIZE) {
		__shared__ float sumOfRow;
		sumOfRow = 0;
		__syncthreads();

		if (column < NEURONS_LAYER3) {
			float t = expf(input[row * NEURONS_LAYER3 + column]);
			atomicAdd(&sumOfRow, t);
			__syncthreads();

			output[row * NEURONS_LAYER3 + column] = t / sumOfRow;
		}
	}
}

void forwardWrapper_SoftMax(float* layer_input, float* output){
	dim3 kernelBlocks(BATCH_SIZE);
	dim3 kernelBlockThreads(NEURONS_LAYER3);
	forwardKernel_SoftMax <<<kernelBlocks, kernelBlockThreads>>> (layer_input, output);

	cudaError_t cudaStatus;
	cudaStatus = cudaDeviceSynchronize();
	if (cudaStatus != cudaSuccess) {
		cerr << "DeviceSync failed: " << cudaStatus;
		exit(EXIT_FAILURE);
	}
}