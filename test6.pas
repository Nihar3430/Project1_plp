PROGRAM Test;

TYPE
  IFoo = INTERFACE
    PROCEDURE Inc;
  END;

  Counter = CLASS(IFoo)
    PUBLIC
      x : INTEGER;
      CONSTRUCTOR Create(v: INTEGER);
      BEGIN
        x := v;
      END;
      PROCEDURE Inc;
      BEGIN
        x := x + 1;
      END;
  END;

VAR
  c : Counter;
  n : INTEGER;

BEGIN
  readln(n);
  c := Counter.Create(n);
  c.Inc;
  writeln(c.x);
END.