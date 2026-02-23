import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {

        String fileName;

        fileName = "test1_io.pas"; // default if you forget to pass args
        if (args != null) {
            if (args.length > 0) {
                fileName = args[0];
            }
        }

        try {

            String code;
            code = Files.readString(Paths.get(fileName));

            CharStream input;
            input = CharStreams.fromString(code);

            delphiLexer lex;
            lex = new delphiLexer(input);

            CommonTokenStream toks;
            toks = new CommonTokenStream(lex);

            delphiParser pars;
            pars = new delphiParser(toks);

            // show parse errors in a simple way
            pars.removeErrorListeners();
            pars.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                        int line, int charPositionInLine,
                                        String msg, RecognitionException e) {
                    System.out.println("Parse error at line " + line + ":" + charPositionInLine);
                    System.out.println(msg);
                }
            });

            ParseTree tree;
            tree = pars.program();

            interperter iRun;
            iRun = new interperter();

            iRun.visit(tree);

            System.out.println("\nExecution done.");

        } catch (Exception e) {

            System.out.println("Something went wrong while running.");
            System.out.println(e.getMessage());
            e.printStackTrace();

        }
    }
}