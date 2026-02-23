import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

// interpreter for a delphi/pascal subset using an antlr visitor
public class interperter extends delphiBaseVisitor<Object> {

    // global variable storage (program scope)
    private Map<String, Object> variableStorage;
    // stdin scanner used by readln
    private Scanner scanner;

    // known class definitions (by uppercased name)
    private Map<String, ClassInfo> classes;
    // known interface definitions (by uppercased name)
    private Map<String, InterfaceInfo> interfaces = new HashMap<String, InterfaceInfo>();

    // "self" object when executing inside a method/ctor/dtor
    private Obj currentSelf;
    // locals for current method call
    private Map<String, Object> localVars;
    // name of class currently executing (for private access rules)
    private String currentClassName;

    public interperter() {
        variableStorage = new HashMap<String, Object>();
        scanner = new Scanner(System.in);
        classes = new HashMap<String, ClassInfo>();

        currentSelf = null;
        localVars = null;
        currentClassName = null;
    }

    // stores interface method signatures
    private static class InterfaceInfo {
        String name;
        Map<String, MethodInfo> methods;

        InterfaceInfo(String n) {
            name = n;
            methods = new HashMap<String, MethodInfo>();
        }
    }

    // runtime object instance
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

    // stores a method body + metadata
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

    // stores class definition info
    private static class ClassInfo {
        String name;
        String parentName;
        List<String> interfaces;

        Map<String, Boolean> fieldIsPublic;
        Map<String, MethodInfo> methods;
        Map<String, MethodInfo> constructors;
        Map<String, MethodInfo> destructors;

        ClassInfo(String n) {
            name = n;
            parentName = null;

            interfaces = new ArrayList<String>();

            fieldIsPublic = new HashMap<String, Boolean>();
            methods = new HashMap<String, MethodInfo>();
            constructors = new HashMap<String, MethodInfo>();
            destructors = new HashMap<String, MethodInfo>();
        }
    }

    // uppercase helper for case-insensitive identifiers
    private String up(String s) {
        if (s == null) {
            return null;
        }
        return s.toUpperCase();
    }

    // throws if object has been destroyed
    private void checkNotDead(Obj o) {
        if (o == null) {
            return;
        }
        if (o.dead == true) {
            throw new RuntimeException("Object already destroyed, cannot use it.");
        }
    }

    // lookup class info by name (case-insensitive)
    private ClassInfo getClassInfo(String name) {
        if (name == null) {
            return null;
        }

        String k;
        k = up(name);

        if (k == null) {
            return null;
        }

        if (classes.containsKey(k)) {
            return classes.get(k);
        }

        return null;
    }

    // find a method in class or any parent
    private MethodInfo findMethodUp(ClassInfo ci, String methodName) {
        if (ci == null) {
            return null;
        }

        String mk;
        mk = up(methodName);

        ClassInfo cur;
        cur = ci;

        while (cur != null) {

            if (cur.methods.containsKey(mk)) {
                return cur.methods.get(mk);
            }

            if (cur.parentName == null) {
                cur = null;
            } else {
                cur = getClassInfo(cur.parentName);
            }
        }

        return null;
    }

    // find a constructor in class or any parent
    private MethodInfo findCtorUp(ClassInfo ci, String ctorName) {
        if (ci == null) {
            return null;
        }

        String ck;
        ck = up(ctorName);

        ClassInfo cur;
        cur = ci;

        while (cur != null) {

            if (cur.constructors.containsKey(ck)) {
                return cur.constructors.get(ck);
            }

            if (cur.parentName == null) {
                cur = null;
            } else {
                cur = getClassInfo(cur.parentName);
            }
        }

        return null;
    }

    // find a destructor in class or any parent
    private MethodInfo findDtorUp(ClassInfo ci, String dtorName) {
        if (ci == null) {
            return null;
        }

        String dk;
        dk = up(dtorName);

        ClassInfo cur;
        cur = ci;

        while (cur != null) {

            if (cur.destructors.containsKey(dk)) {
                return cur.destructors.get(dk);
            }

            if (cur.parentName == null) {
                cur = null;
            } else {
                cur = getClassInfo(cur.parentName);
            }
        }

        return null;
    }

    // find field visibility in class or any parent
    private Boolean findFieldVisUp(ClassInfo ci, String fieldName) {
        if (ci == null) {
            return null;
        }

        String fk;
        fk = up(fieldName);

        ClassInfo cur;
        cur = ci;

        while (cur != null) {

            if (cur.fieldIsPublic.containsKey(fk)) {
                return cur.fieldIsPublic.get(fk);
            }

            if (cur.parentName == null) {
                cur = null;
            } else {
                cur = getClassInfo(cur.parentName);
            }
        }

        return null;
    }

    // init fields across class + ancestors with default 0
    private void initFieldsUp(Obj obj, ClassInfo ci) {
        if (obj == null) {
            return;
        }
        if (ci == null) {
            return;
        }

        ClassInfo cur;
        cur = ci;

        while (cur != null) {

            List<String> keys;
            keys = new ArrayList<String>();
            keys.addAll(cur.fieldIsPublic.keySet());

            int i;
            i = 0;
            while (i < keys.size()) {

                String f;
                f = keys.get(i);

                if (obj.fields.containsKey(f) == false) {
                    obj.fields.put(f, Integer.valueOf(0));
                }

                i = i + 1;
            }

            if (cur.parentName == null) {
                cur = null;
            } else {
                cur = getClassInfo(cur.parentName);
            }
        }
    }

    // verify class implements all methods required by its interfaces
    private void checkInterfaces(ClassInfo ci) {
        if (ci == null) {
            return;
        }

        int i;
        i = 0;
        while (i < ci.interfaces.size()) {

            String iname;
            iname = ci.interfaces.get(i);

            InterfaceInfo ii;
            ii = interfaces.get(up(iname));

            if (ii == null) {
                throw new RuntimeException("Unknown interface: " + iname);
            }

            List<String> need;
            need = new ArrayList<String>();
            need.addAll(ii.methods.keySet());

            int j;
            j = 0;
            while (j < need.size()) {

                String m;
                m = need.get(j);

                MethodInfo got;
                got = findMethodUp(ci, m);

                if (got == null) {
                    throw new RuntimeException("Class " + ci.name + " does not implement method " + m + " from " + iname);
                }

                j = j + 1;
            }

            i = i + 1;
        }
    }

    // split on first dot only (supports "a.b")
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

    // resolve identifier value: locals -> self fields -> globals
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

    // assign to identifier: locals -> self fields -> globals
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

    // read a field with access control
    private Object getFieldValue(Obj obj, String fieldName, boolean fromOutside) {

        checkNotDead(obj);

        ClassInfo ci;
        ci = getClassInfo(obj.className);

        if (ci == null) {
            throw new RuntimeException("Unknown class: " + obj.className);
        }

        Boolean isPub;
        isPub = findFieldVisUp(ci, fieldName);

        if (isPub == null) {
            throw new RuntimeException("Field not found: " + fieldName);
        }

        if (fromOutside == true) {
            if (isPub.booleanValue() == false) {
                throw new RuntimeException("Cannot access PRIVATE field: " + fieldName);
            }
        }

        return obj.fields.get(up(fieldName));
    }

    // write a field with access control
    private void setFieldValue(Obj obj, String fieldName, Object value, boolean fromOutside) {

        checkNotDead(obj);

        ClassInfo ci;
        ci = getClassInfo(obj.className);

        if (ci == null) {
            throw new RuntimeException("Unknown class: " + obj.className);
        }

        Boolean isPub;
        isPub = findFieldVisUp(ci, fieldName);

        if (isPub == null) {
            throw new RuntimeException("Field not found: " + fieldName);
        }

        if (fromOutside == true) {
            if (isPub.booleanValue() == false) {
                throw new RuntimeException("Cannot write PRIVATE field: " + fieldName);
            }
        }

        obj.fields.put(up(fieldName), value);
    }

    // extract parameter names from parse tree
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

        // parse a type definition (interface or class)
        String typeName;
        typeName = up(ctx.identifier().getText());

        if (ctx.type_() == null) {
            return super.visitTypeDefinition(ctx);
        }

        // handle interface definitions
        if (ctx.type_().interfaceType() != null) {

            InterfaceInfo ii;
            ii = new InterfaceInfo(typeName);

            delphiParser.InterfaceTypeContext it;
            it = ctx.type_().interfaceType();

            List<delphiParser.InterfaceMemberContext> mems;
            mems = it.interfaceBody().interfaceMember();

            int i;
            i = 0;
            while (i < mems.size()) {

                delphiParser.InterfaceMemberContext m;
                m = mems.get(i);

                if (m.PROCEDURE() != null) {

                    String mname;
                    mname = up(m.identifier().getText());

                    List<String> params;
                    params = new ArrayList<String>();

                    if (m.formalParameterList() != null) {
                        params = grabParams(m.formalParameterList());
                    }

                    MethodInfo fake;
                    fake = new MethodInfo(true, null, params);

                    ii.methods.put(mname, fake);
                }

                if (m.FUNCTION() != null) {

                    String mname;
                    mname = up(m.identifier().getText());

                    List<String> params;
                    params = new ArrayList<String>();

                    if (m.formalParameterList() != null) {
                        params = grabParams(m.formalParameterList());
                    }

                    MethodInfo fake;
                    fake = new MethodInfo(true, null, params);

                    ii.methods.put(mname, fake);
                }

                i = i + 1;
            }

            interfaces.put(typeName, ii);

            return null;
        }

        // handle class definitions
        if (ctx.type_().classType() != null) {

            ClassInfo ci;
            ci = new ClassInfo(typeName);

            delphiParser.ClassTypeContext ct;
            ct = ctx.type_().classType();

            // parse parent + interfaces list
            if (ct.classHeritage() != null) {

                List<delphiParser.IdentifierContext> ids;
                ids = ct.classHeritage().identifierList().identifier();

                if (ids.size() > 0) {
                    String first;
                    first = ids.get(0).getText();
                    ci.parentName = up(first);
                }

                int z;
                z = 1;
                while (z < ids.size()) {
                    String in;
                    in = ids.get(z).getText();
                    ci.interfaces.add(up(in));
                    z = z + 1;
                }
            }

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

            checkInterfaces(ci);

            return null;
        }

        return super.visitTypeDefinition(ctx);
    }

    @Override
    public Object visitProgram(delphiParser.ProgramContext ctx) {
        delphiParser.BlockContext b;
        b = ctx.block();
        return visit(b);
    }

    @Override
    public Object visitForStatement(delphiParser.ForStatementContext ctx) {

        // evaluate bounds then run loop body
        String varName = ctx.identifier().getText();
        Object startObj = visit(ctx.forList().initialValue());
        Object endObj   = visit(ctx.forList().finalValue());

        int start = convertToInt(startObj);
        int end   = convertToInt(endObj);

        boolean isTo = (ctx.forList().TO() != null);

        if (isTo) {
            int v = start;
            while (v <= end) {
                setNameValue(varName, Integer.valueOf(v));
                visit(ctx.statement());
                v = v + 1;
            }
        } else {
            int v = start;
            while (v >= end) {
                setNameValue(varName, Integer.valueOf(v));
                visit(ctx.statement());
                v = v - 1;
            }
        }

        return null;
    }

    @Override
    public Object visitProcedureStatement(delphiParser.ProcedureStatementContext ctx) {

        // detect builtins and dispatch calls
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

        // allow "obj.method;" without parentheses
        if (ctx.variable() != null) {

            String vtxt;
            vtxt = ctx.variable().getText();

            if (vtxt != null && vtxt.contains(".")) {

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
                        ci = getClassInfo(obj.className);

                        if (ci == null) {
                            throw new RuntimeException("Unknown class: " + obj.className);
                        }

                        MethodInfo di;
                        di = findDtorUp(ci, right);

                        if (di != null) {

                            List<Object> noArgs;
                            noArgs = new ArrayList<Object>();

                            runMethodLikeCode(obj, ci, di, noArgs);
                            obj.dead = true;

                            return null;
                        }

                        MethodInfo mi;
                        mi = findMethodUp(ci, right);

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

        return super.visitProcedureStatement(ctx);
    }

    @Override
    public Object visitCall_Designator(delphiParser.Call_DesignatorContext ctx) {

        // handle "Class.Ctor(...)" and "obj.method(...)"
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

        if (targetText != null && targetText.contains(".")) {

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
                    ctor = findCtorUp(ci, right);

                    if (ctor == null) {
                        throw new RuntimeException("Constructor not found: " + left + "." + right);
                    }

                    Obj obj;
                    obj = new Obj(left);

                    initFieldsUp(obj, ci);

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
                    ci = getClassInfo(obj.className);

                    if (ci == null) {
                        throw new RuntimeException("Unknown class: " + obj.className);
                    }

                    MethodInfo di;
                    di = findDtorUp(ci, right);

                    if (di != null) {
                        runMethodLikeCode(obj, ci, di, args);
                        obj.dead = true;
                        return null;
                    }

                    MethodInfo mi;
                    mi = findMethodUp(ci, right);

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

        return null;
    }

    // run a method/ctor/dtor with new self + locals
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

        if (mi.block != null) {
            visit(mi.block);
        }

        currentSelf = oldSelf;
        localVars = oldLocals;
        currentClassName = oldClass;
    }

    @Override
    public Object visitAssignmentStatement(delphiParser.AssignmentStatementContext ctx) {

        // assign to var or obj.field
        String leftText;
        leftText = ctx.variable().getText();

        Object value;
        value = visit(ctx.expression());

        if (leftText != null && leftText.contains(".")) {

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

        setNameValue(leftText, value);
        return null;
    }

    @Override
    public Object visitVariable(delphiParser.VariableContext ctx) {

        // read var or obj.field
        String txt;
        txt = ctx.getText();

        if (txt != null && txt.contains(".")) {

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

        return getNameValue(txt);
    }

    @Override
    public Object visitUnsignedInteger(delphiParser.UnsignedIntegerContext ctx) {
        return Integer.valueOf(Integer.parseInt(ctx.getText()));
    }

    @Override
    public Object visitExpression(delphiParser.ExpressionContext ctx) {

        // supports equality only when relationaloperator exists
        if (ctx.relationaloperator() == null) {
            return visit(ctx.simpleExpression());
        }

        Object leftValue;
        leftValue = visit(ctx.simpleExpression());

        Object rightValue;
        rightValue = visit(ctx.expression());

        return Boolean.valueOf(leftValue.equals(rightValue));
    }

    @Override
    public Object visitSimpleExpression(delphiParser.SimpleExpressionContext ctx) {

        if (ctx.additiveoperator() == null) {
            return visit(ctx.term());
        }

        int leftNumber = convertToInt(visit(ctx.term()));
        int rightNumber = convertToInt(visit(ctx.simpleExpression()));

        if (ctx.additiveoperator().PLUS() != null) {
            return Integer.valueOf(leftNumber + rightNumber);
        }

        return Integer.valueOf(leftNumber - rightNumber);
    }

    @Override
    public Object visitTerm(delphiParser.TermContext ctx) {

        if (ctx.multiplicativeoperator() == null) {
            return visit(ctx.signedFactor());
        }

        int a = convertToInt(visit(ctx.signedFactor()));
        int b = convertToInt(visit(ctx.term()));

        if (ctx.multiplicativeoperator().STAR() != null) {
            return Integer.valueOf(a * b);
        }

        if (b == 0) {
            throw new RuntimeException("Cannot divide by zero.");
        }

        return Integer.valueOf(a / b);
    }

    // converts runtime value to int or throws
    private int convertToInt(Object incoming) {
        if (incoming == null) {
            throw new RuntimeException("Null value found where integer expected.");
        }

        if (incoming instanceof Integer) {
            return ((Integer) incoming).intValue();
        }

        throw new RuntimeException("Expected integer but found different type.");
    }
}