#!/bin/sh
cd bin
java Driver > robot.data
gnuplot < robot.gnu
open robot.eps
