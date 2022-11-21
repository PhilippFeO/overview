#!/bin/bash

echo "Usage: "$0" <SERVER-ADRESS> <PORT>"

java -cp "bin" vsue.communication.VSClient $1 $2
