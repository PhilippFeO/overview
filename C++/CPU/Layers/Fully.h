#pragma once

#include <array>
#include <random>

#include "../Network_Config.h"
#include "MatrixOperations.h"

using std::array;


/*
	Für ERSTE FC-Schicht:
	neurons_in = image_dim * image_dim * #filter

	Die gefilterten Bilder werden also alle eine Zeile geschrieben

	Das muss deswegen so angegeben, da dann die Zeilenanzahl wieder BATCH_SIZE lautet und nicht NMB_FILTERED_ELEMENTS.
	Würde die FC-Schicht NMB_FILTERED_IMAGES Zeilen zulassen, gäbe es am Ende in der Loss-Schicht Probleme, da dort auch „b_y = labels“
	eingesetzt wird und die hat BATCH_SIZE x OUTPUT_LAYER viele Elemente (nicht NMB_FILTERED_ELEMENTS x OUTPUT_LAYER viele)
*/
template<unsigned neurons_in, unsigned neurons_out>
class Fully {
public:
	array<float, neurons_in * neurons_out> W;
	
	/* 	Für Gradientenabstieg in backward benötigt man die Eingabe der Schicht, da
			d_W[ X * W ] = X.T * _
	*/
	array<float, BATCH_SIZE * neurons_in> input;
// public:	
	Fully();
	
	array<float, BATCH_SIZE * neurons_out> forward(const array<float, BATCH_SIZE * neurons_in>&);
	
	array<float, BATCH_SIZE * neurons_in> backward(const array<float, BATCH_SIZE * neurons_out>&);
	
	// void subtract(array<float, neurons_in * neurons_out>&, const float);
};

template<unsigned neurons_in, unsigned neurons_out>
Fully<neurons_in, neurons_out>::Fully() {
	std::normal_distribution<float> distribution(0, sqrt(2.0 / (neurons_in + neurons_out)));
	static std::default_random_engine generator;
	for (unsigned i = 0; i < W.size(); ++i) {
		W[i] = distribution(generator);
	}
}

/*
	Fully Connected-Schicht f�r Vorw�rtspropagation.
	Bildet Eingabe X der Schicht i per Gewichtsmatrix W_ij auf Schicht i+1 ab.

	Matrixprodukt der Matrizen X und W_ij.

	Template-Parameter:
	BATCH_SIZEOfX: 		Anzahl der Zeilen von X == Anzahl der Bilder, die in einem Durchgang bearbeitet werden sollen
	columnsOfX: 	Anzahl der Spalten von X == Anzahl der Zeilen von B == Anzahl der Neuronen von Schicht i
	columnsOfW_ij: 	Anzahl der Spalten von W_ij == Anzahl der Neuronen von Schicht i+1

	Funktionsparameter
	X:			Eingabe f�r die Schicht
	W_ij:			Gewichte der Schicht

	R�ckgabe:
	Matrixprodukt von X und W_ij.
*/

template<unsigned neurons_in, unsigned neurons_out>
array<float, BATCH_SIZE* neurons_out> Fully<neurons_in, neurons_out>::forward(const array<float, BATCH_SIZE* neurons_in>& in) {
	// Eingabe speicher für „backward()“
	input = in;
	array<float, BATCH_SIZE* neurons_out> out;
	for (int r = 0; r < BATCH_SIZE; ++r) {
		for (int columnW = 0; columnW < neurons_out; ++columnW) {
			float res = 0.0f;
			for (int c = 0; c < neurons_in; ++c) {
				res += in[r * neurons_in + c] * W[c * neurons_out + columnW];
			}
			out[r * neurons_out + columnW] = res;
		}
	}
	return out;
}

/*
	Fully Connected-Schicht f�r R�ckw�rtspropagation.

		d_W [ X * W ] = X.T * _
		d_X [ X * W ] = _ * W.T

	Matrixprodukt der Matrizen E_n und W_ij.T

	Template-Parameter:
	BATCH_SIZEOfE_n: 		Anzahl der Zeilen von E_n == Anzahl der Bilder, die in einem Durchgang bearbeitet werden sollen
	columnsOfE_n: 	Anzahl der Spalten von E_n == Anzahl der Zeilen von E_n == Anzahl der Neuronen von Schicht i
	columnsOfW_ij: 	Anzahl der Spalten von W_ij == Anzahl der Neuronen von Schicht i+1

	Funktionsparameter
	E_n:			Eingabe f�r die Schicht
	W_ij:			Gewichte der Schicht

	R�ckgabe:
	Matrixprodukt von E_n und W_ij.T
*/

template<unsigned neurons_in, unsigned neurons_out>
array<float, BATCH_SIZE* neurons_in> Fully<neurons_in, neurons_out>::backward(const array<float, BATCH_SIZE* neurons_out>& error_tensor) {
	// Ableitung bzgl. der Eingabe
	array<float, neurons_out* neurons_in> W_transposed = transpose<neurons_in, neurons_out>(W);
	array<float, BATCH_SIZE* neurons_in> d_input;
	for (int r = 0; r < BATCH_SIZE; ++r) {
		for (int columnW = 0; columnW < neurons_in; ++columnW) {
			float res = 0.0f;
			for (int c = 0; c < neurons_out; ++c) {
				res += error_tensor[r * neurons_out + c]
						* W_transposed[c * neurons_in + columnW];
			}
			d_input[r * neurons_in + columnW] = res;
		}
	}

	// Gradientenabstieg
	// Ableitung bzgl. der Gewichte: d_W = X.T * _
	array<float, BATCH_SIZE * neurons_in>
			d_W = transpose<BATCH_SIZE, 
							neurons_in>(
								input);	
	// W = W - learning_rate * d_W * error_tensor
	// error_tensor mithilfe der Ableitung abbilden
	array<float, neurons_in * neurons_out> 
		d_W_error_tensor_applied
			= matMul<	neurons_in,
						BATCH_SIZE,
						neurons_out>(
							d_W,
							error_tensor);
	const int d_W_error_tensor_applied_size = d_W_error_tensor_applied.size();
	W = matMinus<	neurons_in, 
					neurons_out>(
						W,
						scalarMult<d_W_error_tensor_applied_size>(
							learning_rate,
							d_W_error_tensor_applied));
	return d_input;
}

// template<unsigned neurons_in, unsigned neurons_out>
// void Fully<neurons_in, neurons_out>::subtract(array<float, neurons_in* neurons_out>& dW, const float learning_rate) {
// 	W = matMinus<neurons_in, neurons_out>(W, scalarMult<neurons_in* neurons_out>(learning_rate, dW));
// }