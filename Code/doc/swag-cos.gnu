set term postscript eps enhanced
set output "swag-cos.eps"
set ylabel "Motor setting"
set xlabel "Time (x * 25 ms)"
plot "swag-cos.data" using 1:2 title "Left Wheel" with linespoints, \
"swag-cos.data" using 1:3 title "Right Wheel" with linespoints
