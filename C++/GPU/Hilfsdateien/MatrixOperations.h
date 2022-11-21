#pragma once

#include "../Network_Config.h"

#include<iostream>
#include<array>
#include<math.h>
#include<algorithm>
#include<numeric>


/*
	Berechnet das Matrixprodukt A*B
*/
__global__ void matMul(float *A, float *B, float* result, const int rowsOfA, const int columnsOfA, const int columnsOfB);
__global__ void matMul_parallel(float *A, float *B, float* result, const int rowsOfA, const int columnsOfA, const int columnsOfB);


/*
	Transponiere eine Matrix

	Template-Parameter:
	ROWS:		Zeilen der Matrix
	COLUMNS:	Spalten der Matrix
*/
__global__ void transpose(float* matrix, float* transposedMatrix, const int ROWS, const int COLUMNS);
__global__ void transpose_parallel(float *matrix, float *transposedMatrix, const int ROWS, const int COLUMNS);


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
__global__ void scalarMult(const float scalar, float *matrix, const int ROWS, const int COLUMNS);
__global__ void scalarMult_parallel(const float, float*, const int, const int);


/*
	Berechnet die Differenz zweier Matrizen

	Template-Parameter:
	rows:		Zeilen der Matrix
	columns:	Spalten der Matrix
*/
__global__ void matMinus(float *left, float *right, const int ROWS, const int COLUMNS);
__global__ void matMinus_parallel(float *, float *, const int, const int);


__global__ void printMatrix(float *matrix, const int rows, const int columns);

void printMatrix2(float *matrix, const int rows, const int columns);