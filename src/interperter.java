import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class interperter extends delphiBaseVisitor<Object> {

    private Map<String, Object> variableStorage;
    private Scanner scanner;

    private Map<String, ClassInfo> classes;

    private Obj currentSelf;
    private Map<String, Object> localVars;
    private String currentClassName;

    public interperter() {

        variableStorage = new HashMap<String, Object>();
        scanner = new Scanner(System.in);

        classes = new HashMap<String, ClassInfo>();

        currentSelf = null;
        localVars = null;
        currentClassName = null;
    }

    private static class Obj {

        String className;
        Map<String, Object> fields;
        boolean dead;

        Obj(String name) {

            className = name;

            fields = new HashMap<String, Object>();

            dead = false;
        }
    }

    private static class MethodInfo {

        boolean isPublic;
        delphiParser.BlockContext block;
        List<String> paramNames;

        MethodInfo(boolean pub, delphiParser.BlockContext b, List<String> p) {

            isPublic = pub;
            block = b;
            paramNames = p;
        }
    }

    private static class ClassInfo {

        String name;

        Map<String, Boolean> fieldIsPublic;
        Map<String, MethodInfo> methods;
        Map<String, MethodInfo> constructors;
        Map<String, MethodInfo> destructors;

        ClassInfo(String n) {

            name = n;

            fieldIsPublic = new HashMap<String, Boolean>();
            methods = new HashMap<String, MethodInfo>();
            constructors = new HashMap<String, MethodInfo>();
            destructors = new HashMap<String, MethodInfo>();
        }
    }

    private String up(String s) {

        if (s == null) {
            return null;
        }

        String t;
        t = s.toUpperCase();

        return t;
    }

    private void checkNotDead(Obj o) {

        if (o == null) {
            return;
        }

        if (o.dead == true) {
            throw new RuntimeException("Object already destroyed, cannot use it.");
        }
    }

    private String[] splitDotPathManual(String text) {

        if (text == null) {
            return new String[0];
        }

        int dotPos;
        dotPos = -1;

        int i;
        i = 0;
        while (i < text.length()) {

            char c;
            c = text.charAt(i);

            if (c == '.') {
                dotPos = i;
                break;
            }

            i = i + 1;
        }

        if (dotPos < 0) {
            String[] one;
            one = new String[1];
            one[0] = text;
            return one;
        }

        String left;
        String right;

        left = text.substring(0, dotPos);
        right = text.substring(dotPos + 1);

        String[] two;
        two = new String[2];
        two[0] = left;
        two[1] = right;

        return two;
    }

    private Object getNameValue(String name) {

        String key;
        key = up(name);

        if (localVars != null) {

            if (localVars.containsKey(key)) {
                return localVars.get(key);
            }

            if (localVars.containsKey(name)) {
                return localVars.get(name);
            }
        }

        if (currentSelf != null) {

            checkNotDead(currentSelf);

            if (currentSelf.fields.containsKey(key)) {
                return currentSelf.fields.get(key);
            }

            if (currentSelf.fields.containsKey(name)) {
                return currentSelf.fields.get(name);
            }
        }

        if (variableStorage.containsKey(key)) {
            return variableStorage.get(key);
        }

        if (variableStorage.containsKey(name)) {
            return variableStorage.get(name);
        }

        throw new RuntimeException("Variable not defined: " + name);
    }

    private void setNameValue(String name, Object val) {

        String key;
        key = up(name);

        if (localVars != null) {

            if (localVars.containsKey(key)) {
                localVars.put(key, val);
                return;
            }

            if (localVars.containsKey(name)) {
                localVars.put(name, val);
                return;
            }
        }

        if (currentSelf != null) {

            checkNotDead(currentSelf);

            if (currentSelf.fields.containsKey(key)) {
                currentSelf.fields.put(key, val);
                return;
            }

            if (currentSelf.fields.containsKey(name)) {
                currentSelf.fields.put(name, val);
                return;
            }
        }

        variableStorage.put(key, val);
    }

    private Object getFieldValue(Obj obj, String fieldName, boolean fromOutside) {

        checkNotDead(obj);

        String clsKey;
        clsKey = up(obj.className);

        ClassInfo ci;
        ci = classes.get(clsKey);

        if (ci == null) {
            throw new RuntimeException("Unknown class: " + obj.className);
        }

        String fKey;
        fKey = up(fieldName);

        Boolean isPub;
        isPub = ci.fieldIsPublic.get(fKey);

        if (isPub == null) {
            throw new RuntimeException("Field not found: " + fieldName);
        }

        if (fromOutside == true) {
            if (isPub.booleanValue() == false) {
                throw new RuntimeException("Cannot access PRIVATE field: " + fieldName);
            }
        }

        return obj.fields.get(fKey);
    }

    private void setFieldValue(Obj obj, String fieldName, Object value, boolean fromOutside) {

        checkNotDead(obj);

        String clsKey;
        clsKey = up(obj.className);

        ClassInfo ci;
        ci = classes.get(clsKey);

        if (ci == null) {
            throw new RuntimeException("Unknown class: " + obj.className);
        }

        String fKey;
        fKey = up(fieldName);

        Boolean isPub;
        isPub = ci.fieldIsPublic.get(fKey);

        if (isPub == null) {
            throw new RuntimeException("Field not found: " + fieldName);
        }

        if (fromOutside == true) {
            if (isPub.booleanValue() == false) {
                throw new RuntimeException("Cannot write PRIVATE field: " + fieldName);
            }
        }

        obj.fields.put(fKey, value);
    }

    private List<String> grabParams(delphiParser.FormalParameterListContext fpl) {

        List<String> params;
        params = new ArrayList<String>();

        if (fpl == null) {
            return params;
        }

        List<delphiParser.FormalParameterSectionContext> secs;
        secs = fpl.formalParameterSection();

        int i;
        i = 0;
        while (i < secs.size()) {

            delphiParser.FormalParameterSectionContext sec;
            sec = secs.get(i);

            delphiParser.ParameterGroupContext pg;
            pg = sec.parameterGroup();

            if (pg != null) {

                List<delphiParser.IdentifierContext> ids;
                ids = pg.identifierList().identifier();

                int j;
                j = 0;
                while (j < ids.size()) {

                    String nm;
                    nm = ids.get(j).getText();

                    params.add(up(nm));

                    j = j + 1;
                }
            }

            i = i + 1;
        }

        return params;
    }

    @Override
    public Object visitTypeDefinition(delphiParser.TypeDefinitionContext ctx) {

        String typeName;
        typeName = up(ctx.identifier().getText());

        if (ctx.type_() != null) {

            if (ctx.type_().classType() != null) {

                ClassInfo ci;
                ci = new ClassInfo(typeName);

                delphiParser.ClassTypeContext ct;
                ct = ctx.type_().classType();

                delphiParser.ClassBodyContext body;
                body = ct.classBody();

                List<delphiParser.VisibilitySectionContext> sections;
                sections = body.visibilitySection();

                int s;
                s = 0;
                while (s < sections.size()) {

                    delphiParser.VisibilitySectionContext sec;
                    sec = sections.get(s);

                    boolean isPublic;
                    isPublic = false;

                    if (sec.PUBLIC() != null) {
                        isPublic = true;
                    }

                    int m;
                    m = 0;
                    while (m < sec.classMember().size()) {

                        delphiParser.ClassMemberContext member;
                        member = sec.classMember(m);

                        if (member.fieldDecl() != null) {

                            List<delphiParser.IdentifierContext> ids;
                            ids = member.fieldDecl().identifierList().identifier();

                            int i;
                            i = 0;
                            while (i < ids.size()) {

                                String f;
                                f = up(ids.get(i).getText());

                                ci.fieldIsPublic.put(f, Boolean.valueOf(isPublic));

                                i = i + 1;
                            }
                        }

                        if (member.methodDecl() != null) {

                            if (member.methodDecl().PROCEDURE() != null) {

                                String methodName;
                                methodName = up(member.methodDecl().identifier().getText());

                                delphiParser.BlockContext b;
                                b = member.methodDecl().block();

                                List<String> params;
                                params = grabParams(member.methodDecl().formalParameterList());

                                MethodInfo mi;
                                mi = new MethodInfo(isPublic, b, params);

                                ci.methods.put(methodName, mi);
                            }
                        }

                        if (member.constructorDecleration() != null) {

                            String ctorName;
                            ctorName = up(member.constructorDecleration().identifier().getText());

                            delphiParser.BlockContext b;
                            b = member.constructorDecleration().block();

                            List<String> params;
                            params = grabParams(member.constructorDecleration().formalParameterList());

                            MethodInfo mi;
                            mi = new MethodInfo(true, b, params);

                            ci.constructors.put(ctorName, mi);
                        }

                        if (member.destructorDecleration() != null) {

                            String dname;
                            dname = up(member.destructorDecleration().identifier().getText());

                            delphiParser.BlockContext b;
                            b = member.destructorDecleration().block();

                            List<String> params;
                            params = new ArrayList<String>();

                            MethodInfo mi;
                            mi = new MethodInfo(true, b, params);

                            ci.destructors.put(dname, mi);
                        }

                        m = m + 1;
                    }

                    s = s + 1;
                }

                classes.put(typeName, ci);
                return null;
            }
        }

        return super.visitTypeDefinition(ctx);
    }

    @Override
    public Object visitProgram(delphiParser.ProgramContext ctx) {

        delphiParser.BlockContext b;
        b = ctx.block();

        Object r;
        r = visit(b);

        return r;
    }

    @Override
    public Object visitProcedureStatement(delphiParser.ProcedureStatementContext ctx) {

        String whole;
        whole = ctx.getText();

        String lower;
        lower = whole.toLowerCase();

        delphiParser.ParameterListContext plist;
        plist = null;

        if (ctx.parameterList() != null) {
            plist = ctx.parameterList();
        } else {
            if (ctx.call_Designator() != null) {
                if (ctx.call_Designator().parameterList() != null) {
                    plist = ctx.call_Designator().parameterList();
                }
            }
        }

        if (lower.startsWith("writeln")) {

            if (plist != null) {

                int howMany;
                howMany = plist.actualParameter().size();

                int i;
                i = 0;
                while (i < howMany) {

                    delphiParser.ActualParameterContext ap;
                    ap = plist.actualParameter(i);

                    Object val;
                    val = visit(ap.expression());

                    if (val == null) {
                        System.out.print("null");
                    } else {
                        System.out.print(val.toString());
                    }

                    if (i < howMany - 1) {
                        System.out.print(" ");
                    }

                    i = i + 1;
                }
            }

            System.out.println();
            return null;
        }

        if (lower.startsWith("readln")) {

            String varName;
            varName = null;

            if (plist != null) {

                int count;
                count = plist.actualParameter().size();

                if (count != 1) {
                    throw new RuntimeException("readln expects exactly 1 variable inside the parentheses");
                }

                varName = plist.actualParameter(0).expression().getText();
            }

            if (varName == null) {
                if (ctx.variable() != null) {
                    varName = ctx.variable().getText();
                }
            }

            if (varName == null) {

                int a;
                a = lower.indexOf('(');

                int b;
                b = lower.lastIndexOf(')');

                if (a >= 0) {
                    if (b > a) {
                        varName = whole.substring(a + 1, b);
                    }
                }
            }

            if (varName == null) {
                throw new RuntimeException("readln could not find a variable name");
            }

            if (varName.matches("[A-Za-z][A-Za-z0-9_]*") == false) {
                throw new RuntimeException("readln needs a simple variable name, got: " + varName);
            }

            int num;
            try {
                num = scanner.nextInt();
            } catch (Exception e) {
                throw new RuntimeException("readln expected an integer input");
            }

            variableStorage.put(up(varName), Integer.valueOf(num));
            variableStorage.put(varName, Integer.valueOf(num));

            return null;
        }

        if (ctx.call_Designator() != null) {

            visit(ctx.call_Designator());
            return null;
        }

        if (ctx.variable() != null) {

            String vtxt;
            vtxt = ctx.variable().getText();

            if (vtxt != null) {

                if (vtxt.contains(".")) {

                    String[] parts;
                    parts = splitDotPathManual(vtxt);

                    if (parts.length == 2) {

                        String left;
                        left = parts[0];

                        String right;
                        right = parts[1];

                        Object objVal;
                        objVal = getNameValue(left);

                        if (objVal instanceof Obj) {

                            Obj obj;
                            obj = (Obj) objVal;

                            checkNotDead(obj);

                            ClassInfo ci;
                            ci = classes.get(up(obj.className));

                            if (ci == null) {
                                throw new RuntimeException("Unknown class: " + obj.className);
                            }

                            MethodInfo di;
                            di = ci.destructors.get(up(right));

                            if (di != null) {

                                List<Object> noArgs;
                                noArgs = new ArrayList<Object>();

                                runMethodLikeCode(obj, ci, di, noArgs);
                                obj.dead = true;

                                return null;
                            }

                            MethodInfo mi;
                            mi = ci.methods.get(up(right));

                            if (mi != null) {

                                boolean fromOutside;
                                fromOutside = true;

                                if (currentClassName != null) {
                                    if (up(currentClassName).equals(up(obj.className))) {
                                        fromOutside = false;
                                    }
                                }

                                if (fromOutside == true) {
                                    if (mi.isPublic == false) {
                                        throw new RuntimeException("Cannot call PRIVATE method: " + right);
                                    }
                                }

                                List<Object> noArgs;
                                noArgs = new ArrayList<Object>();

                                runMethodLikeCode(obj, ci, mi, noArgs);
                                return null;
                            }
                        }
                    }
                }
            }
        }

        return super.visitProcedureStatement(ctx);
    }

    @Override
    public Object visitCall_Designator(delphiParser.Call_DesignatorContext ctx) {

        String targetText;
        targetText = ctx.variable().getText();

        List<Object> args;
        args = new ArrayList<Object>();

        if (ctx.parameterList() != null) {

            int i;
            i = 0;
            while (i < ctx.parameterList().actualParameter().size()) {

                Object v;
                v = visit(ctx.parameterList().actualParameter(i).expression());

                args.add(v);

                i = i + 1;
            }
        }

        if (targetText != null) {

            if (targetText.contains(".")) {

                String[] parts;
                parts = splitDotPathManual(targetText);

                if (parts.length == 2) {

                    String leftRaw;
                    leftRaw = parts[0];

                    String rightRaw;
                    rightRaw = parts[1];

                    String left;
                    left = up(leftRaw);

                    String right;
                    right = up(rightRaw);

                    if (classes.containsKey(left)) {

                        ClassInfo ci;
                        ci = classes.get(left);

                        MethodInfo ctor;
                        ctor = ci.constructors.get(right);

                        if (ctor == null) {
                            throw new RuntimeException("Constructor not found: " + left + "." + right);
                        }

                        Obj obj;
                        obj = new Obj(left);

                        List<String> keys;
                        keys = new ArrayList<String>();

                        keys.addAll(ci.fieldIsPublic.keySet());

                        int k;
                        k = 0;
                        while (k < keys.size()) {

                            String f;
                            f = keys.get(k);

                            obj.fields.put(f, Integer.valueOf(0));

                            k = k + 1;
                        }

                        runMethodLikeCode(obj, ci, ctor, args);

                        return obj;
                    }

                    Object objVal;
                    objVal = getNameValue(leftRaw);

                    if (objVal instanceof Obj) {

                        Obj obj;
                        obj = (Obj) objVal;

                        checkNotDead(obj);

                        ClassInfo ci;
                        ci = classes.get(up(obj.className));

                        if (ci == null) {
                            throw new RuntimeException("Unknown class: " + obj.className);
                        }

                        MethodInfo di;
                        di = ci.destructors.get(right);

                        if (di != null) {
                            runMethodLikeCode(obj, ci, di, args);
                            obj.dead = true;
                            return null;
                        }

                        MethodInfo mi;
                        mi = ci.methods.get(right);

                        if (mi == null) {
                            throw new RuntimeException("Method not found: " + obj.className + "." + right);
                        }

                        boolean fromOutside;
                        fromOutside = true;

                        if (currentClassName != null) {
                            if (up(currentClassName).equals(up(obj.className))) {
                                fromOutside = false;
                            }
                        }

                        if (fromOutside == true) {
                            if (mi.isPublic == false) {
                                throw new RuntimeException("Cannot call PRIVATE method: " + right);
                            }
                        }

                        runMethodLikeCode(obj, ci, mi, args);
                        return null;
                    }

                    throw new RuntimeException(leftRaw + " is not an object");
                }
            }
        }

        return null;
    }

    private void runMethodLikeCode(Obj obj, ClassInfo ci, MethodInfo mi, List<Object> args) {

        Obj oldSelf;
        oldSelf = currentSelf;

        Map<String, Object> oldLocals;
        oldLocals = localVars;

        String oldClass;
        oldClass = currentClassName;

        currentSelf = obj;
        currentClassName = ci.name;
        localVars = new HashMap<String, Object>();

        int p;
        p = 0;

        while (p < mi.paramNames.size()) {

            String pnameRaw;
            pnameRaw = mi.paramNames.get(p);

            String pnameKey;
            pnameKey = up(pnameRaw);

            Object aval;
            aval = Integer.valueOf(0);

            if (p < args.size()) {
                aval = args.get(p);
            }

            localVars.put(pnameKey, aval);
            localVars.put(pnameRaw, aval);

            p = p + 1;
        }

        visit(mi.block);

        currentSelf = oldSelf;
        localVars = oldLocals;
        currentClassName = oldClass;
    }

    @Override
    public Object visitAssignmentStatement(delphiParser.AssignmentStatementContext ctx) {

        String leftText;
        leftText = ctx.variable().getText();

        Object value;
        value = visit(ctx.expression());

        if (leftText != null) {

            if (leftText.contains(".")) {

                String[] parts;
                parts = splitDotPathManual(leftText);

                if (parts.length != 2) {
                    throw new RuntimeException("Only one dot supported right now (obj.field). Got: " + leftText);
                }

                String objName;
                objName = parts[0];

                String fieldName;
                fieldName = parts[1];

                Object objVal;
                objVal = getNameValue(objName);

                if (objVal instanceof Obj) {

                    Obj o;
                    o = (Obj) objVal;

                    checkNotDead(o);

                    boolean fromOutside;
                    fromOutside = true;

                    if (currentClassName != null) {
                        if (up(currentClassName).equals(up(o.className))) {
                            fromOutside = false;
                        }
                    }

                    setFieldValue(o, fieldName, value, fromOutside);
                    return null;
                }

                throw new RuntimeException(objName + " is not an object");
            }
        }

        setNameValue(leftText, value);
        return null;
    }

    @Override
    public Object visitVariable(delphiParser.VariableContext ctx) {

        String txt;
        txt = ctx.getText();

        if (txt != null) {

            if (txt.contains(".")) {

                String[] parts;
                parts = splitDotPathManual(txt);

                if (parts.length != 2) {
                    throw new RuntimeException("Only one dot supported right now (obj.field). Got: " + txt);
                }

                String objName;
                objName = parts[0];

                String fieldName;
                fieldName = parts[1];

                Object objVal;
                objVal = getNameValue(objName);

                if (objVal instanceof Obj) {

                    Obj o;
                    o = (Obj) objVal;

                    checkNotDead(o);

                    boolean fromOutside;
                    fromOutside = true;

                    if (currentClassName != null) {
                        if (up(currentClassName).equals(up(o.className))) {
                            fromOutside = false;
                        }
                    }

                    return getFieldValue(o, fieldName, fromOutside);
                }

                throw new RuntimeException(objName + " is not an object");
            }
        }

        return getNameValue(txt);
    }

    @Override
    public Object visitUnsignedInteger(delphiParser.UnsignedIntegerContext ctx) {

        String t;
        t = ctx.getText();

        int n;
        n = Integer.parseInt(t);

        Integer box;
        box = Integer.valueOf(n);

        return box;
    }

    @Override
    public Object visitExpression(delphiParser.ExpressionContext ctx) {

        if (ctx.relationaloperator() == null) {
            return visit(ctx.simpleExpression());
        }

        Object leftValue;
        leftValue = visit(ctx.simpleExpression());

        Object rightValue;
        rightValue = visit(ctx.expression());

        boolean same;
        same = leftValue.equals(rightValue);

        Boolean box;
        box = Boolean.valueOf(same);

        return box;
    }

    @Override
    public Object visitSimpleExpression(delphiParser.SimpleExpressionContext ctx) {

        if (ctx.additiveoperator() == null) {
            return visit(ctx.term());
        }

        Object leftObj;
        leftObj = visit(ctx.term());

        Object rightObj;
        rightObj = visit(ctx.simpleExpression());

        int leftNumber;
        leftNumber = convertToInt(leftObj);

        int rightNumber;
        rightNumber = convertToInt(rightObj);

        if (ctx.additiveoperator().PLUS() != null) {

            int sum;
            sum = leftNumber + rightNumber;

            Integer box;
            box = Integer.valueOf(sum);

            return box;
        }

        int diff;
        diff = leftNumber - rightNumber;

        Integer box;
        box = Integer.valueOf(diff);

        return box;
    }

    @Override
    public Object visitTerm(delphiParser.TermContext ctx) {

        if (ctx.multiplicativeoperator() == null) {
            return visit(ctx.signedFactor());
        }

        Object aObj;
        aObj = visit(ctx.signedFactor());

        Object bObj;
        bObj = visit(ctx.term());

        int a;
        a = convertToInt(aObj);

        int b;
        b = convertToInt(bObj);

        if (ctx.multiplicativeoperator().STAR() != null) {

            int prod;
            prod = a * b;

            Integer box;
            box = Integer.valueOf(prod);

            return box;
        }

        if (b == 0) {
            throw new RuntimeException("Cannot divide by zero.");
        }

        int div;
        div = a / b;

        Integer box;
        box = Integer.valueOf(div);

        return box;
    }

    private int convertToInt(Object incoming) {

        if (incoming == null) {
            throw new RuntimeException("Null value found where integer expected.");
        }

        if (incoming instanceof Integer) {

            Integer ii;
            ii = (Integer) incoming;

            int v;
            v = ii.intValue();

            return v;
        }

        throw new RuntimeException("Expected integer but found different type.");
    }
}