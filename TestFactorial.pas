PROGRAM TestFactorial;

VAR
  n: INTEGER;
  i: INTEGER;
  fact: INTEGER;

BEGIN
  readln(n);

  fact := 1;
  FOR i := 1 TO n DO
    fact := fact * i;

  writeln(fact);
END.