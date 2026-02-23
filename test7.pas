PROGRAM Test9;

TYPE
  IPing = INTERFACE
    PROCEDURE Ping;
  END;

  Pinger = CLASS(IPing)
    PUBLIC
      CONSTRUCTOR Create;
      BEGIN
      END;

      PROCEDURE Ping;
      BEGIN
        writeln(77);
      END;
  END;

VAR
  p : Pinger;

BEGIN
  p := Pinger.Create();
  p.Ping;
END.