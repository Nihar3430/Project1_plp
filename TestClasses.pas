PROGRAM TestMultipleClasses;

TYPE
  Person = CLASS
    PRIVATE
      age: INTEGER;
    PUBLIC
      name: INTEGER;

      CONSTRUCTOR Create(n: INTEGER; a: INTEGER);
      BEGIN
        name := n;
        age := a;
      END;

      PROCEDURE PrintAge;
      BEGIN
        writeln(age);
      END;

      PROCEDURE SetAge(newAge: INTEGER);
      BEGIN
        age := newAge;
      END;
  END;

  Student = CLASS(Person)
    PRIVATE
      grade: INTEGER;
    PUBLIC
      CONSTRUCTOR Create(n: INTEGER; a: INTEGER; g: INTEGER);
      BEGIN
        name := n;
        age := a;
        grade := g;
      END;

      PROCEDURE PrintGrade;
      BEGIN
        writeln(grade);
      END;
  END;

VAR
  p: Person;
  s: Student;

BEGIN
  p := Person.Create(100, 25);
  writeln(p.name);
  p.PrintAge;

  p.SetAge(26);
  p.PrintAge;

  s := Student.Create(200, 20, 85);
  writeln(s.name);
  s.PrintGrade;
END.