import java.util.*;

public class Java_TranslationSchemaAnalysis {
    private static StringBuffer prog = new StringBuffer();

    //分词后的输入流
    private static List<Token> input = new ArrayList<>();

    /**
     * this method is to read the standard input
     */
    private static void read_prog() {
        Scanner sc = new Scanner(System.in);
        while (sc.hasNextLine()) {
            prog.append(sc.nextLine());
            prog.append('\n'); // 每行后面加上换行符
        }
        //test
//        prog.append("int a = 1 ; \n" +
//                "int b = 2 ; \n" +
//                "real c = 3.0 ;\n" +
//                "{\n" +
//                "a = a + 1 ;\n" +
//                "b = b * a ;\n" +
//                "if ( a < b ) \n" +
//                "then c = c / 2 ; \n" +
//                "else c = c / 4 ;\n" +
//                "}");
    }


    // add your method here!!
    private static boolean isError = false; //用于记录是否出错 true表示出错，并输出相应的错误 false表示未出错
    //存储错误信息 可能有多条错误信息 因此用List<String>
    //本实验需要考虑错误处理，如果程序不正确，它应该输出语义错误信息（与行号一起）并退出，不需要进行错误改正。
    private static List<String> error_info = new ArrayList<>();

    //关键字
    private static HashSet<String> keywords = new HashSet<>(Arrays.asList("if", "then", "else", "int", "real"));

    //操作符
    private static HashSet<String> operator = new HashSet<>(Arrays.asList("+", "-", "*", "/", "(", ")"));

    //用于记录标识符及其属性值
    private static HashMap<String, Attribute> ID_Attribute = new HashMap<>();


    /**
     * 词法分析 分词并放入input中
     */
    private static void lexical_analysis() {
        int cnt = 1;
        StringBuffer stringBuffer = new StringBuffer();
        //分词 一个一个字符地读入
        for (int i = 0; i < prog.length(); i++) {
            if (prog.charAt(i) == '\0') { //遇到结束符
                if (stringBuffer.length() > 0) {
                    input.add(new Token(stringBuffer.toString(), cnt));
                    stringBuffer.delete(0, stringBuffer.length()); //清空string buffer
                }
                break; //跳出循环
            } else if (prog.charAt(i) == '\n') {
                if (stringBuffer.length() > 0) {
                    input.add(new Token(stringBuffer.toString(), cnt++)); //遇到换行符 cnt++
                    stringBuffer.delete(0, stringBuffer.length()); //清空string buffer
                }
            } else if (prog.charAt(i) == ' ') {
                if (stringBuffer.length() > 0) {
                    input.add(new Token(stringBuffer.toString(), cnt)); //遇到空格 加入input
                    stringBuffer.delete(0, stringBuffer.length()); //清空string buffer
                }
            } else {
                stringBuffer.append(prog.charAt(i)); //否则就压入word中
            }
        }
    }

    /**
     * 初始化:识别input中的identifier，放入属性表中
     */
    private static void init() {
        for (Token token : input) {
            //开头为单词 且不属于关键字————>标识符
            if (Character.isAlphabetic(token.getToken().charAt(0)) && !keywords.contains(token.getToken())) {
                //初使状态设为void 值设为0
                ID_Attribute.put(token.getToken(), new Attribute("void", 0.0));
            }
        }
    }

    /**
     * 语义分析--进行类型检查，以便对算术表达式(无论是整数算术还是实数算术)执行正确的操作。
     */
    private static void parse() {
        //遍历input中的token
        for (int i = 0; i < input.size(); i++) {
            Token token = input.get(i);
            //如果是int/real的声明语句
            if (token.getToken().equals("int") || token.getToken().equals("real")) {
                //属性文法 将声明的标识符的属性记录下来
                String type = token.getToken();
                //input.get(i+1) --> 标识符
                String id = input.get(i + 1).getToken();
                //input.get(i+2) --> 赋值符号
                String opr = input.get(i + 2).getToken();
                //input.get(i+3) --> 数值
                String val = input.get(i + 3).getToken();
                if (opr.equals("=")) { //如果i+3处是赋值符号
                    Attribute attr = ID_Attribute.get(id);
                    //设置id对应的属性类型
                    attr.setType(type);
                    //设置id对应的属性值 注意转换为double
                    attr.setValue(Double.parseDouble(val));
                    //如果是声明是int 但是后面却是小数 错误处理1：translate_error
                    if (type.equals("int") && val.contains(".")) {
                        translate_error(token);
//                        break; //跳出程序
                    }
                    //input.get(i+4) --> 分号
                    String semi = input.get(i + 4).getToken();
                    if (semi.equals(";")) { //如果i+4处是分号
                        i += 4; // i+4, 跳到分号处
                        continue;
                    } else { //如果不是分号 错误处理2: missingSemi_error
                        missingSemi_error(token);
//                        break; //退出
                    }
                } else { //如果不是赋值符号 错误处理3: missingAssgn_error
                    missingAssgn_error(token);
//                    break; //退出
                }
            }
            //如果token在ID_Atrribute中出现过，即是标识符
            else if (ID_Attribute.containsKey(token.getToken())) {
                int j = i + 1;
                while (!input.get(j).getToken().equals(";")) //定位到分号所在处-->stmt的终止
                    j++;
                //算术表达式的计算，传入起始下标和终止下标
                computeArithExpr(i, j);
                //更新i
                i = j;
                continue;
            }
            //如果是if语句 进行布尔表达式的计算
            else if (token.getToken().equals("if")) {
                int thenIndex = i + 1; //定位then语句的位置
                while (!input.get(thenIndex).getToken().equals("then"))
                    thenIndex++;
                int elseIndex = thenIndex + 1; //定位else语句的位置
                while (!input.get(elseIndex).getToken().equals("else"))
                    elseIndex++;
                int semiIndex = elseIndex + 1; //定位最后分号的位置
                while (!input.get(semiIndex).getToken().equals(";"))
                    semiIndex++;
                //布尔表达式位于i+2 ~ thenIndex-2(index-1为')')之间，计算结果
                boolean res = computeBoolExpr(i + 2, thenIndex - 2);
                //为true 进入then语句进行计算 thenIndex+1 ~ elseIndex-1
                if (res)
                    computeArithExpr(thenIndex + 1, elseIndex - 1);
                    //否则进入else语句进行计算 elseIndex+1 ~ semiIndex
                else
                    computeArithExpr(elseIndex + 1, semiIndex);
                //更新i
                i = semiIndex;
                continue;
            }
        }
    }

    //错误处理1：强制转换错误
    private static void translate_error(Token token) {
        isError = true; //error标记为true
        error_info.add("error message:line " + token.getLineNum() + ",realnum can not be translated into int type");
    }

    //错误处理2：句末漏分号
    private static void missingSemi_error(Token token) {
        isError = true; //error标记为true
        error_info.add("error message:line " + token.getLineNum() + ",missing \";\" at end of sentence");
    }

    //错误处理3：声明变量时 赋值符号缺失
    private static void missingAssgn_error(Token token) {
        isError = true; //error标记为true
        error_info.add("error message:line " + token.getLineNum() + ",missing assignment symbol");
    }

    /**
     * 计算布尔表达式的值 默认是 a op b的形式？
     * boolop -> < | > | <= | >= | ==
     *
     * @param start 语句的起始位置 a
     * @param end   语句的结束位置 b
     * @return 返回布尔表达式的值
     */
    private static boolean computeBoolExpr(int start, int end) {
        String left = input.get(start).getToken(); //op左边部分 a
        String right = input.get(end).getToken(); //op右边部分 b
        Attribute attr1 = ID_Attribute.get(left);
        Attribute attr2 = ID_Attribute.get(right);
        double value1 = attr1.getValue();
        double value2 = attr2.getValue();

        //boolop -> < | > | <= | >= | == start+1处为operator
        //switch语句
        switch (input.get(start + 1).getToken()) {
            case ">=": {
                return value1 >= value2;
            }
            case "<=": {
                return value1 <= value2;
            }
            case "==": {
                return value1 == value2;
            }
            case "<": {
                return value1 < value2;
            }
            case ">": {
                return value1 > value2;
            }
            default: //注意default
                return false;
        }
    }


    /**
     * 算术表达式的计算
     * 文法给的翻译定义是前缀表达式，但是这里为了简便采用后缀表达式计算
     * 从左到右遍历中缀表达式的每个操作数和操作符
     * 当读到操作数时，立即把它push进后缀表达式
     * 若读到操作符，判断该符号与栈顶符号的优先级，
     * 若该符号优先级高于栈顶元素，则将该操作符入栈，
     * 否则就一次把栈中运算符弹出并加进后缀表达式，到遇到优先级低于该操作符的栈元素，然后把该操作符压入栈中。
     * 如果遇到”(”，直接压入栈中，如果遇到一个”)”，那么就将栈元素弹出并加到后缀表达式尾端，但左右括号并不输出。
     * 最后，如果读到中缀表达式的尾端，将栈元素依次完全弹出并加到后缀表达式尾端。
     *
     * @param start 算术表达式的起始下标
     * @param end   算术表达式的终止下标
     */
    private static void computeArithExpr(int start, int end) {
        //postfixTokens：存储后缀表达式
        List<Token> postfixExpr = new ArrayList<>();
//        List<Token> prefixExpr = new ArrayList<>();
        //opStack：储存运算符的栈
        Stack<Token> opStack = new Stack<>();
        //start为标识符， start+1为'='
        String id = input.get(start).getToken();
        //运算语句从start+2开始
        for (int i = start + 2; i < end; i++) {
            String currentInput = input.get(i).getToken();
            //如果是操作符
            if (isOpr(currentInput)) {
                if (currentInput.equals("(")) //如果是右括号
                    opStack.push(input.get(i)); //直接压入栈
                    //遇到右括号，要把“("与”)”之间的运算符全部pop出来加入后缀表达式中
                else if (currentInput.equals(")")) {
                    while (!opStack.peek().getToken().equals("("))
                        postfixExpr.add(opStack.pop());
                    opStack.pop(); //同时pop出左括号
                } else { //否则 --> 遇到其他运算符
                    //比较操作符与栈顶符号的优先级 如果当前符号优先级更高 那么将其入栈
                    //否则就pop并加入后缀表达式 直至遇到优先级 小于 当前符号的 然后再入栈
                    while (!opStack.empty() && comparePriority(opStack.peek().getToken(), currentInput))
                        postfixExpr.add(opStack.pop());
                    opStack.push(input.get(i));
                }
            } else { //否则 遇到操作数 直接加入后缀表达式
                postfixExpr.add(input.get(i));
            }
        }
        //跳出for循环 说明读完中缀表达式，若符号栈仍不为空 则全部加入后缀表达式
        //当运算符栈不为空
        while (!opStack.empty())
            postfixExpr.add(opStack.pop());
        //后缀表达式的计算 --借助辅助栈numStack
        //numStack：存储运算数的栈
        Stack<Double> numStack = new Stack<>();
        //遍历后缀表达式
        for (Token token : postfixExpr) {
            String s = token.getToken();
            double res = 0.0;
            //遇到运算符（+ - * /），则连续从numStack中pop出2个数组 进行对应的计算 再将计算结果push进numStack
            if (isOpr(s)) {
                switch (s) {
                    case "+": {
                        res = numStack.pop() + numStack.pop();
//                        numStack.push(numStack.pop() + numStack.pop());
                        break;
                    }
                    case "-": { //减法注意取负数
                        res = -(numStack.pop() - numStack.pop());
//                        numStack.push(0 - numStack.pop() + numStack.pop());
                        break;
                    }
                    case "*": {
                        res = numStack.pop() * numStack.pop();
//                        numStack.push(numStack.pop() * numStack.pop());
                        break;
                    }
                    case "/": { //除法注意
                        double divisor = numStack.pop(); //除数
                        double dividend = numStack.pop(); //被除数
                        res = dividend / divisor;
//                        numStack.push(dividend/divisor);
                        if (divisor == 0) {
                            divideZero_error(token);
                        }
                        break;
                    }
                }
                //计算结果push进numStack
                numStack.push(res);
            } else {
                //否则遇到操作数直接push入numStack
                //注意操作数可能不存在与属性表中 如1.0 将属性置为null
                Attribute attribute = ID_Attribute.getOrDefault(token.getToken(), null);
                //若操作数在属性表中，如a,则getValue
                if (attribute != null) {
                    numStack.push(attribute.getValue());
                } else { //如果是数值而不是变量（不出现在ID_Attribute中）
                    numStack.push(Double.parseDouble(token.getToken())); //string 转double
                }
            }
        }
        //遍历完后缀表达式，最后栈里剩下的数就是计算结果，设置为当前id的属性值
//        System.out.println(numbers.peek());
        ID_Attribute.get(id).setValue(numStack.peek());
    }

    //错误处理4：除以0错误
    private static void divideZero_error(Token token) {
        isError = true;
        error_info.add("error message:line " + token.getLineNum() + ",division by zero");
    }

    /**
     * 比较栈顶符号与当前符号的优先级
     *
     * @param stackTop
     * @param currentOp
     * @return
     */
    private static boolean comparePriority(String stackTop, String currentOp) {
        //如果当前符号是乘or除 栈顶是加or减 当前优先级更高 返回false
        if ((currentOp.equals("*") || currentOp.equals("/")) && (stackTop.equals("+") || stackTop.equals("-")))
            return false;
        else //否则是当前符号优先级相等或更低
            return true;
    }

    /**
     * 判断是否是运算符operator
     *
     * @param token
     * @return
     */
    private static boolean isOpr(String token) {
        return operator.contains(token);
    }

    /**
     * 打印结果
     */
    private static void print_res() {
        if (isError) { //如果程序有错误 输出错误结果
            for (String s : error_info) {
                System.out.println(s);
            }
        } else { //否则输出变量名及其数值，中间相隔一个空格
            for (String id : ID_Attribute.keySet()) {
                Attribute attribute = ID_Attribute.get(id);
                System.out.print(id + ": ");
                if (attribute.getType().equals("int")) //如果类型是int 还要把double型的数值重新转换为int
                    System.out.println((int) attribute.getValue());
                else
                    System.out.println(attribute.getValue());
            }
        }
    }

    /**
     * you should add some code in this method to achieve this lab
     */
    private static void analysis() {
        read_prog();
        //词法分析 分词并放入input中
        lexical_analysis();
        //初始化类型系统
        init();
        //开始解析prog
        parse();
        //打印结果
        print_res();
    }


    /**
     * this is the main method
     *
     * @param args
     */
    public static void main(String[] args) {
        analysis();
    }

    //内部类 属性文法
    private static class Attribute {
        String type; //类型
        double value; //数值

        //构造函数
        public Attribute(String type, double value) {
            this.type = type;
            this.value = value;
        }

        public String getType() {
            return type;
        }

        public double getValue() {
            return value;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setValue(double value) {
            this.value = value;
        }
    }

    private static class Token {
        String token; //单词
        int lineNum; //行数

        public Token(String s, int c) {
            token = s;
            lineNum = c;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public int getLineNum() {
            return lineNum;
        }

        public void setLineNum(int lineNum) {
            this.lineNum = lineNum;
        }
    }

}
