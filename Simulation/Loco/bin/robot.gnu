set term postscript eps enhanced
set output "robot.eps"
set xlabel "x"
set ylabel "y"
set size ratio -1
#set size square
set palette maxcolors 3
set palette defined (0 '#bbbbbb', 1 '#990000', 2 '#009999')
l(x) = 0.2361017*x + 27.070236

r(x) = -3.45018*x + -108.706795
set object circle at first 0,0 radius char 0.5 fillcolor rgb 'red' fillstyle solid noborder
plot "robot.data" using 1:2:3 title "Robot" with linespoints palette, \
l(x) title "left 0.22978401*x + -0.9732417*y + 26.345884 = 0", \
r(x) title"right 0.96047026*x + 0.27838263*y + 30.262083 = 0"
