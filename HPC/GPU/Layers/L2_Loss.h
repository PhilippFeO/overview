#pragma once

void forwardWrapper_L2_Loss(float*, float*, float*);
void backwardWrapper_L2_Loss(float*, float*, float*);

struct L2_Loss{
	/*
		Berechnet 1/2 * ||x - y|| ^ 2
	*/
	void forward(float* y_hat, float* labels, float* output) {
		forwardWrapper_L2_Loss(y_hat, labels, output);
	}

	/*
		Berechnet x - y
	*/
	void backward(float* y_hat, float* labels, float* output) {
		backwardWrapper_L2_Loss(y_hat, labels, output);
	}
};