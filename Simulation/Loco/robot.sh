#!/bin/sh
if [ $# -lt 1 ]
then
	echo "Requires a file .data in bin/."
	exit 1
else
	if [ -r "bin/$1.data" ]
	then
		echo "Using bin/$1.data"
	else
		echo "bin/$1.data is not readable."
		exit 2
	fi
fi
cd bin
java Ping < $1.data > robot.data
gnuplot < robot.gnu
open robot.eps
