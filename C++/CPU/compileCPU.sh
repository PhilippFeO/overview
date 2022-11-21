#!/bin/bash

# clear
if [ $# == 0 ]; then
	g++ Network.cpp -o Network.out
else
	output_file_name=$1
	g++ Network.cpp -o $output_file_name
fi