// Schichten inkludieren
#include "./Layers/ReLU.h"
#include "./Layers/Fully.h"
#include "./Layers/L2_Loss.h"
#include "./Layers/SoftMax.h"
#include "./Layers/Maxpool.h"
#include "./Layers/Conv.h"

#include "Network_Config.h"

#include<fstream>
#include<string>
#include<vector>
#include<sstream>
#include<chrono>

using std::vector;
using std::string;
using std::ifstream;

vector<string> split(const string &s, char delim);
// std::ofstream file("Epoch_Print.txt");

int main(){

    printf(">>> %d %d %d\n", ITERATIONS, BATCH_SIZE, NMB_FILTERS);

	string line;
    vector<string> line_v;

    // cout << "Loading data ...\n";
    // X_train_NN wird alle Bilder linear enthalten
    vector<float> X_train_NN;
    vector<float> y_train_NN;

	//Labels (1st column) and the variables. 
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
            int digit = strtof((line_v[0]).c_str(),0);
            for (unsigned i = 0; i < 10; ++i) {
                if (i == digit)
                {
                    y_train_NN.push_back(1.);
                }
                else y_train_NN.push_back(0.);
            }
            
            int size = static_cast<int>(line_v.size());
            for (unsigned i = 1; i < size; ++i) {
                X_train_NN.push_back(strtof((line_v[i]).c_str(),0)/255.0);
            }
			// cout << ii++ << endl;
        }
        // X_train = X_train/255.0;
        myfile.close();
    }
	// Verschiebe eingelesene Daten in ein "std::array"
	//array<float, 42000*784> X_train;
	// array<float, 42000*10> y_train;

	//std::move(X_train_NN.begin(), X_train_NN.end(), X_train.begin());
	// std::move(y_train_NN.begin(), y_train_NN.end(), y_train.begin());

    // Initialisieren der Schichten
    Conv<NMB_FILTERS, FILTER_DIM, TRAIN_IMAGE_DIM> Conv;
    Maxpool<CONV_OUTPUT_IMG_DIM, MAXPOOL_OUTPUT_IMG_DIM, NMB_FILTERS> Maxpool;

    // Erste FC-Schicht -> Faktor „NMB_FILTERS“ notwendig, da FC von BATCH_SIZE Zeilen ausgeht.
    Fully<MAXPOOL_OUTPUT_IMG_SIZE * NMB_FILTERS, NEURONS_LAYER2> FC_12;
    ReLU<BATCH_SIZE * NEURONS_LAYER2> ReLU_2;

    Fully<NEURONS_LAYER2, NEURONS_LAYER3> FC_23;
    
    SoftMax SoftMax;
    L2_Loss L2_Loss;

    // ==== Datenstrukturen für das Netzwerk ====
    // b_X wird BATCH_SIZE viele Bilder als Zeilen enthalten (wenn man den vector als Matrix interpretiert)
    array<float, BATCH_SIZE * TRAIN_IMAGE_SIZE>  b_X;
    array<float, BATCH_SIZE * NEURONS_LAYER3> b_y;

    // Datenstrukturen für die Vorwärtspropagation
    array<float, BATCH_SIZE * NMB_FILTERS * CONV_OUTPUT_IMG_SIZE>       Conv_ouput;
    array<float, BATCH_SIZE * NMB_FILTERS * MAXPOOL_OUTPUT_IMG_SIZE>    Maxpool_output;
    array<float, BATCH_SIZE * NEURONS_LAYER2>                           FC_12_output;
    array<float, FC_12_output.size()>                                   ReLU_2_output;
    array<float, BATCH_SIZE * NEURONS_LAYER3>                           FC_23_output;
    array<float, FC_23_output.size()>                                   y_hat;
    float loss;

    // Datenstrukturen für die Rückwärtspropagation
    array<float, BATCH_SIZE * NEURONS_LAYER3>                           d_y_hat;
    array<float, BATCH_SIZE * NEURONS_LAYER2>                           FC_23_backward_output;
    array<float, BATCH_SIZE * NEURONS_LAYER2>                           ReLU_2_backward_output;
    array<float, BATCH_SIZE * NMB_FILTERS * MAXPOOL_OUTPUT_IMG_SIZE>    FC_12_backward_output;
    array<float, BATCH_SIZE * NMB_FILTERS * CONV_OUTPUT_IMG_SIZE>       Maxpool_backward_output;
    array<float, BATCH_SIZE * TRAIN_IMAGE_SIZE>                         Conv_backward_output;

	// cout << "Training the model ...\n";

	// const int iterations = 100;

	auto begin = std::chrono::high_resolution_clock::now();
    for (unsigned i = 0; i < ITERATIONS; ++i) {

        // Building batches of input variables (X) and labels (y)
    // Es kann sein, dass nicht alle Bilder oder manche mehrfach für das Training genutzt werden, da der Startindex zufällig ausgewählt wird
    // Um Ergebnisgüte zu erhöhen am besten alle Bilder gleich oft verwenden
        // int randindx = (i * BATCH_SIZE) % (363 - BATCH_SIZE);
        int randindx = rand() % (42000-BATCH_SIZE);
        for (unsigned j = randindx*784; j < (randindx+BATCH_SIZE)*784; ++j){
            b_X[j-randindx*784] = X_train_NN[j];
        }
        for (unsigned k = randindx*10; k < (randindx+BATCH_SIZE)*10; ++k){
            b_y[k-randindx*10] = y_train_NN[k];
        }


        // Vorwärtspropagation
        Conv_ouput      = Conv.forward(b_X);
        Maxpool_output  = Maxpool.forward(Conv_ouput);
        FC_12_output    = FC_12.forward(Maxpool_output);
        ReLU_2_output   = ReLU_2.forward(FC_12_output);
        FC_23_output    = FC_23.forward(ReLU_2_output);
        y_hat           = SoftMax.forward(FC_23_output); 
        loss            = L2_Loss.forward(y_hat, b_y);

        // Rückwärtspropagation
        d_y_hat                 = L2_Loss.backward(y_hat, b_y);
        FC_23_backward_output   = FC_23.backward(d_y_hat);
        ReLU_2_backward_output  = ReLU_2.backward(FC_23_backward_output);
        FC_12_backward_output   = FC_12.backward(ReLU_2_backward_output);
        Maxpool_backward_output = Maxpool.backward(FC_12_backward_output);
        Conv_backward_output    = Conv.backward(Maxpool_backward_output);

        // cout << "[CPU] Epoch " << i << "   Loss: " << 2*loss/BATCH_SIZE << endl;

        // if ((i+1) % 100 == 0){
        //     // cout << "Epoch " << i+1 << endl;
        //     cout << "-----------------------------------------------Epoch " << i+1 << "--------------------------------------------------" <<"\n";
        //     cout << "Predictions:" << "\n";
        //     printMatrix<BATCH_SIZE, NEURONS_LAYER3>(y_hat, 10, 10);
        //     cout << "Ground truth:" << "\n";
        //     printMatrix<BATCH_SIZE, NEURONS_LAYER3>(b_y, 10, 10);
        //     cout << "                                            Loss " << 2*loss/BATCH_SIZE <<"\n";
        //     cout << "--------------------------------------------End of Epoch :(------------------------------------------------" <<"\n";
        // };	
	}
	// file.close();

    auto end = std::chrono::high_resolution_clock::now();

	std::ofstream file;
	file.open("../time_measurements/CPU_Seq_" + std::to_string(ITERATIONS) + "-Iter_" + std::to_string(BATCH_SIZE) + "-Batchsize_" + std::to_string(NMB_FILTERS) + "-Filters.txt", std::ios_base::app);

	auto duration = std::chrono::duration_cast<std::chrono::nanoseconds>(end-begin).count();
	file << duration << "\n";

	// for(int i = 0; i < 11; i++){
	// 	file << arrr[i] << "\n";
	// }
	// file << ">>> NEW MEASUREMENT <<<\n";

	file.close();
	return 0;
}


vector<string> split(const string &s, char delim) {
    std::stringstream ss(s);
    string item;
    vector<string> tokens;
    while (getline(ss, item, delim)) {
        tokens.push_back(item);
    }
    return tokens;
}
