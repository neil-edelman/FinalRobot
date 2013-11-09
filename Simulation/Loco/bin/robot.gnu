set term postscript eps enhanced
set output "robot.eps"
set xlabel "{/Symbol q}"
set ylabel "Sonic"
set yrange [0:260]

plot "robot.data" using 1:2 title "Robot" with linespoints
