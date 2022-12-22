#include "MatrixOperations.h"

using std::cout;
using std::endl;
using std::array;

// Für SoftMax-Abbildung
using std::max_element;
using std::exp;

/*
	Berechnet das Matrixprodukt A*B
*/
__global__ void matMul(float *A, float *B, float* result, const int rowsOfA, const int columnsOfA, const int columnsOfB){
	for (int r = 0; r < rowsOfA; r++) {
		for (int col = 0; col < columnsOfB; col++) {
			float res = 0.0f;
			for (int k = 0; k < columnsOfA; k++) {
				res += A[r * columnsOfA + k] * B[k * columnsOfB + col];
			}
			result[r * columnsOfB + col] = res;
		}
	}
}

__global__ void matMul_parallel(float *A, float *B, float* result, const int rowsOfA, const int columnsOfA, const int columnsOfB){
	/*
		Die Ausgabe-Matrix hat bspw r*c-Elemente. Für jedes dieser wird ein Thread gestartet.
		Da man pro Block maximal 1024 Threads nutzen kann, müssen ggfl. weitere Blöcke hinzugenommen werden.
		In diesem Fall muss der Index des Threads erst aus den Block- & Thread-Koordinaten berechnet werden.
	*/
	int row = blockIdx.x;
	int column = blockIdx.y * MAX_THREADS + threadIdx.x;
	if (row < rowsOfA && column < columnsOfB) {
		result[row * columnsOfB + column] = 0;
		for (int i = 0; i < columnsOfA; i++) {
			result[row * columnsOfB + column] += A[row * columnsOfA + i] * B[i * columnsOfB + column];
		}
	}
}

/*
	Transponiere eine Matrix

	Template-Parameter:
	ROWS:		Zeilen der Matrix
	COLUMNS:	Spalten der Matrix
*/
__global__ void transpose(float* matrix, float* transposedMatrix, const int ROWS, const int COLUMNS){
	for (int row = 0; row < ROWS; row++) {
		for (int column = 0; column < COLUMNS; column++) {
			transposedMatrix[column * ROWS + row] = matrix[row * COLUMNS + column];
		}
	}
}
__global__ void transpose_parallel(float *matrix, float *transposedMatrix, const int ROWS, const int COLUMNS){
	// const int row = blockIdx.x;
	// const int column = threadIdx.x;
	// transposedMatrix[column * ROWS + row] = matrix[row * COLUMNS + column];	
	const int row = blockIdx.x;
	const int column = blockIdx.y * MAX_THREADS + threadIdx.x;	// Jede Reihe wird in "blockIdx.y" 1024 Elemente-lange Sequenzen unterteilt (außer evtl. der Rest)
	if(row < ROWS && column < COLUMNS){	// Der Index muss innerhalb einer Reihe liegen, dh. die berechnete Spalte darf nicht größer als die der Matrix sein
		transposedMatrix[column * ROWS + row] = matrix[row * COLUMNS + column];
	}
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
__global__ void scalarMult(const float scalar, float *matrix, const int ROWS, const int COLUMNS) {
	for (int i = 0; i < ROWS * COLUMNS; i++) {
		matrix[i] = scalar * matrix[i];
	}
}
__global__ void scalarMult_parallel(const float scalar, float *matrix, const int ROWS, const int COLUMNS) {
	const int row = blockIdx.x;
	const int column = threadIdx.x;
	matrix[row * COLUMNS + column] = scalar * matrix[row * COLUMNS + column];
}


/*
	Berechnet die Differenz zweier Matrizen

	Template-Parameter:
	rows:		Zeilen der Matrix
	columns:	Spalten der Matrix
*/
__global__ void matMinus(float *left, float *right, const int ROWS, const int COLUMNS) {
	for (int i = 0; i < ROWS * COLUMNS; i++) {
		left[i] = left[i] - right[i];
	}
}
__global__ void matMinus_parallel(float *left, float *right, const int ROWS, const int COLUMNS) {
	const int row = blockIdx.x;
	const int column = threadIdx.x;
	const int idx = row * COLUMNS + column;
	left[idx] = left[idx] - right[idx];
}


__global__ void printMatrix(float *matrix, const int rows, const int columns){
	for (int r = 0; r < rows; r++) {
		for (int c = 0; c < columns; c++) {
			float tmp = matrix[r * columns + c];
			// Da y_hat in SoftMax überschrieben wird, steht dort durch das Label eine negative Zahl.
			// Dies wird hier für die Ausgabe korrigiert.
			printf("%.4f ", (tmp < 0 ? tmp + 1 : tmp));
		}
		printf("\n");
	}
}

void printMatrix2(float *matrix, const int rows, const int columns){
	for (int r = 0; r < rows; r++) {
		for (int c = 0; c < columns; c++) {
			float tmp = matrix[r * columns + c];
			// Da y_hat in SoftMax überschrieben wird, steht dort durch das Label eine negative Zahl.
			// Dies wird hier für die Ausgabe korrigiert.
			printf("%.4f ", (tmp < 0 ? tmp + 1 : tmp));
		}
		printf("\n");
	}
}