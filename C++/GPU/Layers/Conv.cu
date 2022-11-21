#include "cuda_runtime.h"
#include "device_launch_parameters.h"

#include <iostream>

#include "../Network_Config.h"

using std::cerr;

#define FILTER_SIZE FILTER_DIM * FILTER_DIM
#define INPUT_IMG_SIZE INPUT_IMG_DIM * INPUT_IMG_DIM
#define OUTPUT_IMG_DIM (INPUT_IMG_DIM - FILTER_DIM + 1)
#define OUTPUT_IMG_SIZE OUTPUT_IMG_DIM * OUTPUT_IMG_DIM

__global__ void forwardKernel(float* input, float* output, float* filters, unsigned filter_num, unsigned filter_dim, unsigned input_img_dim) {
	// Über alle Bilder iterieren
	const unsigned i = blockIdx.x;
	// Über alle Filter iterieren
	const unsigned f = blockIdx.y;
	// Über Zeilen des Bildes iterieren
	const unsigned r = threadIdx.x;
	// Über Spalten des Bildes iterieren
	const unsigned c = threadIdx.y;
	float tmp = 0;
	// Über Zeilen des Filter iterieren
	for (unsigned filter_r = 0; filter_r < filter_dim; ++filter_r) {
		// Über Spalten des Filters iterieren
		for (unsigned filter_c = 0; filter_c < filter_dim; ++filter_c) {
			tmp += input[input_img_dim * input_img_dim * i + (r + filter_r) * input_img_dim + c + filter_c]
				* filters[f * filter_dim * filter_dim + filter_r*filter_dim + filter_c];
		}
	}
	output[i * (input_img_dim - filter_dim + 1) * (input_img_dim - filter_dim + 1) * filter_num
		+ f * (input_img_dim - filter_dim + 1) * (input_img_dim - filter_dim + 1)
		+ r * (input_img_dim - filter_dim + 1) + c] = tmp;
}

void forwardWrapper(float * input, float * output, float* filters, unsigned filter_num, unsigned filter_dim, unsigned input_img_dim)
{
	dim3 numBlocks(BATCH_SIZE, filter_num);
	dim3 threadsPerBlock((input_img_dim - filter_dim + 1), (input_img_dim - filter_dim + 1));
	forwardKernel << <numBlocks, threadsPerBlock >> > (input, output, filters, filter_num, filter_dim, input_img_dim);
	cudaError_t cudaStatus;
	cudaStatus = cudaDeviceSynchronize();
	if (cudaStatus != cudaSuccess) {
		cerr << "DeviceSync failed:" << cudaStatus;
		exit(EXIT_FAILURE);
	}
}

__global__ void backwardKernel(float* error_tensor, float* d_input, float* filters, float* d_filters, float* layer_input,
	unsigned FILTERS, unsigned FILTER_DIM, unsigned INPUT_IMG_DIM) {

	// Über alle Bilder iterieren
	//for (int layer_input_image_index = 0; layer_input_image_index < BATCH_SIZE; ++layer_input_image_index) {
	const int layer_input_image_index = blockIdx.x;
	const int filter_index = threadIdx.x;
	const int rr = threadIdx.y;
	const int cc = threadIdx.z;
	d_filters[layer_input_image_index * FILTER_SIZE * FILTERS + filter_index * FILTER_SIZE + FILTER_DIM * rr + cc] = 0;
	// Über die Zeilen des Bildes iterieren
	for (int r = 0; r < OUTPUT_IMG_DIM; ++r) {
		// Über die Spalten des Bildes iterieren
		for (int c = 0; c < OUTPUT_IMG_DIM; ++c) {
			// Über alle Filter iterieren
			//for (int filter_index = 0; filter_index < FILTERS; ++filter_index) {
			// Über Zeilen des Filters iterieren
			//for (int rr = 0; rr < FILTER_DIM; ++rr) {
				// Über Spalten des Filters iterieren
				//for (int cc = 0; cc < FILTER_DIM; ++cc) {
					// Gradient bzgl. der Gewichte
			d_filters[layer_input_image_index * FILTER_SIZE * FILTERS + filter_index * FILTER_SIZE + FILTER_DIM * rr + cc] +=
				error_tensor[layer_input_image_index * FILTERS * OUTPUT_IMG_SIZE + filter_index * OUTPUT_IMG_SIZE + r * OUTPUT_IMG_DIM + c] *
				layer_input[INPUT_IMG_SIZE * layer_input_image_index + (r + rr) * INPUT_IMG_DIM + (c + cc)];
			// Gradient bezüglich der Eingabe
			//d_input[layer_input_image_index * INPUT_IMG_SIZE + (r + rr) * INPUT_IMG_DIM + (c + cc)] +=
			//	error_tensor[layer_input_image_index * FILTERS * OUTPUT_IMG_SIZE + filter_index * OUTPUT_IMG_SIZE + r * OUTPUT_IMG_DIM + c] *
			//	filters[filter_index * FILTER_SIZE + rr * FILTER_DIM + cc];
		//}
	//}
	//}
		}
	}
	//}
}

__global__ void backwardInit(float* d_input, float* d_filters, unsigned FILTERS) {
	// Ableitungsdatenstruktur mit 0 initialisieren
	for (int i = threadIdx.x; i < BATCH_SIZE * TRAIN_IMAGE_SIZE; i += 32) d_input[i] = 0;
}

__global__ void gradient(float* filters, float* d_filters, unsigned FILTERS, const float learning_rate) {
	// Gradientenabstieg: Filter - learning_rate * d_Filter
	for (int i = threadIdx.x; i < FILTERS * FILTER_SIZE; i += 32) {
		filters[i] -= learning_rate * d_filters[i];
	}
}

__global__ void reduktion(float* g_odata, float* d_input, const unsigned FILTERS, const unsigned FILTER_DIM) {
	extern __shared__ float sdata[];
	if (threadIdx.x < BATCH_SIZE) {
		sdata[threadIdx.x] = g_odata[threadIdx.x * FILTER_SIZE * FILTERS + blockIdx.x * FILTER_SIZE + blockIdx.y * FILTER_DIM + blockIdx.z];
	}
	else {
		sdata[threadIdx.x] = 0;
	}
	__syncthreads();

	for (int s = 1; s < blockDim.x; s *= 2) {
		if (threadIdx.x % (2 * s) == 0) {
			//printf("%d %d %d\n", threadIdx.x, s, threadIdx.x + s);
			sdata[threadIdx.x] += sdata[threadIdx.x + s];
		}
		__syncthreads();
	}
	if (threadIdx.x == 0) d_input[blockIdx.x * FILTER_SIZE + blockIdx.y * FILTER_DIM + blockIdx.z] = sdata[0];
}

int reduktionThreads() {
	if (!((BATCH_SIZE & (BATCH_SIZE - 1)) == 0)) {
		if (BATCH_SIZE < 4) return 4;
		if (BATCH_SIZE < 8) return 8;
		if (BATCH_SIZE < 16) return 16;
		if (BATCH_SIZE < 32) return 32;
		if (BATCH_SIZE < 64) return 64;
		if (BATCH_SIZE < 128) return 128;
		if (BATCH_SIZE < 256) return 256;
		if (BATCH_SIZE < 512) return 512;
		if (BATCH_SIZE < 1024) return 1024;
	}
	else {
		return BATCH_SIZE;
	}
}

void backwardWrapper(float* error_tensor, float* d_input, float* filters, float* d_filters, float* layer_input,
	unsigned FILTERS, unsigned FILTER_DIM, unsigned INPUT_IMG_DIM, float* g_odata)
{
	backwardInit << <1, 32 >> > (d_input, d_filters, FILTERS);
	cudaError_t cudaStatus = cudaDeviceSynchronize();
	if (cudaStatus != cudaSuccess) {
		cerr << "DeviceSync failed:" << cudaStatus;
		exit(EXIT_FAILURE);
	}

	dim3 threadsPerBlock(FILTERS, FILTER_DIM, FILTER_DIM);
	backwardKernel << <BATCH_SIZE, threadsPerBlock >> > (error_tensor, d_input, filters, g_odata, layer_input,
		FILTERS, FILTER_DIM, INPUT_IMG_DIM);
	cudaStatus = cudaDeviceSynchronize();
	if (cudaStatus != cudaSuccess) {
		cerr << "DeviceSync failed:" << cudaStatus;
		exit(EXIT_FAILURE);
	}

	//Reduktion
	int threads = reduktionThreads();
	reduktion << <threadsPerBlock, threads, threads * sizeof(float) >> > (g_odata, d_filters, FILTERS, FILTER_DIM);
	cudaStatus = cudaDeviceSynchronize();
	if (cudaStatus != cudaSuccess) {
		cerr << "DeviceSync failed:" << cudaStatus;
		exit(EXIT_FAILURE);
	}


	gradient << <1, 32 >> > (filters, d_filters, FILTERS, learning_rate);
	cudaStatus = cudaDeviceSynchronize();
	if (cudaStatus != cudaSuccess) {
		cerr << "DeviceSync failed:" << cudaStatus;
		exit(EXIT_FAILURE);
	}
}
