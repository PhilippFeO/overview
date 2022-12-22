
#include "cuda_runtime.h"
#include "device_launch_parameters.h"

#include <iostream>
#include <fstream>
#include <string>

#include "../Network_Config.h"
#include "../Hilfsdateien/CudaHilfsfunktionen.h"
#include "../Hilfsdateien/MatrixOperations.h"

// Berechnet die Koordiaten-Darstellung für die zu startenden Blöcke

dim3 calc_blocks(const int threads){
    const int rest = threads % MAX_THREADS == 0 ? 0 : 1;
    const int blocks_per_row = threads / MAX_THREADS + rest;
    //printf("columns / MAX_THREADS + rest = %d\n", blocks_per_row);
    const dim3 blocks_for_transpose(BATCH_SIZE, blocks_per_row);
    return blocks_for_transpose;
}

void printToFile_parallel(float *data_GPU, const int nmbElements, std::string filename, const bool logging){
	if (logging) {
		float* data_CPU = new float[nmbElements];
		cudaMemcpy(data_CPU, data_GPU, nmbElements * sizeof(float), cudaMemcpyDeviceToHost);
		std::ofstream out(("./Ausgaben/parallel_" + filename + ".txt").c_str());
		for (int i = 0; i < nmbElements; i++) {
			// out<<"["<<i<<"] "<<data_CPU[i]<<"\n";
			out << data_CPU[i] << "\n";
		}
		out.close();
		delete[] data_CPU;
	}
}

/// FORWARD ///////////////////////////////////////////

void forwardWrapper_Fully(float* layer_input, float* W, float* output, const int NEURONS_IN, const int NEURONS_OUT){
	// Führe Kernel-Funktion aus
	// Jeder Thread übernimmt eine Reihen-Spalte-Multiplikation
	// Es gibt BATCH_SIZE * NEURONS_OUT viele solcher Multiplikationen
	// => BATCH_SIZE * NEURONS_OUT Threads
	dim3 nmbBlocks(BATCH_SIZE);	// Ein Block für jede Reihe
	dim3 threadsPerBlock(NEURONS_OUT); // Ein Thread für jede Spalte 
	matMul_parallel<<<nmbBlocks, threadsPerBlock>>>(layer_input, W, output, BATCH_SIZE, NEURONS_IN, NEURONS_OUT);
	// printf("Grid : {%d, %d, %d} blocks. Blocks : {%d, %d, %d} threads.\n",
	// 	nmbBlocks.x, nmbBlocks.y, nmbBlocks.z,
	// 	threadsPerBlock.x, threadsPerBlock.y, threadsPerBlock.z);
	// matMul<<<1, 1>>>(layer_input, W, output, BATCH_SIZE, NEURONS_IN, NEURONS_OUT);
	cudaDeviceSynchronize_Helper(cudaDeviceSynchronize(), "matMul (Fully, Vorwärtsschritt)");
}



/// BACKWARD //////////////////////////////////////////

void backwardWrapper_Fully(	float* error_tensor,
							float* W,
							float* d_input, 
							float* layer_input,
							const int NEURONS_IN,
							const int NEURONS_OUT){

	const bool logging = true;
	
	float *W_transposed;
	const int nmbBytesOfW_transposed = NEURONS_IN * NEURONS_OUT * sizeof(float);
	cudaError_t cudaStatus;

	// Reserviere Speicher für „W_transposed“
	cudaStatus = cudaMalloc((void **) &W_transposed, nmbBytesOfW_transposed);
	cudaHelper(cudaStatus, true, "W_transposed");

	// Rückwärtsschritt: error_tensor * W.T
	// Transponiere Matrix „W“ steht nun in „W_transposed“ und befindet sich bereits auf der GPU
	// Für jede Reihe einen Block und für jede Spalte einen Thread
	transpose_parallel<<< NEURONS_IN, NEURONS_OUT >>>(W, W_transposed, NEURONS_IN, NEURONS_OUT);
	// transpose<<<1, 1>>>(W, W_transposed, BATCH_SIZE, NEURONS_IN);
	cudaDeviceSynchronize_Helper(cudaDeviceSynchronize(), "transpose 1 (Fully parallel, Rückwärtsschritt)");
		// printToFile_parallel(W_transposed, NEURONS_IN * NEURONS_OUT, "W_transposed", logging);

	// Führe Rückwärtsschritt durch
	// matMul<<<1, 1>>>(error_tensor, W_transposed, d_input, BATCH_SIZE, NEURONS_IN, NEURONS_OUT);
	const dim3 blocks_for_matMul = calc_blocks (NEURONS_IN);
	matMul_parallel<<< blocks_for_matMul, MAX_THREADS >>> (error_tensor, W_transposed, d_input, BATCH_SIZE, NEURONS_OUT, NEURONS_IN);
	// matMul_parallel<<< BATCH_SIZE, NEURONS_IN >>> (error_tensor, W_transposed, d_input, BATCH_SIZE, NEURONS_OUT, NEURONS_IN);
	cudaDeviceSynchronize_Helper(cudaDeviceSynchronize(), "matMul 1 (Fully parallel, Rückwärtsschritt)");
		// printToFile_parallel(d_input, BATCH_SIZE * NEURONS_IN, "d_input", logging);


	// Gradientenabstieg: W = W - learning_rate * d_W * error_tensor
	// Ableitung bzgl Gewichte: d_W = input.T * _
	float *d_W;
	const int nmbBytes_d_W = BATCH_SIZE * NEURONS_IN * sizeof(float);
	cudaStatus = cudaMalloc((void **) &d_W, nmbBytes_d_W);
	cudaHelper(cudaStatus, true, "d_W");
	// transpose<<<1, 1>>>(layer_input, d_W, BATCH_SIZE, NEURONS_IN);
	dim3 blocks_for_transpose = calc_blocks(NEURONS_IN);
	transpose_parallel<<< blocks_for_transpose, MAX_THREADS >>>(layer_input, d_W, BATCH_SIZE, NEURONS_IN);
	// transpose_parallel<<< BATCH_SIZE, NEURONS_IN >>>(layer_input, d_W, BATCH_SIZE, NEURONS_IN);
	cudaDeviceSynchronize_Helper(cudaDeviceSynchronize(), "transpose 2 (Fully parallel, Gradientenabstieg)");
		// printToFile_parallel(layer_input, BATCH_SIZE * NEURONS_IN, "input_transposed", logging);

	// Berechne d_W * error_tensor = input.T * error_tensor
	float *tmp;
	const int nmbBytes_tmp = NEURONS_IN * NEURONS_OUT * sizeof(float);
	cudaStatus = cudaMalloc((void **) &tmp, nmbBytes_tmp);
	cudaHelper(cudaStatus, true, "tmp");
	// matMul<<<1, 1>>>(d_W, error_tensor, tmp, NEURONS_IN, BATCH_SIZE, NEURONS_OUT);
	matMul_parallel<<< NEURONS_IN, NEURONS_OUT >>>(d_W, error_tensor, tmp, NEURONS_IN, BATCH_SIZE, NEURONS_OUT);
	cudaDeviceSynchronize_Helper(cudaDeviceSynchronize(), "matMul 2 (Fully parallel, Gradientenabstieg)");
		// printToFile_parallel(tmp, NEURONS_IN * NEURONS_OUT, "d_W_times_error_tensor", logging);

	// W = W - learning_rate * (d_W * error_tensor) = W - learning_rate * tmp
	// scalarMult<<<1, 1>>>(learning_rate, tmp, NEURONS_IN, NEURONS_OUT);
	scalarMult_parallel<<< NEURONS_IN, NEURONS_OUT >>>(
		learning_rate,
		tmp,
		NEURONS_IN, NEURONS_OUT);
	cudaDeviceSynchronize_Helper(cudaDeviceSynchronize(), "scalarMult (Fully, Gradientenabstieg)");
	
	// matMinus<<<1, 1>>>(W, tmp, NEURONS_IN, NEURONS_OUT);
	matMinus_parallel<<< NEURONS_IN, NEURONS_OUT >>> (W, tmp, NEURONS_IN, NEURONS_OUT);
	cudaDeviceSynchronize_Helper(cudaDeviceSynchronize(), "matMinus (Fully, Gradientenabstieg)");
		// printToFile_parallel(W, NEURONS_IN * NEURONS_OUT, "neue_Gewichte", logging);

	// Speicher freigeben
	cudaFree(W_transposed); cudaFree(d_W); cudaFree(tmp);
}