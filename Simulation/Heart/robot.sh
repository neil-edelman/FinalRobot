#!/bin/sh
cd bin
java Heart > robot.data
gnuplot < robot.gnu
open robot.eps
