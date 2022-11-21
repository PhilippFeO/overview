#pragma once

#include<array>

#include "MatrixOperations.h"
#include "../Network_Config.h"


using std::array;

struct L2_Loss{
	L2_Loss();
    
	float forward(const array<float, BATCH_SIZE * NEURONS_LAYER3>& y_hat, array<float, BATCH_SIZE * NEURONS_LAYER3>& labels);
    
	array<float, BATCH_SIZE * NEURONS_LAYER3> backward(array<float, BATCH_SIZE * NEURONS_LAYER3>& y_hat, array<float, BATCH_SIZE * NEURONS_LAYER3>& labels);
};

L2_Loss::L2_Loss(){}

/*
	Berechnet 1/2 * ||x - y|| ^ 2
*/
float L2_Loss::forward(const array<float, BATCH_SIZE * NEURONS_LAYER3>& y_hat, array<float, BATCH_SIZE * NEURONS_LAYER3>& labels) {
	array<float, BATCH_SIZE * NEURONS_LAYER3> difference = matMinus<BATCH_SIZE, NEURONS_LAYER3>(y_hat, labels);
	float res = 0;
	for (int r = 0; r < BATCH_SIZE; r++) {
		for (int c = 0; c < NEURONS_LAYER3; c++) {
			res += 0.5 * difference[r * NEURONS_LAYER3 + c] * difference[r * NEURONS_LAYER3 + c];
		}
	}
	return res;
}

/*
	Berechnet x - y
*/
array<float, BATCH_SIZE * NEURONS_LAYER3> L2_Loss::backward(array<float, BATCH_SIZE * NEURONS_LAYER3>& y_hat, array<float, BATCH_SIZE * NEURONS_LAYER3>& labels) {
	return matMinus<BATCH_SIZE, NEURONS_LAYER3>(y_hat, labels);
}