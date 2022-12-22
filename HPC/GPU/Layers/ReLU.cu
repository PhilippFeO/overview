
#include "cuda_runtime.h"
#include "device_launch_parameters.h"

#include <stdio.h>
#include <iostream>

using std::cerr;
using std::cout;
using std::endl;

#include<cmath>
using std::ceil;
using std::floor;

#include "../Network_Config.h"
#include "../Hilfsdateien/CudaHilfsfunktionen.h"

#define MAX_THREADS 1024
#define MAX_THREADS_IN_X 32

/// FORWARD ///

__global__ void forwardKernel_ReLU(float* input, float* output, const int ROWS, const int COLUMNS){
	// const int idx = (threadIdx.x + threadIdx.y * MAX_THREADS_IN_X) + blockIdx.x * blockDim.x * blockDim.y;
	// if(idx < ROWS * COLUMNS){
		// output[idx] = input[idx] <= 0 ? 0 : input[idx];
	// }
	const int row = blockIdx.x;
	const int column = threadIdx.x;
	const int idx = row * COLUMNS + column;
	output[idx] = input[idx] <= 0 ? 0 : input[idx];
}

void forwardWrapper_ReLU(float* layer_input, float *output, const int ROWS, const int COLUMNS){
	// const int blocks = ceil((ROWS * COLUMNS) / MAX_THREADS);
		// dim3 threadsPerBlock(COLUMNS);
		// dim3 numBlocks(ROWS);
		// printf("Grid : {%d, %d, %d} blocks. Blocks : {%d, %d, %d} threads.\n",
		// 	numBlocks.x, numBlocks.y, numBlocks.z,
		// 	threadsPerBlock.x, threadsPerBlock.y, threadsPerBlock.z);
	forwardKernel_ReLU<<<ROWS, COLUMNS>>>(layer_input, output, ROWS, COLUMNS);
	cudaDeviceSynchronize_Helper(cudaDeviceSynchronize(), "forwardKernel_ReLU");
}


/// BACKWARD ///

__global__ void backwardKernel_ReLU(float* error_tensor, float *layer_input, float *d_input, const int ROWS, const int COLUMNS){
	// const int idx = (threadIdx.x + threadIdx.y * MAX_THREADS_IN_X) + blockIdx.x * blockDim.x * blockDim.y;
	// if(idx < ROWS * COLUMNS){
	// 	error_tensor[idx] = layer_input[idx] <= 0 ? 0 : error_tensor[idx];
	// }
	const int row = blockIdx.x;
	const int column = threadIdx.x;
	const int idx = row * COLUMNS + column;
	d_input[idx] = layer_input[idx] <= 0 ? 0 : error_tensor[idx];
}

void backwardWrapper_ReLU(float *error_tensor, float *layer_input, float *d_input, const int ROWS, const int COLUMNS){	
	// const int blocks = ceil((ROWS * COLUMNS) / MAX_THREADS);
	// dim3 threadsPerBlock(MAX_THREADS_IN_X, MAX_THREADS_IN_X);
	// dim3 numBlocks(blocks);
	// backwardKernel_ReLU<<<numBlocks, threadsPerBlock>>>(error_tensor, layer_input, d_input, ROWS, COLUMNS);
	backwardKernel_ReLU<<<ROWS, COLUMNS>>>(error_tensor, layer_input, d_input, ROWS, COLUMNS);
	cudaDeviceSynchronize_Helper(cudaDeviceSynchronize(), "backwardKernel_ReLU");
}