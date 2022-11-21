#!/bin/bash

echo "Usage: "$0" <USER-NAME> <SERVER-ADRESS> <PORT>"

java -cp "bin" vsue.rmi.VSAuctionRMIClient $1 $2 $3
