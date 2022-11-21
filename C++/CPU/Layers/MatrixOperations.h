#pragma once

#include<iostream>
#include<array>
#include<math.h>
#include<algorithm>
#include<numeric>

#define NMB_ELEMENTS (rows * columns)

using std::cout;
using std::endl;
using std::array;

// Für SoftMax-Abbildung
using std::max_element;
using std::exp;


/*
	Zeigt eine Matrix im Terminal an.

	Template-Parameter:
	rows:		Zeilen der Matrix
	columns:	Spalten der Matrix

	Funktionsparameter:
	matrix:	Matrix die ausgegeben werden soll.
*/


/*
	row2 = Bis zu welcher Reihe geprintet werden soll. 0 = Ende
	columns2 = Analog zu „rows2“
*/
template<int rows, int columns>
void printMatrix(const array<float, NMB_ELEMENTS>& matrix, const int rows2, const int columns2){
	int rEnd = rows2 == 0 ? rows : rows2;
	int cEnd = columns2 == 0 ? columns : columns2;
	char buff[100];
	for (int r = 0; r < rEnd; r++) {
		for (int c = 0; c < cEnd; c++) {
			snprintf(buff, sizeof(buff), "%.4f  ", matrix[r * columns + c]);
			std::string buffAsStdStr = buff;
			cout << buffAsStdStr;
			// cout << matrix[r * columns + c] << " ";
		}
		cout << endl;
	}
}

/*
	Matrixmultiplikation der Matrizen A und B
*/

template<int rowsOfA, int columnsOfX, int columnsOfB>
array<float, rowsOfA* columnsOfB> matMul(const array<float, rowsOfA* columnsOfX>& X, const array<float, columnsOfX* columnsOfB>& W_ij) {
	
	array<float, rowsOfA* columnsOfB> result;
	for (int r = 0; r < rowsOfA; r++) {
		for (int columnW = 0; columnW < columnsOfB; columnW++) {
			float res = 0.0f;
			for (int c = 0; c < columnsOfX; c++) {
				res += X[r * columnsOfX + c] * W_ij[c * columnsOfB + columnW];
			}
			result[r * columnsOfB + columnW] = res;
		}
	}
	return result;
}

/*
	Berechnet die Differenz zweier Matrizen

	Template-Parameter:
	rows:		Zeilen der Matrix
	columns:	Spalten der Matrix
*/

template<int rows, int columns>	// NMB_ELEMENTS = rows * columns
array<float, NMB_ELEMENTS> matMinus(const array<float, NMB_ELEMENTS>& left, const array<float, NMB_ELEMENTS>& right) {
	array<float, NMB_ELEMENTS> difference;
	for (int i = 0; i < NMB_ELEMENTS; i++) {
		difference[i] = left[i] - right[i];
	}
	return difference;
}

/*
	Transponiere eine Matrix

	Template-Parameter:
	rows:		Zeilen der Matrix
	columns:	Spalten der Matrix
*/

template<int rows, int columns>		// NMB_ELEMENTS = rows * columns
array<float, NMB_ELEMENTS> transpose(const array<float, NMB_ELEMENTS>& matrix) {
	array<float, NMB_ELEMENTS> transposed;
	for (int row = 0; row < rows; row++) {
		for (int column = 0; column < columns; column++) {
			transposed[column * rows + row] = matrix[row * columns + column];
		}
	}
	// for(unsigned n = 0; n != columns*rows; n++) {
	//     unsigned i = n/columns;
	//     unsigned j = n%columns;
	//     transposed[n] = matrix[rows*j + i];
	// }
	return transposed;
}


/*
	Skalare Multiplikation

	Template-Parameter:
	nmbElements: Anzahl der Elemente beider Matrizen

	Funktionsparameter:
	scalar: float -- Skalar mit dem multipliziert werden soll
	matrix: array<float, nmbElements> -- Matrix, deren Einträge multipliziert werden sollen

	Rückgabewert:
	result: array<float, nmbElements> -- Die skalarmultiplizierte Matrix
*/

template<int nmbElements>
array<float, nmbElements> scalarMult(const float scalar, array<float, nmbElements>& matrix) {
	array<float, nmbElements> result;
	for (int i = 0; i < nmbElements; i++) {
		result[i] = scalar * matrix[i];
	}
	return result;
}

/*
	Elementweise Multiplikation zweier Matrizen

	Template-Parameter:
	nmbElements: Anzahl der Elemente beider Matrizen
*/

template<int nmbElements>
array<float, nmbElements> elementwiseMult(array<float, nmbElements>& left, array<float, nmbElements>& right) {
	array<float, nmbElements> result;
	for (int i = 0; i < nmbElements; i++) {
		result[i] = left[i] * right[i];
	}
	return result;
}