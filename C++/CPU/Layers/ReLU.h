#pragma once

#include<array>

#include "../Network_Config.h"

using std::array;

template<int NMB_ENTRIES>
struct ReLU{
	ReLU();

	array<float, NMB_ENTRIES> input;

    array<float, NMB_ENTRIES> forward(array<float, NMB_ENTRIES>& matrix);
    
	array<float, NMB_ENTRIES> backward(array<float, NMB_ENTRIES>& matrix);
};

template<int NMB_ENTRIES>
ReLU<NMB_ENTRIES>::ReLU(){}

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
template<int NMB_ENTRIES>
array<float, NMB_ENTRIES> ReLU<NMB_ENTRIES>::forward(array<float, NMB_ENTRIES>& matrix) {
	input = matrix;
	array<float, NMB_ENTRIES> result;
	for (int i = 0; i < NMB_ENTRIES; i++) {
		result[i] = matrix[i] <= 0 ? 0 : matrix[i];
	}
	return result;
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
template<int NMB_ENTRIES>
array<float, NMB_ENTRIES> ReLU<NMB_ENTRIES>::backward(array<float, NMB_ENTRIES>& matrix) {
	// TODO evtl. mit std::for_each schneller
	// Eingabe von „forward“ („input“) ableiten und elementweise mit „matrix“ multiplizieren
	array<float, NMB_ENTRIES> result;
	for (int i = 0; i < NMB_ENTRIES; i++) {
		// ReLU'(input)
		result[i] = input[i] <= 0 ? 0 : 1;
		// ReLU'(input) *_elementweise matrix	<-- Eigentliche Rückwärtspropagation
		result[i] = result[i] * matrix[i];
	}
	return result;
}

