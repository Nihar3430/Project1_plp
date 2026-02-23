PROGRAM Test_encapsulation_SHOULD_FAIL_WITH_PRIVATE_ERROR;

TYPE
  SecretBox = CLASS
    PRIVATE
      secret : INTEGER;
    PUBLIC
      CONSTRUCTOR Create;
      BEGIN
        secret := 123;
      END;
  END;

VAR
  s : SecretBox;

BEGIN
  s := SecretBox.Create();
  writeln(1);

  s.secret := 5;

  writeln(2);
END.