PROGRAM Test;

TYPE
  IFoo = INTERFACE
    PROCEDURE Inc;
  END;

  Base = CLASS
    PUBLIC
      x : INTEGER;
      CONSTRUCTOR Create(v: INTEGER);
      BEGIN
        x := v;
      END;
  END;

  Child = CLASS(Base, IFoo)
    PUBLIC
      PROCEDURE Inc;
      BEGIN
        x := x + 1;
      END;
  END;

VAR
  c : Child;
  n : INTEGER;

BEGIN
  readln(n);
  c := Child.Create(n);
  c.Inc;
  writeln(c.x);
END.