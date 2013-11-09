#!/bin/sh
cd bin
java Ping > robot.data
gnuplot < robot.gnu
open robot.eps
