#pragma once

#include <array>
#include <iostream>

// #include "Conv_Variables.h"

#include "../Network_Config.h"

#define INPUT_IMAGE_SIZE (INPUT_IMG_DIM * INPUT_IMG_DIM)
#define OUTPUT_IMAGE_SIZE (OUTPUT_IMG_DIM * OUTPUT_IMG_DIM)

using std::array;

template<unsigned INPUT_IMG_DIM, unsigned OUTPUT_IMG_DIM, unsigned NMB_FILTERS>
class Maxpool {
	array<int, OUTPUT_IMAGE_SIZE * BATCH_SIZE * NMB_FILTERS> max_pool_idx;
public:
	Maxpool();
	array<float, OUTPUT_IMAGE_SIZE * BATCH_SIZE * NMB_FILTERS> forward(const array<float, INPUT_IMAGE_SIZE * BATCH_SIZE * NMB_FILTERS>&);
	array<float, INPUT_IMAGE_SIZE * BATCH_SIZE * NMB_FILTERS> backward(const array<float, OUTPUT_IMAGE_SIZE * BATCH_SIZE * NMB_FILTERS>&);
	float GetMax(const float, const float, const float, const float);
};

template<unsigned INPUT_IMG_DIM, unsigned OUTPUT_IMG_DIM, unsigned NMB_FILTERS>
Maxpool<INPUT_IMG_DIM, OUTPUT_IMG_DIM, NMB_FILTERS>::Maxpool() {}

template<unsigned INPUT_IMG_DIM, unsigned OUTPUT_IMG_DIM, unsigned NMB_FILTERS>
array<float, OUTPUT_IMAGE_SIZE * BATCH_SIZE * NMB_FILTERS> Maxpool<INPUT_IMG_DIM, OUTPUT_IMG_DIM, NMB_FILTERS>::forward(const array<float, INPUT_IMAGE_SIZE * BATCH_SIZE * NMB_FILTERS>& matrix) {
	array<float, OUTPUT_IMAGE_SIZE * BATCH_SIZE * NMB_FILTERS> result;
	const int nmbOfPictures = BATCH_SIZE * NMB_FILTERS;
	int nextBlock, nextRow, nextMatrix;
	int counter = 0;

	for (int i = 0; i < nmbOfPictures; i++) {
		for (int r = 0; r < INPUT_IMG_DIM; r += 2) {
			for (int c = 0; c < INPUT_IMG_DIM; c += 2) {
				nextMatrix = i * INPUT_IMAGE_SIZE;
				nextBlock = c + r * INPUT_IMG_DIM + nextMatrix;
				nextRow = c + (r + 1) * INPUT_IMG_DIM + nextMatrix;

				float x1 = matrix[nextBlock];
				float x2 = matrix[nextBlock + 1];
				float x3 = matrix[nextRow];
				float x4 = matrix[nextRow + 1];

				result[counter] = GetMax(x1, x2, x3, x4);

				if (result[counter] == x1)      { max_pool_idx[counter] = nextBlock; }
				else if (result[counter] == x2) { max_pool_idx[counter] = nextBlock + 1; }
				else if (result[counter] == x3) { max_pool_idx[counter] = nextRow; }
				else { //result[counter] == x4
					max_pool_idx[counter] = nextRow + 1;
				}

				counter++;
			}
		}
	}

	return result;
}

template<unsigned INPUT_IMG_DIM, unsigned OUTPUT_IMG_DIM, unsigned NMB_FILTERS>
array<float, INPUT_IMAGE_SIZE * BATCH_SIZE * NMB_FILTERS> Maxpool<INPUT_IMG_DIM, OUTPUT_IMG_DIM, NMB_FILTERS>::backward(const array<float, OUTPUT_IMAGE_SIZE * BATCH_SIZE * NMB_FILTERS>& matrix) {
	array<float, INPUT_IMAGE_SIZE * BATCH_SIZE * NMB_FILTERS> result = { 0 };
	for (int i = 0; i < max_pool_idx.size(); i++) {
		result[max_pool_idx[i]] = matrix[i];
	}
	return result;
}

template<unsigned INPUT_IMG_DIM, unsigned OUTPUT_IMG_DIM, unsigned NMB_FILTERS>
float Maxpool<INPUT_IMG_DIM, OUTPUT_IMG_DIM, NMB_FILTERS>::GetMax(const float x1, const float x2, const float x3, const float x4) {
	float tmp = x1;
	if (tmp < x2) tmp = x2;
	if (tmp < x3) tmp = x3;
	if (tmp < x4) tmp = x4;
	return tmp;
}