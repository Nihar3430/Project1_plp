PROGRAM Test3;

TYPE
  A = CLASS
    PRIVATE
      secret : INTEGER;
    PUBLIC
      x : INTEGER;

      CONSTRUCTOR Create(v: INTEGER);
      BEGIN
        x := v;
        secret := 99;
      END;
  END;

VAR
  o : A;

BEGIN
  o := A.Create(1);
  writeln(o.secret);
END.