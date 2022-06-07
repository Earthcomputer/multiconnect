for dotfile in *.dot; do
  svgfile="${dotfile%.*}.svg"
  dot -Tsvg -Nfontname=Helvetica -Nfontsize=12 -Efontname=Helvetica -Efontsize=12 "$dotfile" > "$svgfile"
  echo "Written $svgfile"
done
