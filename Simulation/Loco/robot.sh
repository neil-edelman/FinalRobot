#!/bin/sh
if [ $# -lt 1 ]
then
	echo "Requires a data file."
	exit 1
else
	if [ -r "$1" ]
	then
		echo "Using $1"
	else
		echo "$1 is not readable."
		exit 2
	fi
fi
cd bin
java Ping < "../$1" > robot.data
gnuplot < robot.gnu
open robot.eps
