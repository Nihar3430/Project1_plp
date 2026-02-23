PROGRAM Test_io_class;

TYPE
  Box = CLASS
    PUBLIC
      x : INTEGER;

      CONSTRUCTOR Create;
      BEGIN
        x := 0;
      END;

      PROCEDURE AddTwo;
      BEGIN
        x := x + 2;
      END;
  END;

VAR
  b : Box;
  n : INTEGER;

BEGIN
  readln(n);

  b := Box.Create();
  b.x := n;

  b.AddTwo;
  writeln(b.x);
END.