#include "CudaHilfsfunktionen.h"

using std::cerr;
using std::cout;
using std::endl;

void cudaMalloc_Helper(float* object, const int nmbBytes, cudaError_t &cudaStatus, const char* variableName){
	cudaStatus = cudaMalloc((void **) & object, nmbBytes);
	cudaHelper(cudaStatus, true, variableName);
}


void cudaMemcpy_Helper(	float* dst,
						float* src,
						const int nmbBytes,
						const cudaMemcpyKind mode,
						cudaError_t &cudaStatus,
						const char* variableName){
	cudaStatus = cudaMemcpy(dst, src, nmbBytes, mode);
	cudaHelper(cudaStatus, false, variableName);
}


// Wertet „cudaStatus“ aus
void cudaHelper(cudaError_t &cudaStatus, const bool cudaMalloc, const char* variableName){
	if(cudaStatus != cudaSuccess) {
		std::cerr << "<" << (cudaMalloc ? "cudaMalloc" : "cudaMemcpy") << "> ist fehlgeschlagen während <" << variableName << "> mit Fehlercode: " << cudaStatus << "!" << std::endl;
		std::cerr << "\t" << cudaGetErrorString(cudaStatus) << std::endl;
		exit(EXIT_FAILURE);
	}
}


void printMat(float * matrix, const int nmbElements, const char* name){
	printf("%s = ", name);
	for(int i = 0; i < nmbElements; i++){
		printf("%.1f ", matrix[i]);
	}
	printf("\n");
}


void cudaDeviceSynchronize_Helper(cudaError_t cudaStatus, const char* functionName){
	if(cudaStatus != cudaSuccess){
		std::cerr << "cudaDeviceSynchronize mit Status " << cudaStatus << " fehlgeschlagen bei " << functionName << std::endl;
		exit(EXIT_FAILURE);
	}
}