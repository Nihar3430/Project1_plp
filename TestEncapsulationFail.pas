PROGRAM TestEncapsulationFail;

TYPE
  Box = CLASS
    PRIVATE
      width: INTEGER;
    PUBLIC
      CONSTRUCTOR Create(w: INTEGER);
      BEGIN
        width := w;
      END;

      PROCEDURE PrintWidth;
      BEGIN
        writeln(width);
      END;
  END;

VAR
  b: Box;

BEGIN
  b := Box.Create(10);
  b.PrintWidth;

  { should throw: Cannot access PRIVATE field: width }
  writeln(b.width);
END.