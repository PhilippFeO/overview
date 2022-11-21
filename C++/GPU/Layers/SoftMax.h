#pragma once

#include<array>
using std::array;

#include "../Network_Config.h"

void forwardWrapper_SoftMax(float*, float*);

class SoftMax{

public:
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
	void forward(float* input, float* output) {
		forwardWrapper_SoftMax(input, output);
	}
};
