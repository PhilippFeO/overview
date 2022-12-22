// #pragma once

#include "cuda_runtime.h"
#include "device_launch_parameters.h"

#include <stdio.h>
#include <iostream>

using std::cerr;
using std::cout;
using std::endl;

#include "../Network_Config.h"

__device__ float GetMax(float x1, float x2, float x3, float x4) {
	float tmp = x1;
	if (tmp < x2) tmp = x2;
	if (tmp < x3) tmp = x3;
	if (tmp < x4) tmp = x4;
	return tmp;
}

/// FORWARD ///

__global__ void forwardKernel_Maxpool(float* input, float* output, int* max_pool_idx, unsigned INPUT_IMG_DIM, unsigned INPUT_IMAGE_SIZE){
	const int i = blockIdx.x;
	const int row = threadIdx.x;

	const int nmbOfPictures = BATCH_SIZE * NMB_FILTERS;
	int nextBlock, nextRow, nextMatrix;
	int counter;

	if (i < nmbOfPictures) {
		if (row < INPUT_IMG_DIM && row % 2 == 0) {
			counter = i * (INPUT_IMG_DIM * INPUT_IMG_DIM / 4) + row * (INPUT_IMG_DIM / 4) + row / 2;
			for (int c = 0; c < INPUT_IMG_DIM; c += 2) {
				nextMatrix = i * INPUT_IMAGE_SIZE;
				nextBlock = c + row * INPUT_IMG_DIM + nextMatrix;
				nextRow = c + (row + 1) * INPUT_IMG_DIM + nextMatrix;

				float x1 = input[nextBlock];
				float x2 = input[nextBlock + 1];
				float x3 = input[nextRow];
				float x4 = input[nextRow + 1];

				output[counter] = GetMax(x1, x2, x3, x4);

				if (output[counter] == x1) max_pool_idx[counter] = nextBlock;
				if (output[counter] == x2) max_pool_idx[counter] = nextBlock + 1;
				if (output[counter] == x3) max_pool_idx[counter] = nextRow;
				if (output[counter] == x4) max_pool_idx[counter] = nextRow + 1;

				counter++;
			}
		}
	}
}

void forwardWrapper_Maxpool(float* input, float* output, int* max_pool_idx, unsigned INPUT_IMG_DIM, unsigned INPUT_IMAGE_SIZE){
	dim3 kernelBlocks(BATCH_SIZE * NMB_FILTERS);
	dim3 kernelBlockThreads(INPUT_IMG_DIM);
	forwardKernel_Maxpool <<<kernelBlocks, kernelBlockThreads>>> (input, output, max_pool_idx, INPUT_IMG_DIM, INPUT_IMAGE_SIZE);

	cudaError_t cudaStatus;
	cudaStatus = cudaDeviceSynchronize();
	if (cudaStatus != cudaSuccess) {
		cerr << "DeviceSync failed (Maxpool.cu -> forwardWrapper): " << cudaStatus;
		exit(EXIT_FAILURE);
	}
}


/// BACKWARD ///

__global__ void backwardKernel_Maxpool(float* input, float* output, int* max_pool_idx, unsigned output_image_size){
	const int i = threadIdx.x + blockIdx.x * blockDim.x;
	if (i < BATCH_SIZE * NMB_FILTERS * output_image_size) {
		output[max_pool_idx[i]] = input[i];
	}
}

void backwardWrapper_Maxpool(float* input, float* output, int* max_pool_idx, unsigned output_image_size){
	cudaError_t cudaStatus;
	cudaStatus = cudaMemset(output, 0, BATCH_SIZE * NMB_FILTERS * CONV_OUTPUT_IMG_SIZE * sizeof(float));
	if (cudaStatus != cudaSuccess) {
		std::cerr << "cudaMemset in backwardWrapper_Maxpool failed with " << cudaStatus << std::endl;
		exit(EXIT_FAILURE);
	}

	dim3 kernelBlocks(BATCH_SIZE * NMB_FILTERS);
	dim3 kernelBlockThreads(output_image_size);
	backwardKernel_Maxpool <<<kernelBlocks, kernelBlockThreads>>> (input, output, max_pool_idx, output_image_size);

	cudaStatus = cudaDeviceSynchronize();
	if (cudaStatus != cudaSuccess) {
		cerr << "DeviceSync failed in backwardWrapper_Maxpool: " << cudaStatus;
		exit(EXIT_FAILURE);
	}
}