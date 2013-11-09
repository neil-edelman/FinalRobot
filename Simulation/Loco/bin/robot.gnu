set term postscript eps enhanced
set output "robot.eps"
set xlabel "x"
set ylabel "y"
set size ratio -1
#set size square
y(x) = 0.8612787*x + 42.21396
plot "robot.data" using 1:2 title "Robot" with linespoints, \
y(x) title "Fit"
