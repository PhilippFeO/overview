#!/bin/bash

# clear
if [ $# == 0 ]; then
	nvcc Network.cu ./Layers/*.cu ./Hilfsdateien/*.cu -o Network.out
else
	output_file_name=$1
	nvcc Network.cu ./Layers/*.cu ./Hilfsdateien/*.cu -o $output_file_name
fi