#!/bin/bash

path=~/Informatik/Master_Informatik/VerteilteSysteme/verteilte-systeme-gruppe-04/
host=ka20xyki@cip2b0.cip.cs.fau.de

 scp -r $path/* $host:~/VerteilteSysteme

# ssh $host /home/cip/nf2016/ka20xyki/VerteilteSysteme/aufgabe1/client.sh

for ((i=0; i<3; i++)); do
	command='ssh ka20xyki@cip2b'$i'.cip.cs.fau.de'
	if (( $i==0 )); then
		command2='ssh ka20xyki@cip2b'$i'.cip.cs.fau.de ./auctionServerHelper.sh'
	else
		command3='ssh ka20xyki@cip2b'$i'.cip.cs.fau.de ./auctionClientHelper.sh'
	fi
	gnome-terminal --window-with-profile=Bash -e "bash -c '$command'"
done
