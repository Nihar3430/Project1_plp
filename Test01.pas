PROGRAM TestEncapsulation;

TYPE
  Box = CLASS
    PRIVATE
      width: INTEGER;
      height: INTEGER;
    PUBLIC
      color: INTEGER;

      CONSTRUCTOR Create(w: INTEGER; h: INTEGER);
      BEGIN
        width := w;
        height := h;
        color := 0;
      END;

      PROCEDURE PrintWidth;
      BEGIN
        writeln(width);
      END;

      PROCEDURE PrintHeight;
      BEGIN
        writeln(height);
      END;

      PROCEDURE PrintArea;
      BEGIN
        writeln(width * height);
      END;

      PROCEDURE PrintColor;
      BEGIN
        writeln(color);
      END;
  END;

VAR
  b: Box;

BEGIN
  b := Box.Create(10, 5);
  b.PrintWidth;
  b.PrintHeight;
  b.PrintArea;

  b.color := 255;
  b.PrintColor;
END.