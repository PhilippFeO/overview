//Cuda
#include "cuda_runtime.h"
#include "device_launch_parameters.h"
// Schichten inkludieren
#include "./Layers/ReLU.h"
#include "./Layers/Fully.h"
#include "./Layers/L2_Loss.h"
#include "./Layers/SoftMax.h"
#include "./Layers/Maxpool.h"
#include "./Layers/Conv.h"

#include "Network_Config.h"

#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <sstream>
#include <chrono>

using std::cerr;
using std::vector;
using std::string;
using std::ifstream;

vector<string> split(const string &s, char delim) {
	std::stringstream ss(s);
	string item;
	vector<string> tokens;
	while (getline(ss, item, delim)) {
		tokens.push_back(item);
	}
	return tokens;
}

int main(){

	printf("+++ %d %d %d\n", ITERATIONS, BATCH_SIZE, NMB_FILTERS);

	string line;
    vector<string> line_v;

    // cout << "Loading data ...\n";
    // x_train_NN wird alle Bilder linear enthalten
    vector<float> x_train_NN;
    vector<float> y_train_NN;

	//Size of y is 42000×1, and the size of X is 42000×784. 
	//Every line of X is a 28×28 grayscale picture of a handwritten number. 
	//Every element of y is a number from 0 to 9.
    ifstream myfile ("train.txt");
    if (myfile.is_open())
    {
		int ii = 0;
        while ( getline (myfile,line) )
        {
            line_v = split(line, '\t');
            float digit = strtof((line_v[0]).c_str(),0);
            for (unsigned i = 0; i < 10; ++i) {
                if (i == digit)
                {
                    y_train_NN.push_back(1.);
                }
                else y_train_NN.push_back(0.);
            }
            
            unsigned size = static_cast<unsigned>(line_v.size());
            for (unsigned i = 1; i < size; ++i) {
                x_train_NN.push_back(strtof((line_v[i]).c_str(),0)/255.0f);
            }
        }
        myfile.close();
    }

	// Grafikkarte auswählen
	cudaError_t cudaStatus;
	cudaStatus = cudaSetDevice(0);
	if (cudaStatus != cudaSuccess) {
		cerr << "cudaSetDevice failed!  Do you have a CUDA-capable GPU installed?";
		exit(EXIT_FAILURE);
	}

	// Speicher für Trainingsdaten auf der GPU allokieren
	float* x_train;
	cudaStatus = cudaMalloc((void**)&x_train, x_train_NN.size() * sizeof(float));
	if (cudaStatus != cudaSuccess) {
		cerr << "cudaMalloc failed!";
		exit(EXIT_FAILURE);
	}

	float* y_train;
	cudaStatus = cudaMalloc((void**)&y_train, y_train_NN.size() * sizeof(float));
	if (cudaStatus != cudaSuccess) {
		cerr << "cudaMalloc failed!";
		exit(EXIT_FAILURE);
	}

	// Trainingsdaten auf GPU kopieren
	cudaStatus = cudaMemcpy(x_train, &x_train_NN[0], x_train_NN.size() * sizeof(float), cudaMemcpyHostToDevice);
	if (cudaStatus != cudaSuccess) {
		cerr << "cudaMemcpy failed!";
		exit(EXIT_FAILURE);
	}

	cudaStatus = cudaMemcpy(y_train, &y_train_NN[0], y_train_NN.size() * sizeof(float), cudaMemcpyHostToDevice);
	if (cudaStatus != cudaSuccess) {
		cerr << "cudaMemcpy failed!";
		exit(EXIT_FAILURE);
	}

	// Initialisieren der Schichten
    Conv<NMB_FILTERS, FILTER_DIM, TRAIN_IMAGE_DIM> Conv;
    Maxpool<CONV_OUTPUT_IMG_DIM, MAXPOOL_OUTPUT_IMG_DIM, NMB_FILTERS> Maxpool;
    Fully<NEURONS_LAYER1, NEURONS_LAYER2> FC_12;
    ReLU<BATCH_SIZE, NEURONS_LAYER2> ReLU_2;
    Fully<NEURONS_LAYER2, NEURONS_LAYER3> FC_23;    
    SoftMax SoftMax;
    L2_Loss L2_Loss;

    // Datenstrukturen für die Vorwärtspropagation allokieren
	float* Conv_output;
	cudaStatus = cudaMalloc((void**)&Conv_output, BATCH_SIZE * NMB_FILTERS * CONV_OUTPUT_IMG_SIZE * sizeof(float));
	if (cudaStatus != cudaSuccess) {
		cerr << "cudaMalloc failed!";
		exit(EXIT_FAILURE);
	}

	float* Maxpool_output;
	cudaStatus = cudaMalloc((void**)&Maxpool_output, BATCH_SIZE * NMB_FILTERS * MAXPOOL_OUTPUT_IMG_SIZE * sizeof(float));
	if (cudaStatus != cudaSuccess) {
		cerr << "cudaMalloc failed!";
		exit(EXIT_FAILURE);
	}

	float* FC_12_output;
	cudaStatus = cudaMalloc((void**)&FC_12_output, BATCH_SIZE * NEURONS_LAYER2 * sizeof(float));
	if (cudaStatus != cudaSuccess) {
		cerr << "cudaMalloc failed!";
		exit(EXIT_FAILURE);
	}

	float* ReLU_2_output;
	cudaStatus = cudaMalloc((void**)&ReLU_2_output, BATCH_SIZE * NEURONS_LAYER2 * sizeof(float));
	if (cudaStatus != cudaSuccess) {
		cerr << "cudaMalloc failed!";
		exit(EXIT_FAILURE);
	}

	float* FC_23_output;
	cudaStatus = cudaMalloc((void**)&FC_23_output, BATCH_SIZE * NEURONS_LAYER3 * sizeof(float));
	if (cudaStatus != cudaSuccess) {
		cerr << "cudaMalloc failed!";
		exit(EXIT_FAILURE);
	}

	float* y_hat;
	cudaStatus = cudaMalloc((void**)&y_hat, BATCH_SIZE * NEURONS_LAYER3 * sizeof(float));
	if (cudaStatus != cudaSuccess) {
		cerr << "cudaMalloc failed!";
		exit(EXIT_FAILURE);
	}

    float loss;

	float *loss_gpu;
	cudaStatus = cudaMalloc((void **)&loss_gpu, sizeof(float));
	if (cudaStatus != cudaSuccess) {
		cerr << "cudaMalloc failed!";
		exit(EXIT_FAILURE);
	}

    // Datenstrukturen für die Rückwärtspropagation allokieren
	float* d_y_hat;
	cudaStatus = cudaMalloc((void**)&d_y_hat, BATCH_SIZE * NEURONS_LAYER3 * sizeof(float));
	if (cudaStatus != cudaSuccess) {
		cerr << "cudaMalloc failed!";
		exit(EXIT_FAILURE);
	}

	float* FC_23_backward_output;
	cudaStatus = cudaMalloc((void**)&FC_23_backward_output, BATCH_SIZE * NEURONS_LAYER2 * sizeof(float));
	if (cudaStatus != cudaSuccess) {
		cerr << "cudaMalloc failed!";
		exit(EXIT_FAILURE);
	}

	float* ReLU_2_backward_output;
	cudaStatus = cudaMalloc((void**)&ReLU_2_backward_output, BATCH_SIZE * NEURONS_LAYER2 * sizeof(float));
	if (cudaStatus != cudaSuccess) {
		cerr << "cudaMalloc failed!";
		exit(EXIT_FAILURE);
	}

	float* FC_12_backward_output;
	cudaStatus = cudaMalloc((void**)&FC_12_backward_output, BATCH_SIZE * NEURONS_LAYER1 * sizeof(float));
	if (cudaStatus != cudaSuccess) {
		cerr << "cudaMalloc failed!";
		exit(EXIT_FAILURE);
	}

	float* Maxpool_backward_output;
	cudaStatus = cudaMalloc((void**)&Maxpool_backward_output, BATCH_SIZE * NMB_FILTERS * CONV_OUTPUT_IMG_SIZE * sizeof(float));
	if (cudaStatus != cudaSuccess) {
		cerr << "cudaMalloc failed!";
		exit(EXIT_FAILURE);
	}
	
	float* Conv_backward_output;
	cudaStatus = cudaMalloc((void**)&Conv_backward_output, BATCH_SIZE * TRAIN_IMAGE_SIZE * sizeof(float));
	if (cudaStatus != cudaSuccess) {
		cerr << "cudaMalloc failed!";
		exit(EXIT_FAILURE);
	}

	// cout << "Training the model ...\n";

	// const int iterations = 100;

	auto begin = std::chrono::high_resolution_clock::now();

    for (unsigned i = 0; i < ITERATIONS; ++i) {
        // Building batches of input variables (x) and labels (y)
        // unsigned indx = (i * BATCH_SIZE) % (2000-BATCH_SIZE);
        int indx = rand() % (42000-BATCH_SIZE);


        // Vorwärtspropagation
		Conv.forward(&x_train[indx*TRAIN_IMAGE_SIZE], Conv_output);
		Maxpool.forward(Conv_output, Maxpool_output);		
		FC_12.forward(Maxpool_output, FC_12_output);
		ReLU_2.forward(FC_12_output, ReLU_2_output);
		FC_23.forward(ReLU_2_output, FC_23_output);
		SoftMax.forward(FC_23_output, y_hat);
		L2_Loss.forward(y_hat, &y_train[indx*10], loss_gpu);

		//loss_gpu auf cpu kopieren
		cudaStatus = cudaMemcpy(&loss, loss_gpu, sizeof(float), cudaMemcpyDeviceToHost);
		if (cudaStatus != cudaSuccess) {
			cerr << "cudaMemcpy failed!";
			exit(EXIT_FAILURE);
		}

        // Rückwärtspropagation
		L2_Loss.backward(y_hat, &y_train[indx*10], d_y_hat);
		FC_23.backward(d_y_hat, FC_23_backward_output);
		ReLU_2.backward(FC_23_backward_output, ReLU_2_backward_output);
		FC_12.backward(ReLU_2_backward_output, FC_12_backward_output);
		Maxpool.backward(FC_12_backward_output, Maxpool_backward_output);
		Conv.backward(Maxpool_backward_output, Conv_backward_output);

		// cout << "[GPU] Epoch " << i << "   Loss: " << 2 * loss / BATCH_SIZE << endl;

        //if ((i+1) % 100 == 0){
        //    // cout << "Epoch " << i+1 << endl;
        //    cout << "-----------------------------------------------Epoch " << i+1 << "--------------------------------------------------" <<"\n";
        //    cout << "Predictions:" << "\n";
        //    //printMatrix<BATCH_SIZE, NEURONS_LAYER3>(y_hat, 10, 10);
        //    cout << "Ground truth:" << "\n";
        //    //printMatrix<BATCH_SIZE, NEURONS_LAYER3>(b_y, 10, 10);
        //    cout << "                                            Loss " << 2*loss/BATCH_SIZE <<"\n";
        //    cout << "--------------------------------------------End of Epoch :(------------------------------------------------" <<"\n";
        //};
	}

	auto end = std::chrono::high_resolution_clock::now();

	std::ofstream file;
	file.open("../time_measurements/GPU_Par_" + std::to_string(ITERATIONS) + "-Iter_" + std::to_string(BATCH_SIZE) + "-Batchsize_" + std::to_string(NMB_FILTERS) + "-Filters.txt", std::ios_base::app);

	auto duration = std::chrono::duration_cast<std::chrono::nanoseconds>(end-begin).count();
	file << duration << "\n";

	// for(int i = 0; i < 11; i++){
	// 	file << arrr[i] << "\n";
	// }
	// file << ">>> NEW MEASUREMENT <<<\n";

	file.close();

	// Speicher freigeben
	cudaFree(x_train);
	cudaFree(y_train);

	cudaFree(Conv_output);
	cudaFree(Maxpool_output);
	cudaFree(FC_12_output);
	cudaFree(ReLU_2_output);
	cudaFree(FC_23_output);
	cudaFree(y_hat);
	cudaFree(loss_gpu);

	cudaFree(d_y_hat);
	cudaFree(FC_23_backward_output);
	cudaFree(ReLU_2_backward_output);
	cudaFree(FC_12_backward_output);
	cudaFree(Maxpool_backward_output);
	cudaFree(Conv_backward_output);

	return 0;
}