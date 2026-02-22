import java.util.HashMap;
import java.util.Map;

public class interperter extends delphiBaseVisitor<Object> {

    private Map<String, Object> variableStorage;

    public interperter() {

        variableStorage = new HashMap<String, Object>();

    }

    @Override
    public Object visitProgram(delphiParser.ProgramContext ctx) {

        delphiParser.BlockContext blockPart;

        blockPart = ctx.block();

        Object resultFromBlock;

        resultFromBlock = visit(blockPart);

        return resultFromBlock;
    }

    @Override
    public Object visitAssignmentStatement(delphiParser.AssignmentStatementContext ctx) {

        delphiParser.VariableContext leftSide;

        leftSide = ctx.variable();

        String variableNameText;

        variableNameText = leftSide.getText();

        delphiParser.ExpressionContext rightSide;

        rightSide = ctx.expression();

        Object computedValue;

        computedValue = visit(rightSide);

        if (variableStorage.containsKey(variableNameText)) {

            variableStorage.remove(variableNameText);

        }

        variableStorage.put(variableNameText, computedValue);

        return null;
    }

    @Override
    public Object visitVariable(delphiParser.VariableContext ctx) {

        String lookupName;

        lookupName = ctx.getText();

        boolean exists;

        exists = variableStorage.containsKey(lookupName);

        if (exists == false) {

            throw new RuntimeException("Variable does not exist yet: " + lookupName);

        }

        Object foundValue;

        foundValue = variableStorage.get(lookupName);

        return foundValue;
    }

    @Override
    public Object visitUnsignedInteger(delphiParser.UnsignedIntegerContext ctx) {

        String textForm;

        textForm = ctx.getText();

        int parsedNumber;

        parsedNumber = Integer.parseInt(textForm);

        Integer wrappedNumber;

        wrappedNumber = Integer.valueOf(parsedNumber);

        return wrappedNumber;
    }

    @Override
    public Object visitExpression(delphiParser.ExpressionContext ctx) {

        if (ctx.relationaloperator() == null) {

            Object simpleValue;

            simpleValue = visit(ctx.simpleExpression());

            return simpleValue;
        }

        Object leftValue;

        Object rightValue;

        leftValue = visit(ctx.simpleExpression());

        rightValue = visit(ctx.expression());

        boolean same;

        same = leftValue.equals(rightValue);

        return same;
    }

    @Override
    public Object visitSimpleExpression(delphiParser.SimpleExpressionContext ctx) {

        if (ctx.additiveoperator() == null) {

            Object onlyTerm;

            onlyTerm = visit(ctx.term());

            return onlyTerm;
        }

        Object firstPart;

        Object secondPart;

        firstPart = visit(ctx.term());

        secondPart = visit(ctx.simpleExpression());

        int leftNumber;

        int rightNumber;

        leftNumber = convertToInt(firstPart);

        rightNumber = convertToInt(secondPart);

        if (ctx.additiveoperator().PLUS() != null) {

            int sum;

            sum = leftNumber + rightNumber;

            return sum;
        }

        int difference;

        difference = leftNumber - rightNumber;

        return difference;
    }

    @Override
    public Object visitTerm(delphiParser.TermContext ctx) {

        if (ctx.multiplicativeoperator() == null) {

            Object singleFactor;

            singleFactor = visit(ctx.signedFactor());

            return singleFactor;
        }

        Object partA;

        Object partB;

        partA = visit(ctx.signedFactor());

        partB = visit(ctx.term());

        int numberA;

        int numberB;

        numberA = convertToInt(partA);

        numberB = convertToInt(partB);

        if (ctx.multiplicativeoperator().STAR() != null) {

            int product;

            product = numberA * numberB;

            return product;
        }

        if (numberB == 0) {

            throw new RuntimeException("Cannot divide by zero.");

        }

        int divisionResult;

        divisionResult = numberA / numberB;

        return divisionResult;
    }

    private int convertToInt(Object incoming) {

        if (incoming == null) {

            throw new RuntimeException("Null value found where integer expected.");

        }

        if (incoming instanceof Integer) {

            int value;

            value = ((Integer) incoming).intValue();

            return value;
        }

        throw new RuntimeException("Expected integer but found different type.");
    }
}