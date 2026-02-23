PROGRAM TestObjectCreate;

TYPE
  Box = CLASS
    PRIVATE
      size: INTEGER;
    PUBLIC
      CONSTRUCTOR Create(x: INTEGER);
      BEGIN
        size := x;
      END;

      PROCEDURE PrintSize;
      BEGIN
        writeln(size);
      END;
  END;

VAR
  b: Box;

BEGIN
  b := Box.Create(123);
  b.PrintSize;
END.