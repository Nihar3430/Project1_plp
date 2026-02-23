PROGRAM Test_inheritance;

TYPE
  Base = CLASS
    PUBLIC
      x : INTEGER;

      CONSTRUCTOR Create;
      BEGIN
        x := 0;
      END;

      PROCEDURE IncX;
      BEGIN
        x := x + 1;
      END;
  END;

  Child = CLASS(Base)
    PUBLIC
      CONSTRUCTOR Create;
      BEGIN
        x := 5;
      END;
  END;

VAR
  c : Child;

BEGIN
  c := Child.Create();
  writeln(c.x);

  c.IncX;
  writeln(c.x);
END.