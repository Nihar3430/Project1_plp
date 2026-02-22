import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class Main {

    public static void main(String[] args) {

        try {

            String chosenFile;

            if (args != null) {
                if (args.length > 0) {
                    chosenFile = args[0];
                } else {
                    chosenFile = "test.pas";
                }
            } else {
                chosenFile = "test.pas";
            }

            File realFile = new File(chosenFile);

            if (!realFile.exists()) {
                System.out.println("ERROR: file not found -> " + chosenFile);
                return;
            }

            InputStream streamFromFile = new FileInputStream(realFile);

            CharStream antlrInput = CharStreams.fromStream(streamFromFile);

            delphiLexer lexerObject = new delphiLexer(antlrInput);

            CommonTokenStream tokenContainer = new CommonTokenStream(lexerObject);

            delphiParser parserObject = new delphiParser(tokenContainer);

            ParseTree parseTreeRoot = parserObject.program();

            interperter executor = new interperter();

            executor.visit(parseTreeRoot);

            System.out.println("Execution done.");

        } catch (Exception problem) {

            System.out.println("Something went wrong while running.");
            System.out.println(problem.getMessage());

            problem.printStackTrace();
        }
    }
}