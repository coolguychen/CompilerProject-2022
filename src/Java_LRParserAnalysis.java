import java.util.*;

public class Java_LRParserAnalysis {
    private static StringBuffer prog = new StringBuffer();
    // non-terminal set 非终结符
    private static String[] nonTerminal = {"program", "stmt", "compoundstmt",
            "stmts", "ifstmt", "whilestmt",
            "assgstmt", "boolexpr", "boolop",
            "arithexpr", "arithexprprime", "multexpr",
            "multexprprime", "simpleexpr"};

    // 获取在nonTerminal中的下标
    private static int getFromNonTerminal(String token) {
        for (int i = 0; i < nonTerminal.length; i++) {
            if (nonTerminal[i].equals(token)) return i;
        }
        return -1;
    }

    private static boolean isNonTerminal(String follow) {
        return getFromNonTerminal(follow) >= 0;
    }

    // terminal 终结符
    private static String[] terminal = {"{", "}", "if", "(", ")", "then", "else",
            "while", "ID", "=", ";", "<", ">", "<=", ">=",
            "==", "+", "-", "*", "/", "NUM", "$"};

    // 获取在terminal中的下标
    private static int getFromTerminal(String token) {
        for (int i = 0; i < terminal.length; i++) {
            if (terminal[i].equals(token)) return i;
        }
        return -1;
    }

    private static boolean isTerminal(String token) {
        return getFromTerminal(token) >= 0;
    }

    // grammar 文法
    private static String[] grammar = {
            "program -> compoundstmt",
            "stmt -> ifstmt",
            "stmt -> whilestmt",
            "stmt -> assgstmt",
            "stmt -> compoundstmt",
            "compoundstmt -> { stmts }",
            "stmts -> stmt stmts",
            "stmts -> E",
            "ifstmt -> if ( boolexpr ) then stmt else stmt",
            "whilestmt -> while ( boolexpr ) stmt",
            "assgstmt -> ID = arithexpr ;",
            "boolexpr -> arithexpr boolop arithexpr",
            "boolop -> <",
            "boolop -> >",
            "boolop -> <=",
            "boolop -> >=",
            "boolop -> ==",
            "arithexpr -> multexpr arithexprprime",
            "arithexprprime -> + multexpr arithexprprime",
            "arithexprprime -> - multexpr arithexprprime",
            "arithexprprime -> E",
            "multexpr -> simpleexpr multexprprime",
            "multexprprime -> * simpleexpr multexprprime",
            "multexprprime -> / simpleexpr multexprprime",
            "multexprprime -> E",
            "simpleexpr -> ID",
            "simpleexpr -> NUM",
            "simpleexpr -> ( arithexpr )"
    };

    /**
     * 产生式左边的非终结符
     *
     * @param s
     * @return
     */
    private static String get_nonTerminal(String s) {
        String res;
        int j = s.indexOf('-');
        //在->左边的是非终结符
        res = s.substring(0, j - 1);
        return res;
    }

    /**
     * 产生式右边部分
     *
     * @param s
     * @return
     */
    private static String get_right(String s) {
        int start = s.indexOf('>') + 2;
        int end = s.length();
        String right = s.substring(start, end);
        return right;
    }


    //状态
    enum actionState {ACCEPT, SHIFT, REDUCE}

    //action table 行为状态 列为终结符 & $ 项为动作
    private static Map<Integer, Map<String, String[]>> actionTable = new HashMap<>();

    //goto table 行为状态，列为非终结符，项为转到的状态
    private static Map<Integer, Map<String, Integer>> gotoTable = new HashMap<>();

    //错误处理
    private static int line = 0;
    //记录每行的token数
    private static int[] line_token;

    /**
     * this method is to read the standard input
     */
    private static void read_prog() {
        Scanner sc = new Scanner(System.in);
        while (sc.hasNextLine()) {
            prog.append(sc.nextLine());
            prog.append('\n'); // 每行后面加上换行符
            line++;
        }
//        prog.append("{\n" +
//                "ID = NUM \n" +
//                "}");
//
//        line = 3;
        //设置行数
        line_token = new int[line];
    }


    // add your method here!!

    /**
     * 事先设计好的LR Table（通过LR Table Generator生成 并转为二维数组）
     */
    private static String[][] action_table = {
            // {	}	if	(	)	then	else	while	ID	=	;	<	>	<=	>=	==	+	-	*	/	NUM	$
            {"s2", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "acc"},
            {"s12", "r7", "s9", "null", "null", "null", "null", "s10", "s11", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "s13", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"s12", "r7", "s9", "null", "null", "null", "null", "s10", "s11", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"r1", "r1", "r1", "null", "null", "null", "null", "r1", "r1", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"r2", "r2", "r2", "null", "null", "null", "null", "r2", "r2", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"r3", "r3", "r3", "null", "null", "null", "null", "r3", "r3", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"r4", "r4", "r4", "null", "null", "null", "null", "r4", "r4", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "s15", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "s16", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "s17", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"s12", "r7", "s9", "null", "null", "null", "null", "s10", "s11", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r5"},
            {"null", "r6", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "s25", "null", "null", "null", "null", "s23", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "s24", "null"},
            {"null", "null", "null", "s25", "null", "null", "null", "null", "s23", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "s24", "null"},
            {"null", "null", "null", "s32", "null", "null", "null", "null", "s30", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "s31", "null"},
            {"null", "s33", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "s34", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "s36", "s37", "s38", "s39", "s40", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r20", "r20", "r20", "r20", "r20", "s42", "s43", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r24", "r24", "r24", "r24", "r24", "r24", "r24", "s45", "s46", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r25", "r25", "r25", "r25", "r25", "r25", "r25", "r25", "r25", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r26", "r26", "r26", "r26", "r26", "r26", "r26", "r26", "r26", "null", "null"},
            {"null", "null", "null", "s52", "null", "null", "null", "null", "s50", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "s51", "null"},
            {"null", "null", "null", "null", "s53", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "s54", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r20", "null", "null", "null", "null", "null", "s56", "s57", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r24", "null", "null", "null", "null", "null", "r24", "r24", "s59", "s60", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r25", "null", "null", "null", "null", "null", "r25", "r25", "r25", "r25", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r26", "null", "null", "null", "null", "null", "r26", "r26", "r26", "r26", "null", "null"},
            {"null", "null", "null", "s52", "null", "null", "null", "null", "s50", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "s51", "null"},
            {"r5", "r5", "r5", "null", "null", "null", "null", "r5", "r5", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "s62", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "s52", "null", "null", "null", "null", "s50", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "s51", "null"},
            {"null", "null", "null", "r12", "null", "null", "null", "null", "r12", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r12", "null"},
            {"null", "null", "null", "r13", "null", "null", "null", "null", "r13", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r13", "null"},
            {"null", "null", "null", "r14", "null", "null", "null", "null", "r14", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r14", "null"},
            {"null", "null", "null", "r15", "null", "null", "null", "null", "r15", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r15", "null"},
            {"null", "null", "null", "r16", "null", "null", "null", "null", "r16", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r16", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r17", "r17", "r17", "r17", "r17", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "s25", "null", "null", "null", "null", "s23", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "s24", "null"},
            {"null", "null", "null", "s25", "null", "null", "null", "null", "s23", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "s24", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r21", "r21", "r21", "r21", "r21", "r21", "r21", "null", "null", "null", "null"},
            {"null", "null", "null", "s25", "null", "null", "null", "null", "s23", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "s24", "null"},
            {"null", "null", "null", "s25", "null", "null", "null", "null", "s23", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "s24", "null"},
            {"null", "null", "null", "null", "s68", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "r20", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "s70", "s71", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "r24", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r24", "r24", "s73", "s74", "null", "null"},
            {"null", "null", "null", "null", "r25", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r25", "r25", "r25", "r25", "null", "null"},
            {"null", "null", "null", "null", "r26", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r26", "r26", "r26", "r26", "null", "null"},
            {"null", "null", "null", "s52", "null", "null", "null", "null", "s50", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "s51", "null"},
            {"s12", "null", "s9", "null", "null", "null", "null", "s10", "s11", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"r10", "r10", "r10", "null", "null", "null", "null", "r10", "r10", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r17", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "s32", "null", "null", "null", "null", "s30", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "s31", "null"},
            {"null", "null", "null", "s32", "null", "null", "null", "null", "s30", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "s31", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r21", "null", "null", "null", "null", "null", "r21", "r21", "null", "null", "null", "null"},
            {"null", "null", "null", "s32", "null", "null", "null", "null", "s30", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "s31", "null"},
            {"null", "null", "null", "s32", "null", "null", "null", "null", "s30", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "s31", "null"},
            {"null", "null", "null", "null", "s81", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"s90", "null", "s87", "null", "null", "null", "null", "s88", "s89", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "r11", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r20", "r20", "r20", "r20", "r20", "s42", "s43", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r20", "r20", "r20", "r20", "r20", "s42", "s43", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r24", "r24", "r24", "r24", "r24", "r24", "r24", "s45", "s46", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r24", "r24", "r24", "r24", "r24", "r24", "r24", "s45", "s46", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r27", "r27", "r27", "r27", "r27", "r27", "r27", "r27", "r27", "null", "null"},
            {"null", "null", "null", "null", "r17", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "s52", "null", "null", "null", "null", "s50", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "s51", "null"},
            {"null", "null", "null", "s52", "null", "null", "null", "null", "s50", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "s51", "null"},
            {"null", "null", "null", "null", "r21", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r21", "r21", "null", "null", "null", "null"},
            {"null", "null", "null", "s52", "null", "null", "null", "null", "s50", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "s51", "null"},
            {"null", "null", "null", "s52", "null", "null", "null", "null", "s50", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "s51", "null"},
            {"null", "null", "null", "null", "s99", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"r9", "r9", "r9", "null", "null", "null", "null", "r9", "r9", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r20", "null", "null", "null", "null", "null", "s56", "s57", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r20", "null", "null", "null", "null", "null", "s56", "s57", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r24", "null", "null", "null", "null", "null", "r24", "r24", "s59", "s60", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r24", "null", "null", "null", "null", "null", "r24", "r24", "s59", "s60", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r27", "null", "null", "null", "null", "null", "r27", "r27", "r27", "r27", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "s104", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "r1", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "r2", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "r3", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "r4", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "s105", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "s106", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "s107", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"s12", "r7", "s9", "null", "null", "null", "null", "s10", "s11", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r18", "r18", "r18", "r18", "r18", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r19", "r19", "r19", "r19", "r19", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r22", "r22", "r22", "r22", "r22", "r22", "r22", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r23", "r23", "r23", "r23", "r23", "r23", "r23", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "r20", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "s70", "s71", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "r20", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "s70", "s71", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "r24", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r24", "r24", "s73", "s74", "null", "null"},
            {"null", "null", "null", "null", "r24", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r24", "r24", "s73", "s74", "null", "null"},
            {"null", "null", "null", "null", "r27", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r27", "r27", "r27", "r27", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r18", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r19", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r22", "null", "null", "null", "null", "null", "r22", "r22", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r23", "null", "null", "null", "null", "null", "r23", "r23", "null", "null", "null", "null"},
            {"s12", "null", "s9", "null", "null", "null", "null", "s10", "s11", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "s25", "null", "null", "null", "null", "s23", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "s24", "null"},
            {"null", "null", "null", "s25", "null", "null", "null", "null", "s23", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "s24", "null"},
            {"null", "null", "null", "s32", "null", "null", "null", "null", "s30", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "s31", "null"},
            {"null", "s117", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "r18", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "r19", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "r22", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r22", "r22", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "r23", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "r23", "r23", "null", "null", "null", "null"},
            {"r8", "r8", "r8", "null", "null", "null", "null", "r8", "r8", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "s118", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "s119", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "s120", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "r5", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "s121", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"s90", "null", "s87", "null", "null", "null", "null", "s88", "s89", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "r10", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"s90", "null", "s87", "null", "null", "null", "null", "s88", "s89", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "r9", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "s124", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"s90", "null", "s87", "null", "null", "null", "null", "s88", "s89", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"},
            {"null", "null", "null", "null", "null", "null", "r8", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"}
    };

    private static int[][] goto_table = {
            {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 4, 8, 3, 5, 6, 7, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 4, 8, 14, 5, 6, 7, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 4, 8, 18, 5, 6, 7, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 19, 0, 20, 0, 21, 0, 22, 22},
            {0, 0, 0, 0, 0, 0, 0, 26, 0, 20, 0, 21, 0, 22, 22},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 27, 0, 28, 0, 29, 29},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 41, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 44, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 47, 0, 48, 0, 49, 49},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 55, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 58, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 61, 0, 48, 0, 49, 49},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 63, 0, 48, 0, 49, 49},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 0, 22, 22},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 65, 0, 22, 22},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 66, 66},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 67, 67},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 69, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 72, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 75, 0, 48, 0, 49, 49},
            {0, 76, 8, 0, 5, 6, 7, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 77, 0, 29, 29},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 78, 0, 29, 29},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 79, 79},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 80, 80},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 82, 86, 0, 83, 84, 85, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 91, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 92, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 93, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 94, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 95, 0, 49, 49},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 96, 0, 49, 49},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 97, 97},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 98, 98},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 100, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 101, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 102, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 103, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 4, 8, 108, 5, 6, 7, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 109, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 110, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 111, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 112, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 113, 8, 0, 5, 6, 7, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 114, 0, 20, 0, 21, 0, 22, 22},
            {0, 0, 0, 0, 0, 0, 0, 115, 0, 20, 0, 21, 0, 22, 22},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 116, 0, 28, 0, 29, 29},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 122, 86, 0, 83, 84, 85, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 123, 86, 0, 83, 84, 85, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 125, 86, 0, 83, 84, 85, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    };


    //input用来存储输入流
    private static List<String> input = new ArrayList<String>();
    //符号栈
    private static Stack<String> tokenStack;
    //状态栈
    private static Stack<Integer> stateStack;

    //输出流
    private static List<String> result = new ArrayList<>();

    //输出结果
    //注意：每一组串之间均有一个空格符相隔开，分号，括号，=>符号前后均有一个空格符隔开，每一句推导只占一行
    private static void print_res() {
        //因为是从底至上 要输出最右输出 还是需要从上至下 所以将列表转置
        Collections.reverse(result);
//        for (String s : result) {
//            System.out.println(s);
//        }
        Stack<String> output = new Stack<>();
        //一定是从开始符开始 因此初始化输出栈为program & compundstmt
        String start_left = get_nonTerminal(result.get(0));
        String start_right = get_right(result.get(0));
        output.push(start_left);
        output.push(start_right);
        System.out.print(start_left + " => \n" + start_right + " => \n");
        for (int i = 1; i < result.size(); i++) {
            String production = result.get(i);
            //获取表达式的左边&右边
            String left = get_nonTerminal(production);
            String right = get_right(production);
            String top = output.peek();//栈顶元素
            if (top.equals(left)) {
                output.pop(); //栈顶弹出 进行替换
                output.push(right);
            } else if (top.contains(left)) {
                //用右边的去替换
                //replace
                replace(output, left, right);
            }
            //输出栈顶
            if (i != result.size() - 1) System.out.print(output.peek() + " => \n");
            else System.out.print(output.peek()); //由于最后一项已经结束推导 因此无 =>
        }
    }

    private static void replace(Stack<String> output, String left, String right) {
        String top = output.pop();//栈顶元素
        int index = top.lastIndexOf(left); //自右向左 找到匹配的下标 因为是最右推导
        int len = left.length();
        String new_string;
        if (right.equals("E")) { //若右边为空
            new_string = top.substring(0, index) + top.substring(index + len + 1);
        } else {
            new_string = top.substring(0, index) + right + top.substring(index + len);
        }
        //新字符串push进去
        output.push(new_string);
    }


    /**
     * 初始化符号栈 & 状态栈
     */
    private static void init() {
        int currentLine = 1; //当前行号为1
        for (int i = 0; i < prog.length(); i++) {
            int index;
            if (prog.charAt(i) == '\n') currentLine++; //检索到换行符 行号+1
            //分词
            if (prog.charAt(i) != ' ' && prog.charAt(i) != '\t' && prog.charAt(i) != '\n') {
                String tmp = null;
                for (index = i + 1; index <= prog.length(); index++) {
                    //截取token
                    tmp = prog.substring(i, index);
                    //匹配非终结符 如果是终结符
                    if (isTerminal(tmp)) {
                        break; //跳出循环
                    }
                }
                //截取到== >= <=
                if ((tmp.equals("=") && prog.charAt(index) == '=') || (tmp.equals(">") && prog.charAt(index) == '=') || (tmp.equals("<") && prog.charAt(index) == '=')) {
                    index++;
                    tmp = prog.substring(i, index);
                }
                //更新i
                i = index - 1;
                //加入输入流中
                input.add(tmp);
                //该行单词数++
                line_token[currentLine - 1]++;
            }
        }
        //输入流最后要加上界符
        input.add("$");
//        test:
//        for (String s : input) {
//            System.out.println(s);
//        }
        //初使化栈
        tokenStack = new Stack<>();
        stateStack = new Stack<>();
        //状态栈初始化 放入0
        stateStack.push(0);
    }

    /**
     * 读入用户输入的待分析的表达式，依次移动指针，如果根据当前栈顶的情况和读头下的符号来决定当前的动作。
     * 根据当前状态栈顶的状态号和读头下的符号查 parsing table，如果查到的是 Si 即 Shift 操作，则将读头下的符号压入符号栈，将 i 压入状态栈，
     * 移动读头到下一个字符；如果查到的是 ri 即 Reduce 操作，则将栈顶的所有与文法表达式 i右边相同的部分一次弹出，同时将状态栈一起弹出，
     * 将文法表达式 i 左边的符号 X 压入符号栈，查当前状态栈栈顶的项 I 在 GOTO 表当中的 GOTO(I,X)，将所得的状态号压入状态栈；
     * 如果读到 r0，则分析成功；如果有其他读入，则不合法
     */

    private static void parse() {
        int index = 0; //指针 指向input中的单词
        while (true) {
            //栈顶节点
            int state_top = stateStack.peek(); //状态栈栈顶元素
            //指针指向的单词
            String current_input = input.get(index);
            int j = getFromTerminal(current_input); //获取对应的终结符下标
            String item = action_table[state_top][j]; //状态栈顶 & 输入字符 对应的actionTable表项
            if (item.charAt(0) == 's') {
                //若动作为shift
                //如果查到的是 Si 即 Shift 操作，则将读头下的符号压入符号栈，将 i 压入状态栈，移动读头到下一个字符；
                String next = item.substring(1); //状态转移
                tokenStack.push(current_input);
                stateStack.push(Integer.parseInt(next)); //将i压入状态栈
                index++;
            } else if (item.charAt(0) == 'r') {
                //若动作为reduce
                String next = item.substring(1); //用第几条产生式进行归约
                //如果查到的是 ri 即 Reduce 操作，则将栈顶的所有与文法表达式i右边相同的部分一次弹出，同时将状态栈一起弹出，
                // 将文法表达式 i 左边的符号 X 压入符号栈，查当前状态栈栈顶的项 I 在 GOTO 表当中的 GOTO(I,X)，将所得的状态号压入状态栈；
                int num = Integer.parseInt(next);
                if (num == 0) break; //如果index是0 r0 解析成功？
                String production = grammar[num];
                //获取表达式右边的部分
                String right = get_right(production);
                int length = -1;
                if (right.equals("E")) length = 0; //归约为空 特殊处理
                else {
                    String[] tmp = right.split(" "); //分词
                    length = tmp.length; //右边token数量
                }
                for (int i = 0; i < length; i++) {
                    //同时弹出token & 状态（成对pop）
                    tokenStack.pop();
                    stateStack.pop();
                }
                //栈顶元素
                state_top = stateStack.peek();
                String nonTerminal = get_nonTerminal(production); //获取左边非终结符 X
                //压入符号栈
                tokenStack.push(nonTerminal);
                //goto table中找 goto[i,X]项
                int next_state = goto_table[state_top][getFromNonTerminal(nonTerminal)]; //栈顶 + 非终结符对应下标
                //将所得的状态号压入状态栈
                stateStack.push(next_state);
                //同时 将归约的表达式放入result
                result.add(production);
            } else if (item == "acc") {
                //动作为accept 解析成功 退出循环
                break;
            } else if (item == "null") { //表项为空 错误处理
                error_handle(index);
                index = 0;
                continue; //重新解析
            }
        }
        //输出流一定是先从program开始 因此最后要加上grammar的开始符
        result.add(grammar[0]);

    }

    private static void error_handle(int index) {
        int sum = 0; //单词数
        int error_line = 0;
        //对每行进行遍历 定位到出错位置
        for (; error_line < line_token.length; error_line++) {
            sum += line_token[error_line];
            if (sum >= index)
                break;
        }
        error_line++; //因为line从0开始 注意+1
        //这里只考虑到了缺少分号的情况 至于其他的错误类型还未进行考虑
        System.out.println("语法错误，第" + error_line + "行，缺少\";\"");
        //加入";" 继续解析
        input.add(index, ";");
        //清空result & stateStack & tokenStack 重新parse
        result.clear();
        stateStack.clear();
        tokenStack.clear();
        stateStack.push(0); //初始化状态栈
    }


    /**
     * you should add some code in this method to achieve this lab
     */
    private static void analysis() {
        read_prog();
        //初始化Parser
        init();
        //开始解析
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

}


