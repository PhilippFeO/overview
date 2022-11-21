#!/bin/bash

echo "Usage: "$0" <PORT>"

java -cp "bin" vsue.rmi.VSAuctionRMIServer $1
