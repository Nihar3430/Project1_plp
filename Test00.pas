PROGRAM TestOO;

TYPE
  IGreeter = INTERFACE
    PROCEDURE Greet(who: INTEGER);
  END;

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

  Student = CLASS(Person, IGreeter)
    PRIVATE
      id: INTEGER;
    PUBLIC
      CONSTRUCTOR Create(n: INTEGER; i: INTEGER);
      BEGIN
        name := n;   { inherited private field is allowed inside object code }
        id := i;
      END;

      PROCEDURE Greet(who: INTEGER);
      BEGIN
        { simulate "Hello, who" with integer math }
        writeln(who + 1000);
      END;

      PROCEDURE PrintId;
      BEGIN
        writeln(id);
      END;
  END;

VAR
  s: Student;

BEGIN
  s := Student.Create(5, 42);
  s.Greet(5);
  s.PrintId;
END.