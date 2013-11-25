set term postscript eps enhanced
set output "swag-cos.eps"
set xlabel "x"
set ylabel "y"
plot "swag-cos.data" using 1:2 title "Left Wheel" with linespoints, \
"swag-cos.data" using 1:3 title "Right Wheel" with linespoints
