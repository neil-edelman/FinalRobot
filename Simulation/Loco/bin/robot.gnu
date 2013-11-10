set term postscript eps enhanced
set output "robot.eps"
set xlabel "x"
set ylabel "y"
set size ratio -1
#set size square
set palette maxcolors 3
set palette defined (0 '#bbbbbb', 1 '#990000', 2 '#009999')
l(x) = 0.8236678*x + 35.802845

r(x) = -1.1078411*x + -43.149864
set object circle at first 0,0 radius char 0.5 fillcolor rgb 'red' fillstyle solid noborder
plot "robot.data" using 1:2:3 title "Robot" with linespoints palette, \
l(x) title "left 0.6357709*x + -0.7718778*y + 27.635422 = 0", \
r(x) title"right 0.7423128*x + 0.6700535*y + 28.912718 = 0"
