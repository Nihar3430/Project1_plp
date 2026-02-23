PROGRAM TestDestructor;

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

      DESTRUCTOR Destroy;
      BEGIN
        { nothing, interpreter marks object dead after call }
      END;
  END;

VAR
  b: Box;

BEGIN
  b := Box.Create(123);
  b.PrintSize;

  b.Destroy;

  { should throw: Object already destroyed, cannot use it. }
  b.PrintSize;
END.