#pragma once

#include<array>

#include "../Network_Config.h"
#include "../Hilfsdateien/CudaHilfsfunktionen.h"

using std::array;

void forwardWrapper_ReLU(float* layer_input, float *output, const int ROWS, int COLUMNS);
void backwardWrapper_ReLU(float *error_tensor, float *layer_input, float *d_input, const int ROWS, int COLUMNS);

template<int ROWS, int COLUMNS>
struct ReLU{
	ReLU();

	float *layer_input;

	void forward(float*, float*);
    
	void backward(float*, float*);
};

template<int ROWS, int COLUMNS>
ReLU<ROWS, COLUMNS>::ReLU(){}

/*
	Wendet ReLU elementweise auf eine Matrix an.

	Template-Parameter:
	nmbElements: Anzahl der Elemente in der Matrix.

	Funktionparameter:
	matrix - Matrix, auf die ReLU angewandt werden soll.

	Rückgabewerte:
	matrix - Matrix, auf die ReLU angewandt wurde.

	Optional: nicht „0“ sondern „x*matrix[i]“ mit x € [-1, 0] zurückgeben
*/
template<int ROWS, int COLUMNS>
void ReLU<ROWS, COLUMNS>::forward(float* input, float* output){
	layer_input = input;
	forwardWrapper_ReLU(layer_input, output, ROWS, COLUMNS);	
}


/*
	Wendet ReLU' (die Ableitung von ReLU) elementweise auf eine Matrix an.

	Template-Parameter:
	nmbElements: Anzahl der Elemente in der Matrix.

	Funktionparameter:
	matrix - Matrix, auf die ReLU' angewandt werden soll.

	Rückgabewerte:
	matrix - Matrix, auf die ReLU' angewandt wurde.
*/
template<int ROWS, int COLUMNS>
void ReLU<ROWS, COLUMNS>::backward(float* error_tensor, float *d_input){
	backwardWrapper_ReLU(error_tensor, layer_input, d_input, ROWS, COLUMNS);
}

