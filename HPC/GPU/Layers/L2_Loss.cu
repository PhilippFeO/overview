#include "cuda_runtime.h"
#include "device_launch_parameters.h"

#include <stdio.h>
#include <iostream>

using std::cerr;
using std::cout;
using std::endl;

#include "../Network_Config.h"

/*
	Berechnet die Differenz zweier Matrizen

	Template-Parameter:
	rows:		Zeilen der Matrix
	columns:	Spalten der Matrix
*/
__global__ void matMinus_L2_Loss(float* left, float* right) {
	int idx = threadIdx.x + blockIdx.x * blockDim.x;
	if (idx < BATCH_SIZE * NEURONS_LAYER3) {
		left[idx] = left[idx] - right[idx];
	}

}


/// FORWARD ///

__global__ void forwardKernel_L2_Loss(float* y_hat, float* labels, float* output) {
	const int row = blockIdx.x;
	const int column = threadIdx.x;

	float tmp = 0.5 * y_hat[row * NEURONS_LAYER3 + column] * y_hat[row * NEURONS_LAYER3 + column];
	atomicAdd(output, tmp);
}

__global__ void forwardInit_L2_Loss(float* output) {
	*output = 0;
}


void forwardWrapper_L2_Loss(float* y_hat, float* labels, float* output) {
	forwardInit_L2_Loss << <1, 1 >> > (output);
	cudaError_t cudaStatus;
	cudaStatus = cudaDeviceSynchronize();
	if (cudaStatus != cudaSuccess) {
		cerr << "[forwardWrapper_L2_Loss -> matMinus] DeviceSync failed: " << cudaStatus << std::endl;
		exit(EXIT_FAILURE);
	}

	dim3 kernelBlocks(BATCH_SIZE);
	dim3 kernelBlockThreads(NEURONS_LAYER3);

	matMinus_L2_Loss << <kernelBlocks, kernelBlockThreads >> > (y_hat, labels);

	cudaStatus = cudaDeviceSynchronize();
	if (cudaStatus != cudaSuccess) {
		cerr << "[forwardWrapper_L2_Loss -> matMinus] DeviceSync failed: " << cudaStatus << std::endl;
		exit(EXIT_FAILURE);
	}

	forwardKernel_L2_Loss << <kernelBlocks, kernelBlockThreads >> > (y_hat, labels, output);
	cudaStatus = cudaDeviceSynchronize();
	if (cudaStatus != cudaSuccess) {
		cerr << "[forwardWrapper_L2_Loss -> forwardKernel] DeviceSync failed: " << cudaStatus << std::endl;
		exit(EXIT_FAILURE);
	}
}


/// BACKWARD ///

__global__ void backwardKernel_L2_Loss(float* y_hat, float* labels, float* output) {
	// Kann man mehr oder weniger überspringen, weil „x - y“, also „y_hat - labels“ schon im Vorwärtsschritt
	// und in „y_hat“ gespeichert wurde
	const int idx = threadIdx.x + blockIdx.x * blockDim.x;
	if (idx < BATCH_SIZE * NEURONS_LAYER3) {
		output[idx] = y_hat[idx];
	}
}

void backwardWrapper_L2_Loss(float* y_hat, float* labels, float* output) {
	dim3 kernelBlocks(BATCH_SIZE);
	dim3 kernelBlockThreads(NEURONS_LAYER3);
	backwardKernel_L2_Loss << <kernelBlocks, kernelBlockThreads >> > (y_hat, labels, output);

	cudaError_t cudaStatus;
	cudaStatus = cudaDeviceSynchronize();
	if (cudaStatus != cudaSuccess) {
		cerr << "[backwardWrapper_L2_Loss] DeviceSync failed: " << cudaStatus;
		exit(EXIT_FAILURE);
	}
}