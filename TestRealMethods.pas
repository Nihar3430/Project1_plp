PROGRAM TestMethods;

TYPE
  Person = CLASS
    PRIVATE
      name: INTEGER;
    PUBLIC
      CONSTRUCTOR Create(n: INTEGER);
      BEGIN
        name := n;
      END;

      PROCEDURE PrintName;
      BEGIN
        writeln(name);
      END;
  END;

VAR
  p: Person;

BEGIN
  p := Person.Create(777);
  p.PrintName;
END.