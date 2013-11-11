set term postscript eps enhanced
set output "robot.eps"
set xlabel "x"
set ylabel "y"
set size ratio -1
#set size square
set palette maxcolors 3
set palette defined (0 '#bbbbbb', 1 '#990000', 2 '#009999')
l(x) = 0.82366794*x + 35.80285

r(x) = -1.107841*x + -43.14986
set object circle at first 0,0 radius char 0.5 fillcolor rgb 'red' fillstyle solid noborder
plot "robot.data" using 1:2:3 title "Robot" with linespoints palette, \
l(x) title "left -0.56578976*x + -0.8245496*y + 8.230578 = 0", \
r(x) title"right -0.41301343*x + 0.9107249*y + 10.0141945 = 0"
