PROGRAM Test2;

TYPE
  Counter = CLASS
    PRIVATE
      secret : INTEGER;
    PUBLIC
      x : INTEGER;

      CONSTRUCTOR Create(v: INTEGER);
      BEGIN
        x := v;
        secret := 100;
      END;

      PROCEDURE Inc;
      BEGIN
        x := x + 1;
      END;
  END;

VAR
  c : Counter;

BEGIN
  c := Counter.Create(5);
  c.Inc;
  writeln(c.x);
END.