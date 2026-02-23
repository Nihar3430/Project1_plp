PROGRAM Test_constructor_and_destructor;

TYPE
  Thing = CLASS
    PUBLIC
      x : INTEGER;

      CONSTRUCTOR Create(v: INTEGER);
      BEGIN
        x := 0;
        x := v;
        x := x + 3;
        writeln(x);
      END;

      DESTRUCTOR Destroy;
      BEGIN
        writeln(999);
      END;
  END;

VAR
  n : INTEGER;
  t : Thing;

BEGIN
  readln(n);

  t := Thing.Create(n);

  writeln(t.x);

  t.Destroy;

  { this line should crash because object is dead now }
  writeln(t.x);
END.