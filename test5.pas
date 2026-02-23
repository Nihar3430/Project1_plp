PROGRAM test_destructor_SHOULD_FAIL_AS_OBJECT_IS_DESTROYED;

TYPE
  Thing = CLASS
    PUBLIC
      x : INTEGER;

      CONSTRUCTOR Create;
      BEGIN
        x := 111;
      END;

      DESTRUCTOR Destroy;
      BEGIN
        writeln(222);
      END;
  END;

VAR
  t : Thing;

BEGIN
  t := Thing.Create();
  writeln(t.x);

  t.Destroy;

  writeln(t.x);
END.