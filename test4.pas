PROGRAM Test4;

TYPE
  Thing = CLASS
    PUBLIC
      x : INTEGER;

      CONSTRUCTOR Create(v: INTEGER);
      BEGIN
        x := v;
      END;

      DESTRUCTOR Destroy;
      BEGIN
        x := 0;
      END;
  END;

VAR
  t : Thing;

BEGIN
  t := Thing.Create(7);
  writeln(t.x);
  t.Destroy;
  writeln(t.x);
END.