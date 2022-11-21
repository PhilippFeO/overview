#/bin/sh

file_name=thesis_main2

pdflatex -synctex=1 -interaction=nonstopmode -output-directory=Hilfsdateien/ $file_name.tex
biber Hilfsdateien/$file_name.bcf
pdflatex -synctex=1 -interaction=nonstopmode -output-directory=Hilfsdateien/ $file_name.tex
# pdflatex -synctex=1 -interaction=nonstopmode -output-directory=Hilfsdateien/ $file_name.tex
ln --force Hilfsdateien/$file_name.pdf | ln --symbolic --force Hilfsdateien/$file_name.synctex.gz
