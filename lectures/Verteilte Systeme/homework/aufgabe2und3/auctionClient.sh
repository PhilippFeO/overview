#!/bin/bash

if (( $# != 3 )); then
    echo "Usage: "$0" <USER-NAME> <SERVER-ADRESS> <PORT>"
else
    java -cp "bin" vsue.rmi.VSAuctionClient $1 $2 $3
fi