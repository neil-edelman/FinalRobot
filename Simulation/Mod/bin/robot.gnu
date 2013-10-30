set term postscript eps enhanced
set output "robot.eps"
set xlabel "X"
set ylabel "Y"

plot "robot.data" using 1:2 title "Robot" with linespoints
