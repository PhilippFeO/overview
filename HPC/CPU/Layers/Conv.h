#pragma once

#include<array>
#include<vector>
#include<algorithm>
#include<iostream>
#include<iterator>
#include<string>
#include <random>

#include "../Network_Config.h"

#define NMB_ELEMENTS_CONV (ROWS * COLUMNS)

#define FILTER_SIZE FILTER_DIM * FILTER_DIM
#define INPUT_IMG_SIZE INPUT_IMG_DIM * INPUT_IMG_DIM
#define OUTPUT_IMG_DIM (INPUT_IMG_DIM - FILTER_DIM + 1)
#define OUTPUT_IMG_SIZE OUTPUT_IMG_DIM * OUTPUT_IMG_DIM

using std::array;
using std::vector;
using std::cout;
using std::endl;
using std::for_each;
using std::string;

template<unsigned FILTERS, unsigned FILTER_DIM, unsigned INPUT_IMG_DIM>
class Conv {
	// Gewichte der Schicht
	array<float, FILTERS * FILTER_SIZE> filters;
	array<float, FILTERS * FILTER_SIZE> d_filters;
	// Eingabe der Schicht (wird in „backward()“ benätigt)
	array<float, BATCH_SIZE* INPUT_IMG_DIM* INPUT_IMG_DIM> layer_input;	// 28 * 28 * BATCH_SIZE

///////////// HILFSFUNKTIONEN ////////////////////////////////
	
	array<float, OUTPUT_IMG_SIZE> correlate(const array<float, INPUT_IMG_SIZE>& x, const array<float, FILTER_SIZE>& filter, const int filter_dim){
		const int ROWS = OUTPUT_IMG_DIM;
		const int COLUMNS = ROWS;
		array<float, ROWS * COLUMNS> result;
		for (unsigned r = 0; r < ROWS; r++) {
			for (unsigned c = 0; c < COLUMNS; c++) {
				// result[r * COLUMNS + c]
				// 	= x[r 		* INPUT_IMG_DIM + c		] * filter[0]
				// 	+ x[r 		* INPUT_IMG_DIM + c + 1	] * filter[1]
				// 	+ x[r 		* INPUT_IMG_DIM + c + 2	] * filter[2]
				// 	+ x[(r + 1) * INPUT_IMG_DIM + c		] * filter[3]
				// 	+ x[(r + 1) * INPUT_IMG_DIM + c + 1	] * filter[4]
				// 	+ x[(r + 1) * INPUT_IMG_DIM + c + 2	] * filter[5]
				// 	+ x[(r + 2) * INPUT_IMG_DIM + c		] * filter[6]
				// 	+ x[(r + 2) * INPUT_IMG_DIM + c + 1	] * filter[7]
				// 	+ x[(r + 2) * INPUT_IMG_DIM + c + 2	] * filter[8];
				float res = 0;
				for(int rr = 0; rr < FILTER_DIM; rr++){
					for(int cc = 0; cc < FILTER_DIM; cc++){
						res += x[ (r + rr) * INPUT_IMG_DIM + (c + cc) ] * filter[ rr * FILTER_DIM + cc ];
					}
				}
				result[r * COLUMNS + c] = res;
			}
		}
		return result;
	}


	template<int ROWS, int COLUMNS>
	array<float, NMB_ELEMENTS_CONV> getPartialImage(const array<float, INPUT_IMG_SIZE>& image, const int start_row, const int start_col){
		array<float, NMB_ELEMENTS_CONV> result;
		for(int r = 0; r < ROWS; r++){
			for(int c = 0; c < COLUMNS; c++){
				result[r * COLUMNS + c ] = image[(start_row + r) * INPUT_IMG_DIM + (start_col + c)];
			}
		}
		return result;
	}


	template<int IMG_SIZE, int NMB_IMAGES_IN_BATCH>
	array<float, IMG_SIZE> getImage(const array<float, NMB_IMAGES_IN_BATCH * IMG_SIZE>& batch, const int image_index){
		array<float, IMG_SIZE> image;
		for(int i = 0; i < IMG_SIZE; i++){
			image[i] = batch[image_index * IMG_SIZE + i];
		}
		return image;
	}


	template<int ROWS, int COLUMNS>
	void print ( const array<float, NMB_ELEMENTS_CONV>& m, const string message) {
		/*  "Couts" the input vector as n_rows x n_columns matrix.
		Inputs:
		m: vector, matrix of size n_rows x n_columns
		n_rows: int, number of rows in the left matrix m1
		n_columns: int, number of columns in the left matrix m1
		*/
		char buff[10];
		if(!message.empty()){
			cout << message << endl;
		}
		for( int i = 0; i != ROWS; ++i ) {
			for( int j = 0; j != COLUMNS; ++j ) {
				snprintf(buff, sizeof(buff), "%.4f  ", m[i * COLUMNS + j]);
				std::string buffAsStdStr = buff;
				cout << buffAsStdStr;
				// cout << m[ i * COLUMNS + j ] << " ";
			}
			cout << '\n';
		}
		cout << endl;
	}


	template<int NMB_ENTRIES>
	array<float, NMB_ENTRIES> scalarMult(const float scalar, const array<float, NMB_ENTRIES>& matrix){
		array<float, NMB_ENTRIES> result;
		for(int i = 0; i < NMB_ENTRIES; i++) result[i] = scalar * matrix[i];
		return result;
	}


	template<int NMB_ENTRIES>
	array<float, NMB_ENTRIES> matMinus(const array<float, NMB_ENTRIES>& left, const array<float, NMB_ENTRIES>& right){
		array<float, NMB_ENTRIES> result;
		for(int i = 0; i < NMB_ENTRIES; i++) result[i] = left[i] - right[i];
		return result;
	}


	/*
		Setzt die Matrix „matrix“ an die per „row“ und „column“ definierte Stelle.
		„matrix“ ist dabei ein mit einem Skalar multiplitzierter Filter.
	*/
	void add_to_layer_derivative(	array<float, INPUT_IMG_SIZE * BATCH_SIZE>& d_input,
									const array<float, FILTER_SIZE>& matrix,
									const int image_index,
									const int row,
									const int column){
		for(int r = 0; r < FILTER_DIM; r++){
			for(int c = 0; c < FILTER_DIM; c++){
				d_input[	image_index * INPUT_IMG_SIZE
							+ (row + r) * INPUT_IMG_DIM
							+ (column + c)]
					+= matrix[r * FILTER_DIM + c];
			}
		}
	}


	void add_to_filter_derivative(array<float, FILTERS * FILTER_SIZE>& d_filters, const array<float, FILTER_SIZE>& matrix, const int filter_position){
		for(int i = 0; i < FILTER_SIZE; i++){
			d_filters[filter_position * FILTER_SIZE + i] += matrix[i];
		}
	}


	array<float, FILTER_SIZE> extractFilter(const array<float, FILTERS * FILTER_SIZE>& filters, const int filter_index){
		array<float, FILTER_SIZE> filter;
		for(int i = 0; i < FILTER_SIZE; i++){
			filter[i] = filters[filter_index * FILTER_SIZE + i];
		}
		return filter;
	}


///////////// FORWARD, BACKWARD, TESTFUNKTIONEN ///////

public:
	Conv(){
		std::default_random_engine gen;
		std::normal_distribution<float> dis(0, sqrt(2.0 / (2.0 * FILTER_SIZE)));
		for (unsigned i = 0; i < FILTERS; ++i) {
			for (unsigned j = 0; j < FILTER_SIZE; ++j) {
				filters[i * FILTER_SIZE + j] = dis(gen);
			}
		}
	}

///////////// FORWARD /////////////////////////////////
	/*
	„output“ hat input×filters viele Elemente bzw. für jedes Bild „filters“ viele gefilterten Elemente
	Struktur:
		Filter_1(Bild_1) Filter_2(Bild_1) ... Filter_n(Bild_1)
		Filter_1(Bild_2) ...
		...
		Filter_1(Bild_m) Filter_2(Bild_m) ... Filter_n(Bild_m)
	*/
	array<float, FILTERS* (BATCH_SIZE* OUTPUT_IMG_SIZE)> forward(
		array<float, BATCH_SIZE* INPUT_IMG_SIZE>& input) {
		// Eingabe für „backward“ speichern
		layer_input = input;
		array<float, FILTERS* (BATCH_SIZE* OUTPUT_IMG_SIZE)> output;
		/*
			Erst über Bilder iterieren, da die Conv-Schicht eigentlich 1 Bild mit N Filtern bearbeitet
			(So ist näher an der konzeptionellen Idee)
		*/
		for (int image_index = 0; image_index < BATCH_SIZE; image_index++) {
			array<float, INPUT_IMG_SIZE>
				image = getImage<	INPUT_IMG_SIZE,
				BATCH_SIZE>(
					input,
					image_index);
			for (int filter_index = 0; filter_index < FILTERS; filter_index++) {
				array<float, OUTPUT_IMG_SIZE> filtered_image;
				array<float, FILTER_SIZE> filter = extractFilter(filters, filter_index);
				filtered_image = correlate(image, filter, FILTER_DIM);
				// print<OUTPUT_IMG_DIM, OUTPUT_IMG_DIM>(filtered_image, "[forward] filtered_image:");
				// Gefiltertes Bild in Ausgabedatenstruktur einfügen
				for (int i = 0; i < filtered_image.size(); i++) {
					// cout << "output.size() = " << output.size() << endl;
					// printf("OUTPUT_IMG_SIZE = %d   FILTERS = %d\n", OUTPUT_IMG_SIZE, FILTERS);
					// printf("image_index = %d   filter_index = %d   i = %d\n", image_index, filter_index, i);
					output[image_index * (FILTERS * OUTPUT_IMAGE_SIZE)
						+ filter_index * OUTPUT_IMG_SIZE
						+ i]
						= filtered_image[i];
				}
			}
		}
		return output;
	}


///////////// BACKWARD ////////////////////////////////
		array<float, BATCH_SIZE * INPUT_IMG_SIZE> backward(
			array<float, FILTERS * (BATCH_SIZE * OUTPUT_IMG_SIZE)>& error_tensor){
		
		// Wird für weitere Rückwärtspropagation benötigt
		array<float, BATCH_SIZE * INPUT_IMG_SIZE> d_input;
		// Ableitung jedes Filters mit 0-Vektor initialisieren = d_L_d_filters = np.zeros(self.filters.shape)
		for(int i = 0; i < d_filters.size(); ++i) d_filters[i] = 0;
		// Ableitungsdatenstruktur mit 0 initialisieren
		for(int i = 0; i < d_input.size(); ++i) d_input[i] = 0; //Gibts bei cnn.py nicht, da nur ein Layer

		for (int layer_input_image_index = 0; layer_input_image_index < BATCH_SIZE; ++layer_input_image_index) {
			for (int r = 0; r < OUTPUT_IMG_DIM; ++r) {
				for (int c = 0; c < OUTPUT_IMG_DIM; ++c) {
					for (int filter_index = 0; filter_index < FILTERS; ++filter_index) {
						// Gradient bzgl. der Gewichte
						for (int rr = 0; rr < FILTER_DIM; rr++) {
							for (int cc = 0; cc < FILTER_DIM; cc++) {
								d_filters[filter_index * FILTER_SIZE + FILTER_DIM * rr + cc] +=
									error_tensor[layer_input_image_index * FILTERS * OUTPUT_IMAGE_SIZE + filter_index * OUTPUT_IMAGE_SIZE + r * OUTPUT_IMG_DIM + c] *
									layer_input[INPUT_IMG_SIZE * layer_input_image_index + (r + rr) * INPUT_IMG_DIM + (c + cc)];
							}
						}						 
						// Gradient bezüglich der Eingabe
						for (int rr = 0; rr < FILTER_DIM; rr++) {
							for (int cc = 0; cc < FILTER_DIM; cc++) {
								d_input[layer_input_image_index * INPUT_IMG_SIZE + (r + rr) * INPUT_IMG_DIM + (c + cc)] +=
									error_tensor[layer_input_image_index * FILTERS * OUTPUT_IMAGE_SIZE + filter_index * OUTPUT_IMAGE_SIZE + r * OUTPUT_IMG_DIM + c] *
									filters[filter_index * FILTER_SIZE + rr * FILTER_DIM + cc];
							}
						}
					}
				}
			}
		}		 
		 
		// Gradientenabstieg: Filter - learning_rate * d_Filter
		for (int i = 0; i < FILTERS * FILTER_SIZE; i++) {
			filters[i] -= learning_rate * d_filters[i];
		}

		//filters = matMinus<FILTERS * FILTER_SIZE>(
		//			filters,
		//			scalarMult<FILTERS * FILTER_SIZE>(
		//				learning_rate,
		//				d_filters));
		return d_input;
	}


///////////// TESTFUNKTIONEN //////////////////////////

	/*
		test_forward()

		Für ein 3 * 3 „Einser-Bild“ und einen 2*2 1234-Filter erhält man „10 10 10 10“
		Für ein 3 * 3 „Zweier-Bild“ und einen 2*2 1234-Filter erhält man „20 20 20 20“

		Für 
			3 * 3 „Einser-Bild“ & 
			3 * 3 „Zweier-Bild“ &
			2*2 1234-Filter		&
			2*2 4321-Filter
		erhält man
			„10 10 10 10“
			„10 10 10 10“
			„20 20 20 20“
			„20 20 20 20“
	*/
	void test_forward(){
		cout << "==== forward()-Test ====" << endl;

		array<float, BATCH_SIZE * INPUT_IMG_SIZE>
			images = {	1, 1, 1,
						1, 1, 1,
						1, 1, 1,
						
						2, 2, 2,
						2, 2, 2,
						2, 2, 2};
		filters = {	1, 2,
					3, 4,
					
					4, 3,
					2, 1};

		cout << "-- Bilder --" << endl;
		for(int image_index = 0; image_index < BATCH_SIZE; image_index++){
			array<float, INPUT_IMG_SIZE>
				image = getImage<	INPUT_IMG_SIZE,
									BATCH_SIZE>(
										images,
										image_index);
			print<INPUT_IMG_DIM, INPUT_IMG_DIM>(image, "Eingabebild " + std::to_string(image_index));
		}
		cout << endl;

		cout << "-- Filter --" << endl;
		for(int filter_index = 0; filter_index < FILTERS; filter_index++){
			array<float, FILTER_SIZE>
				filter = extractFilter(filters, filter_index);
			print<FILTER_DIM, FILTER_DIM>(filter, "Filter " + std::to_string(filter_index));
		}
		cout << endl;

		cout << "-- forward() --" << endl;
		array<float, FILTERS * (BATCH_SIZE * OUTPUT_IMG_SIZE)> res = forward(images);
		for(int output_image_index = 0; output_image_index < FILTERS * BATCH_SIZE; output_image_index++){
			array<float, OUTPUT_IMG_SIZE>
				output_image = getImage<OUTPUT_IMG_SIZE, FILTERS * BATCH_SIZE>(res, output_image_index);
			print<OUTPUT_IMG_DIM, OUTPUT_IMG_DIM>(output_image, "Ausgabebild " + std::to_string(output_image_index));
		}
		
		// cout << "(soll „45 9 90 18“ ausgeben)" << endl;
		// print(res, OUTPUT_IMG_DIM, OUTPUT_IMG_DIM);
		cout << endl;

		cout << "==== forward()-Test beendet ====" << endl;
		cout << endl;
	}


	/*
		2 Bilder, 1 Filter, 1*2 in error_tensor
		---------------------------------------
		input = {	1, 1, 1,
					1, 1, 1,
					1, 1, 1,

					2, 2, 2,
					2, 2, 2,
					2, 2, 2};
		error_tensor = 	{1, 1,
						1, 1,
						
						2, 2,
						2, 2};
		filter =  {	1, 2,
				3, 4};
			=> Ableitung nach der Eingabe
				1 3  2
				4 10 6
				3 7  4

				2 6  4
				8 20 12
				6 14 8
			=> Ableitung bzgl der Filter/Gewichte
				36 36
				36 36

		
		1 Bild, 2 Filter, 1*2 in error_tensor
		-------------------------------------
		input = {	1, 1, 1,
				1, 1, 1,
				1, 1, 1};
		filter = {	1, 2,
			3, 4,

			2, 4,
			6, 8};
		error_tensor = {1, 1,
					1, 1,
					
					2, 2,
					2, 2};
			=> Ableitung nach der Eingabe
				9  27 18
				36 90 54
				27 63 36
			=> Ableitung bzgl der Filter/Gewichte
				4 4
				4 4

				8 8
				8 8
		
		2 Bilder, 2 Filter, 2*2 in error_tensor
		---------------------------------------
		input =	{	1, 1, 1,
				1, 1, 1,
				1, 1, 1,

				2, 2, 2,
				2, 2, 2,
				2, 2, 2}; 
		filter = {	1, 2,
				3, 4,

				4, 3,
				2, 1};
		error_tensor =	{	1, 1,
							1, 1,
							
							2, 2,
							2, 2,
							
							3, 3,
							3, 3,
							
							4, 4,
							4, 4};
			=> Ableitung nach der Eingabe
				15 30 15
				30 60 30
				15 30 15

				35 70  35
				70 140 70
				35 70  35
	*/
	void test_backward(){
		cout << "==== backward()-Test ====" << endl;
		array<float, BATCH_SIZE * INPUT_IMG_SIZE>
			images = {	1, 1, 1,
						1, 1, 1,
						1, 1, 1,

						2, 2, 2,
						2, 2, 2,
						2, 2, 2};
		filters = {	1, 2,
					3, 4,

					4, 3,
					2, 1};
		array<float, FILTERS * (BATCH_SIZE * OUTPUT_IMG_SIZE)>
			error_tensor = {1, 1,
							1, 1,
							
							2, 2,
							2, 2,
							
							3, 3,
							3, 3,
							
							4, 4,
							4, 4};
		layer_input = images;

		cout << "-- backward() --" << endl;
		array<float, BATCH_SIZE * INPUT_IMG_SIZE> res = backward(error_tensor);
		for(int image_index = 0; image_index < BATCH_SIZE; image_index++){
			array<float, INPUT_IMG_SIZE>
				image = getImage<	INPUT_IMG_SIZE,
									BATCH_SIZE>(
										res,
										image_index);
			print<INPUT_IMG_DIM, INPUT_IMG_DIM>(image, "Ausgabebild " + std::to_string(image_index));
		}
		cout << endl;
		for(int d_filter_index = 0; d_filter_index < FILTERS; d_filter_index++){
			array<float, FILTER_SIZE>
				d_filter = extractFilter(d_filters, d_filter_index);
			print<FILTER_DIM, FILTER_DIM>(d_filter, "d_filter " + std::to_string(d_filter_index));
		}
		cout << "==== backward()-Test beendet ====" << endl;
	}

};

// int main(){

// 	Conv<2, 2, 3> C;

// 	//C.test_forward();

// 	C.test_backward();	
	
// 	return 0;
// }


// Conv<2, 2, 3> conv;

// // array<float, OUTOUT_IMG_SIZE> correlate2D(	const array<float, INPUT_IMG_SIZE>& x,
// // 											const array<float, FILTER_SIZE>& filter);
// array<float, OUTPUT_IMG_SIZE> correlate(const array<float, INPUT_IMG_SIZE>& x,
// 										const array<float, FILTER_SIZE>& filter,
// 										const int FILTER_DIM);
// template<int IMG_SIZE, int NMB_IMAGES_IN_BATCH>
// array<float, IMG_SIZE> getImage(const array<float, NMB_IMAGES_IN_BATCH * IMG_SIZE>& batch, 
// 								const int image_index);
// template<int ROWS, int COLUMNS>
// array<float, NMB_ELEMENTS_CONV> getPartialImage(	const array<float, INPUT_IMG_SIZE>& image,
// 											const int start_row,
// 											const int start_col);
// template<int ROWS, int COLUMNS>
// void print ( const array<float, NMB_ELEMENTS_CONV>& m, const string message);
// template<int NMB_ENTRIES>
// array<float, NMB_ENTRIES> matMinus(	const array<float, NMB_ENTRIES>& left,
// 									const array<float, NMB_ENTRIES>& right);
// template<int NMB_ENTRIES>
// array<float, NMB_ENTRIES> scalarMult(	const float scalar,
// 										const array<float, NMB_ENTRIES>& matrix);
// void add_to_layer_derivative(	array<float, INPUT_IMG_SIZE * BATCH_SIZE>& d_input,
// 								const array<float, FILTER_SIZE>& matrix,
// 								const int image_index,
// 								const int row,
// 								const int column);
// void add_to_filter_derivative(	array<float, filters.size()>& d_filters,
// 								const array<float, FILTER_SIZE>& matrix,
// 								const int filter_position);
// array<float, FILTER_SIZE> extractFilter(const array<float, filters.size()>& filters,
// 										const int filter_index);
