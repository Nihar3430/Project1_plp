PROGRAM Test_add_two_nums;

TYPE
  Adder = CLASS
    PUBLIC
      a : INTEGER;
      b : INTEGER;

      CONSTRUCTOR Create;
      BEGIN
        a := 0;
        b := 0;
      END;

      PROCEDURE DoAdd;
      BEGIN
        writeln(a + b);
      END;
  END;

VAR
  x : INTEGER;
  y : INTEGER;
  obj : Adder;

BEGIN
  readln(x);
  readln(y);

  obj := Adder.Create();
  obj.a := x;
  obj.b := y;

  obj.DoAdd;
END.