PROGRAM Test_encapsulation_good;

TYPE
  SecretBox = CLASS
    PRIVATE
      secret : INTEGER;
    PUBLIC
      CONSTRUCTOR Create;
      BEGIN
        secret := 0;
      END;

      PROCEDURE Set42;
      BEGIN
        secret := 42;
      END;

      PROCEDURE Set50;
      BEGIN
        secret := 50;
      END;

      PROCEDURE Show;
      BEGIN
        writeln(secret);
      END;
  END;

VAR
  s : SecretBox;

BEGIN
  s := SecretBox.Create();
  s.Set42;
  s.Show;

  s.Set50;
  s.Show;
END.