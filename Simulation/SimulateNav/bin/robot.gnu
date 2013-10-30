#plot "robot.data" using ($2+3):($3+3):1 with labels, "data.txt" using 2:3

set term postscript eps enhanced
set output "robot.eps"
set xlabel "X"
set ylabel "Y"

set parametric

#set trange [0:0.5*pi]
#r = 1.0
#fx(t) = r*cos(t)
#fy(t) = r*sin(t)
#plot fx(t),fy(t) ls 1

plot "robot.data" using 1:2 title "Robot" with linespoints
