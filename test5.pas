PROGRAM Test;

TYPE
  Base = CLASS
    PUBLIC
      x : INTEGER;
      CONSTRUCTOR Create(v: INTEGER);
      BEGIN
        x := v;
      END;
      PROCEDURE AddOne;
      BEGIN
        x := x + 1;
      END;
  END;

  Child = CLASS(Base)
    PUBLIC
      PROCEDURE AddTwo;
      BEGIN
        x := x + 2;
      END;
  END;

VAR
  c : Child;
  n : INTEGER;

BEGIN
  readln(n);
  c := Child.Create(n);   { uses Base.Create via inheritance }
  c.AddOne;               { Base method }
  c.AddTwo;               { Child method }
  writeln(c.x);           { inherited field }
END.