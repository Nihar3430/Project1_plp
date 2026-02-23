PROGRAM test_inheritance_interfaces;

TYPE
  IRun = INTERFACE
    PROCEDURE Run;
  END;

  Base = CLASS
    PUBLIC
      PROCEDURE Run;
      BEGIN
        writeln(88);
      END;

      CONSTRUCTOR Create;
      BEGIN
      END;
  END;

  Child = CLASS(Base, IRun)
    PUBLIC
      CONSTRUCTOR Create;
      BEGIN
      END;
  END;

VAR
  c : Child;

BEGIN
  c := Child.Create();
  c.Run;
END.