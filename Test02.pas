PROGRAM TestInheritance;

TYPE
  IShape = INTERFACE
    PROCEDURE PrintArea;
  END;

  Rectangle = CLASS
    PRIVATE
      w: INTEGER;
      h: INTEGER;
    PUBLIC
      CONSTRUCTOR Create(a: INTEGER; b: INTEGER);
      BEGIN
        w := a;
        h := b;
      END;

      PROCEDURE PrintArea;
      BEGIN
        writeln(w * h);
      END;
  END;

  Square = CLASS(Rectangle, IShape)
    PUBLIC
      CONSTRUCTOR Create(side: INTEGER);
      BEGIN
        w := side;
        h := side;
      END;
  END;

VAR
  r: Rectangle;
  s: Square;

BEGIN
  r := Rectangle.Create(8, 4);
  r.PrintArea;      { 32 }

  s := Square.Create(5);
  s.PrintArea;      { 25 }
END.