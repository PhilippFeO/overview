#pragma once

#include<iostream>

#include "cuda_runtime.h"
#include "device_launch_parameters.h"

void cudaMalloc_Helper(float*, const int, cudaError_t&, const char*);
void cudaMemcpy_Helper(	float*,
						float*,
						const int,
						const cudaMemcpyKind,
						cudaError_t &,
						const char*);
void cudaHelper(cudaError_t&, bool, const char*);
void printMat(float*, const int, const char*);
void cudaDeviceSynchronize_Helper(cudaError_t cudaStatus, const char* functionName);


// void cudaMalloc_Helper(float* object, const int nmbBytes, cudaError_t &cudaStatus, const char* variableName){
// 	cudaStatus = cudaMalloc((void **) & object, nmbBytes);
// 	cudaHelper(cudaStatus, true, variableName);
// }


// void cudaMemcpy_Helper(	float* dst,
// 						float* src,
// 						const int nmbBytes,
// 						const cudaMemcpyKind mode,
// 						cudaError_t &cudaStatus,
// 						const char* variableName){
// 	cudaStatus = cudaMemcpy(dst, src, nmbBytes, mode);
// 	cudaHelper(cudaStatus, false, variableName);
// }


// // Wertet „cudaStatus“ aus
// void cudaHelper(cudaError_t &cudaStatus, const bool cudaMalloc, const char* variableName){
// 	if(cudaStatus != cudaSuccess) {
// 		std::cerr << "<" << (cudaMalloc ? "cudaMalloc" : "cudaMemcpy") << "> ist fehlgeschlagen während <" << variableName << ">!" << std::endl;
// 		exit(EXIT_FAILURE);
// 	}
// }


// void printMat(float * matrix, const int nmbElements, const char* name){
// 	printf("%s = ", name);
// 	for(int i = 0; i < nmbElements; i++){
// 		printf("%.1f ", matrix[i]);
// 	}
// 	printf("\n");
// }


// void cudaDeviceSynchronize_Helper(cudaError_t cudaStatus, const char* functionName){
// 	if(cudaStatus != cudaSuccess){
// 		std::cerr << "cudaDeviceSynchronize fehlgeschlagen bei " << functionName << std::endl;
// 		exit(EXIT_FAILURE);
// 	}
// }