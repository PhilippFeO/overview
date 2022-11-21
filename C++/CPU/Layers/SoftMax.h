#pragma once

#include<array>
using std::array;

#include "../Network_Config.h"

struct SoftMax{
	SoftMax();
    
	array<float, BATCH_SIZE * NEURONS_LAYER3> forward(array<float, BATCH_SIZE * NEURONS_LAYER3>& matrix);
};

SoftMax::SoftMax() {}

/*
	Wendet SoftMax zeilenweise auf eine Matrix an. Davor wird jedes Element um das zeilenweise Maximum nach links verschoben.

	Template-Parameter:
	BATCH_SIZE:		Zeilen der Matrix
	NEURONS_LAYER3:	Spalten der Matrix

	Funktionsparameter:
	matrix:	Matrix für die SoftMax wie oben beschrieben angewandt werden soll.

	Rückgabewert:
	Matrix für die SoftMax wie oben beschrieben angwandt wurde.
*/
array<float, BATCH_SIZE * NEURONS_LAYER3> SoftMax::forward(array<float, BATCH_SIZE * NEURONS_LAYER3>& matrix) {
	// Für bessere Stabilität (in der Exponentialfunktion) verschiebe jede Eingabe/jede Zeile um ihr Maximum
	array<float, BATCH_SIZE * NEURONS_LAYER3> result;
	for (int r = 0; r < BATCH_SIZE; r++) {
		// Zeilenwieses Maximum berechnen (gibt Iterator zurück, der dereferenziert werden muss)
		const float* BATCH_SIZEtart = &matrix[r * NEURONS_LAYER3];
		//*rowEnd = &matrix[(r+1) * NEURONS_LAYER3];
		const float max = *max_element(BATCH_SIZEtart, BATCH_SIZEtart + NEURONS_LAYER3);
		// Werte in jeder Zeile um zeilenweises Maximum verscheiben
		for (int c = 0; c < NEURONS_LAYER3; c++) {
			matrix[r * NEURONS_LAYER3 + c] -= max;
		}

		// Ab hier beginnt die eigentliche SoftMax-Abbildung
		float sumOfRow = 0;
		for (int i = 0; i < NEURONS_LAYER3; i++) {
			sumOfRow += exp(matrix[r * NEURONS_LAYER3 + i]);
		}
		for (int c = 0; c < NEURONS_LAYER3; c++) {
			result[r * NEURONS_LAYER3 + c] = exp(matrix[r * NEURONS_LAYER3 + c]) / sumOfRow;
		}
	}
	return result;
}
