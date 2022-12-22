#!/bin/bash

cd ./GPU

ulimit -s hard

printf "Starte %s\n" $1
$1

# for((i=0; i<3; i++)){
#     $1
# }