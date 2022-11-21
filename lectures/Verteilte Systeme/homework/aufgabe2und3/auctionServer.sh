#!/bin/bash

if (( $# != 1 )); then
    echo "Usage: "$0" <PORT>"
else
    java -cp "bin" vsue.rmi.VSAuctionServer $1
fi