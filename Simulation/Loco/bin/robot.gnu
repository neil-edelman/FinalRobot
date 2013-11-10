set term postscript eps enhanced
set output "robot.eps"
set xlabel "x"
set ylabel "y"
#set size ratio -1
set size square
y(x) = -1.1340432*x + 42.889206
plot "robot.data" using 1:2 title "Robot" with linespoints, \
y(x) title "Fit"
