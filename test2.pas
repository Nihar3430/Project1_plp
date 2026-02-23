PROGRAM Test_constructor_and_separate_classes;

TYPE
  Box = CLASS
    PUBLIC
      x : INTEGER;

      CONSTRUCTOR Create(v: INTEGER);
      BEGIN
        x := v;
      END;
  END;

VAR
  a : Box;
  b : Box;

BEGIN
  a := Box.Create(10);
  b := Box.Create(99);

  writeln(a.x);
  writeln(b.x);

  writeln(a.x);
END.