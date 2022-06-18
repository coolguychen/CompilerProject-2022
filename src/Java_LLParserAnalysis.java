import java.util.*;

public class Java_LLParserAnalysis {
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
            "while", "ID", "=", ">", "<", ">=", "<=",
            "==", "+", "-", "*", "/", "NUM", ";"};

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


    // add your method here!!

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


    //切分产生式到map中
    private static Map<String, List<String>> production = new HashMap<>();

    private static void generate_production(String[] grammar) {
        for (String s : grammar) {
            String left = get_nonTerminal(s); //产生式左边部分 非终结符
            String right = get_right(s);
            List<String> pro = new ArrayList<>();
            //如果包含该非终结符的产生式 更新value
            if (production.containsKey(left)) {
                pro = production.get(left);
            }
            pro.add(right); //将产生式加进来
            //更新map
            production.put(left, pro);
        }
    }


    //FIRST集
    private static Map<String, Set<String>> firstSet = new HashMap<>();
    //FOLLOW集
    private static Map<String, Set<String>> followSet = new HashMap<>();


    /**
     * 对每条产生式 计算非终结符的first集
     * 放入 Map<String, Set<String>> firstSet 中
     */
    private static void generate_first() {
        for (Map.Entry<String, List<String>> entry : production.entrySet()) {
            String nonTerminal = entry.getKey(); //非终结符
            Set<String> first_set = new HashSet<>();
            searchFirstTerm(nonTerminal, first_set);
            firstSet.put(nonTerminal, first_set);
        }
//        print_firstSet();
    }

    /**
     * 递归找first集
     *
     * @param nonTerminal
     * @param set
     */
    private static void searchFirstTerm(String nonTerminal, Set<String> set) {
        List<String> right = production.get(nonTerminal); //产生式右边 列表
        for (String s : right) {
            String[] tmp = s.split(" "); //以空格分割
            String firstTerm = tmp[0];
            //首字符是非终结符，则继续递归查找
            if (production.containsKey(firstTerm)) {
                searchFirstTerm(firstTerm, set);
            }
            // 若不是则直接添加进FIRST集合
            else {
                set.add(firstTerm);
            }
        }
    }

    /**
     * 打印first集的结果
     */
    private static void print_firstSet() {
        for (Map.Entry<String, Set<String>> entry : firstSet.entrySet()) {
            System.out.println(entry.getKey() + "的FIRST集：" + entry.getValue().toString());
        }
    }

    /**
     * 对每条产生式 计算非终结符的follow集
     * 首先，在右侧式子中寻找在非终结符集中存在的非终结符，找到非终结符后，判断非终结符后面是否有元素
     * 如果非终结符后面没有元素或者后面元素是非终结符且该非终结符有ε，直接将该式子的Follow集直接添加进所找非终结符的Follow集中
     * 如果非终结符后面有元素，判断该元素是否是非终结符
     * 如果是终结符的话，则直接往Follow集里面添加，
     * 如果是非终结符的话就往下递归查找该非终结符的里面的元素
     */
    private static void generate_follow() {
        //循环直至follow_set不变
        //两次循环 第二次补齐follow_set
        for (int i = 0; i < 2; i++) {
            for (Map.Entry<String, List<String>> entry : production.entrySet()) {
                String nonTerminal = entry.getKey(); //非终结符
                Set<String> follow_set = new HashSet<>(); //非终结符对应的FOLLOW集
                if (nonTerminal != "program") {
                    follow_set = searchFollowTerm(nonTerminal, follow_set);
                }
                followSet.put(nonTerminal, follow_set);
            }
        }
        //打印结果
//        print_followSet();
    }

    private static void print_followSet() {
        for (Map.Entry<String, Set<String>> entry : followSet.entrySet()) {
            System.out.println(entry.getKey() + "的FOLLOW集：" + entry.getValue().toString());
        }
    }


    /**
     * 查找目标非终结符的FOLLOW
     * 查找结果可能是终结符也可能是非终结符
     *
     * @param nonTerminal
     */
    private static Set<String> searchFollowTerm(String nonTerminal, Set<String> set) {
        if (nonTerminal.equals("program")) {
            // 首先在开始符号的FOLLOW集中加入界符
            set.add("$");
        }
        //遍历产生式右边部分
        for (Map.Entry<String, List<String>> entry : production.entrySet()) {
            //查找该非终结符在右边出现的位置
            List<String> right = entry.getValue();
            //遍历右边的每条产生式 看是否包含nonTerminal
            for (String s : right) {
                int index = -1; //下标记录位置,index>=0说明找到非终结符位置
                if (s.indexOf(nonTerminal) >= 0) {
                    String[] tmp = s.split(" "); //产生式-划分单词
                    for (int i = 0; i < tmp.length; i++) {
                        if (tmp[i].equals(nonTerminal)) { //找到终结符的位置
                            index = i;
                            break;
                        }
                    }
                    //index>=0 说明找到非终结符在右边产生式的位置
                    if (index >= 0) {
                        //如果终结符在产生式的末尾
                        if (index == tmp.length - 1) {
                            //将FOLLOW(A)加入FOLLOW(B)
                            String left = entry.getKey(); //左边的非终结符
                            //递归地找FOLLOW集
                            //stmts -> stmt stmts 死循环?
                            //终止条件？循环直至set不变
                            if (left.equals(nonTerminal)) break;
                            if (followSet.containsKey(left)) { //此时已计算出left的FOLLOW集 直接加进去
                                set.addAll(followSet.get(left));
                            }
                        }
                        //否则看非终结符后跟着什么
                        else {
                            String follow = tmp[++index];
                            //如果是非终结符
                            if (isNonTerminal(follow)) {
                                //查看其FIRST集里是否包含空串
                                if (isContainEmptySet(follow)) {
                                    //如果包含空串
                                    //就将其FIRST集除去空串加入FOLLOW集
                                    Set<String> setExceptNull = eliminateEmptySet(follow);
                                    set.addAll(setExceptNull); //FIRST(follow)-E 加入 FOLLOW(nonTerminal)
                                    //如果此时这个符号是最后一个符号,还要左部的FOLLOW集加入FOLLOW集
                                    if (index == tmp.length - 1) {
                                        // FOLLOW(A)加入FOLLOW(B)
                                        String left = entry.getKey(); //左边的非终结符
                                        //递归将FOLLOW集加入
                                        if (followSet.containsKey(left)) { //此时已计算出left的FOLLOW集 直接加进去
                                            set.addAll(followSet.get(left));
                                        } else {
                                            searchFollowTerm(left, set);
                                        }
                                    }
                                }
                                //若follow的FIRST集不含空串 直接加入FOLLOW集
                                else {
                                    set.addAll(firstSet.get(follow));
                                }
                            }
                            //如果是终结符 直接加入follow集
                            else if (isTerminal(follow)) {
                                set.add(follow);
                            }
                        }
                    }

                }
            }
        }
        return set;
    }

    /**
     * 消除First集中的ε
     *
     * @param nonTerminal 目标非终结符对应的式子
     * @return 返回消除ε后的集合
     */
    private static Set<String> eliminateEmptySet(String nonTerminal) {
        Set<String> set = firstSet.get(nonTerminal);
        Set<String> result = new HashSet<>();
        //去除First集中的ε
        for (String character : set) {
            if (!character.equals("E")) {
                result.add(character);
            }
        }
        return result;
    }

    /**
     * 查看相应First集中是否存在ε
     *
     * @param nonTerminal 目标非终结符对应的式子
     * @return 存在则是true，否则是false
     */
    private static boolean isContainEmptySet(String nonTerminal) {
        Set<String> set = firstSet.get(nonTerminal);
        for (String string : set) {
            if (string.equals("E")) {
                return true;
            }
        }
        return false;
    }

    //二维数组
    //Integer表示使用的是grammar中第几条产生式
    private static Map<String, Map<String, Integer>> LL_table = new HashMap<>();

    /**
     * 构建LL1 Table
     * 预测分析表的构造方法
     * 1、对文法G的每个产生式 A→α 执行第2步 和第3步；
     * 2、对每个终结符a ∈ FIRST(α)，把A→α加至M[A,a]中，
     * 3、若ɛ ∈ FIRST(α)，则对任何b∈FOLLOW(A)，把A→α加至M[A,b]中，
     * 4、把所有无定义的M[A,a]标上“出错标志”。
     */
    private static void construct_table() {
        //遍历G的每个产生式
        for (int i = 0; i < grammar.length; i++) {
            //G中第i条产生式
            String production = grammar[i];
            String left = get_nonTerminal(production); //非终结符
            String right = get_right(production); //右边产生式
            //计算右边产生式的FIRST集
            Set<String> first = calculateFirst(right);
            Map<String, Integer> map;
            //判断是否包含空 如果为空 对任何 b ∈ FOLLOW(A)，把A→α加至M[A,b]中，
            if (first.contains("E")) {
                Set<String> follow = followSet.get(left);
                map = addItem(follow, i + 1); //注意下标+1 保证有产生式的项 >0
            } else { //对每个终结符a ∈ FIRST(α)，把A→α加至M[A,a]中，
                map = addItem(first, i + 1); //i+1
            }
            //若map中已经含有该key
            if (LL_table.containsKey(left)) {
                //更新value
                Map<String, Integer> value = LL_table.get(left);
                value.putAll(map);
                LL_table.put(left, value);
            } else {
                LL_table.put(left, map);
            }
            transition(); //转二位数组
        }
//        print_LLTable();
    }

    private static void print_LLTable() {
        for (Map.Entry<String, Map<String, Integer>> entry : LL_table.entrySet()) {
            String nonTerminal = entry.getKey();
            Map<String, Integer> map = entry.getValue();
            for (Map.Entry<String, Integer> item : map.entrySet()) {
                System.out.println(nonTerminal + " + " + item.getKey() + " : " + grammar[item.getValue() - 1]); //注意这里-1
            }
            System.out.println("-------------------");
        }
    }

    private static int[][] LL1_table = new int[nonTerminal.length][terminal.length];

    //Map转二维数组，便于后续检索
    private static void transition() {
        for (Map.Entry<String, Map<String, Integer>> entry : LL_table.entrySet()) {
            String nonTerminal = entry.getKey();
            int i = getFromNonTerminal(nonTerminal);
//            System.out.println(nonTerminal + " " + i);
            Map<String, Integer> map = entry.getValue();
            for (Map.Entry<String, Integer> item : map.entrySet()) {
                String terminal = item.getKey();
                int j = getFromTerminal(terminal);
                LL1_table[i][j] = item.getValue();
            }
        }
    }

    /**
     * 计算产生式右边部分的FIRST集
     *
     * @param right
     * @return
     */
    private static Set<String> calculateFirst(String right) {
        Set<String> set = new HashSet<>();
        String[] s = right.split(" ");
        String first = s[0];
        //若first为终结符或者空集 直接加入
        if (isTerminal(first) || first.equals("E")) {
            set.add(first);
        } else { //非终结符 返回first集
            set.addAll(firstSet.get(first));
        }
        return set;
    }

    /**
     * 将产生式加入到对应的表项中
     *
     * @param set
     * @param index
     * @return
     */
    private static Map<String, Integer> addItem(Set<String> set, int index) {
        //对于集合中的非终结符
        Map<String, Integer> map = new HashMap<>();
        for (String s : set) {
            map.put(s, index);
        }
        return map;
    }

    //input用来存储输入流
    private static List<String> input = new ArrayList<String>();
    //辅助栈
    private static Stack<Node> nodeStack = new Stack<Node>();

    //错误处理
    private static int line = 0;
    //记录每行的token数
    private static int[] line_token;

    /**
     * this method is to read the standard input
     */
    private static void read_prog() {
//        Scanner sc = new Scanner(System.in);
//        while(sc.hasNextLine())
//        {
//            prog.append(sc.nextLine());
//            prog.append('\n'); // 每行后面加上换行符
//            line++;
//        }
        prog.append("{\n" +
                "ID = NUM \n" +
                "}");

        line = 3;
        //设置行数
        line_token = new int[line];
    }


    //输出流
    private static List<Node> result = new ArrayList<Node>();

    //输出结果
    private static void print_res() {
        Node root;
        for (int i = 0; i < result.size(); i++) {
            root = result.get(i);
            for (int j = 0; j < root.level; j++)
                //层级数 tab键
                System.out.print('\t');
            if (i != result.size() - 1)
                System.out.println(root.token);
            else
                System.out.print(root.token);
        }

    }

    /**
     * 初始化，对输入流进行分词
     */
    private static void initial() {
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
//                        success = true; //匹配成功
                        break; //跳出循环
                    }
//                    if (success) {
//                        break;
//                    }
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
        for(String s: input) {
            System.out.println(s);
        }
        //初使化栈：压入开始符和终止符
        nodeStack.push(new Node("$", -1));
        nodeStack.push(new Node("program", 0));
    }


    private static void parse() {
        Node top;//栈顶节点
        int index = 0; //指针 指向input中的单词

        while (true) {
            //栈顶节点
            String top_element = nodeStack.peek().token;
            //指针指向的单词
            String current_input = input.get(index);

            int i = 0, j = 0;//查找表项
            //如果栈顶是非终结符
            if (isNonTerminal(top_element)) {
                //栈顶在非终结符数组的下标
                i = getFromNonTerminal(top_element);
            }
            //单词在终结符数组的下标
            if (isTerminal(current_input)) {
                j = getFromTerminal(current_input);
            }

            //如果是非终结符 & 单词 ==> 查表
            if(i >=0 && j >= 0){
                if (LL1_table[i][j] == 0)//表项为空。发现错误。
                {
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
                    System.out.println("语法错误,第" + error_line + "行,缺少\";\"");
                    //加入";" 继续解析
                    input.add(index, ";");
                    //清空result & stack 重新parse
                    result.clear();
                    nodeStack.clear();
                    nodeStack.add(new Node("$", -1));
                    nodeStack.add(new Node("program", 0));
                    index = 0;
                    continue; //重新解析
                }
            }

            //1. When X = a = $, 栈顶元素与a匹配，且a恰好是终止符($)，程序结束，成功分析；
            //2. When X = a ≠ $, 栈顶元素与终结符a匹配，pop X off stack, 继续扫描后续字符；
            //if M[X,a] = {X → UVW}, POP X, PUSH W,V,U(最左推导，注意这里是倒序push)
            int length = 0;
            String production = grammar[LL1_table[i][j] - 1]; //注意-1
//            String left = get_nonTerminal(production);
            String right = get_right(production);
            String[] produce = right.split(" "); //右边部分 分词
            if (produce[0].equals("E")) { //如果右边为空 直接压入栈 length=0
                length = 0;
            } else { //否则获取右边单词长度，倒序压进去
                length = produce.length;
            }

            top = nodeStack.pop(); //取出栈顶节点
            //加入输出列表
            result.add(top);

            if (length == 0) {
                //如果是空 直接压入栈 层级++
                result.add(new Node("E", top.level + 1));
            }

            while (length > 0)//产生式倒序压栈
            {
                //倒序 将右边的压进栈
                //层级++
                nodeStack.push(new Node(produce[length - 1], top.level + 1));
                length--;
            }

            ///匹配字符
            while (nodeStack.size() != 1 && index != input.size() - 1) {
                //When X = a ≠ $, 栈顶元素与终结符a匹配，pop X off stack, 继续扫描后续字符；
                if (nodeStack.peek().token.equals(input.get(index))) {
                    result.add(nodeStack.pop());
                    index++; //继续向后找
                } else break;
            }
            //当栈只剩下"$" 输入流只剩最后一个字符"$" 匹配 结束分析
            if (nodeStack.size() == 1 && index == input.size() - 1)
                break;
        }
    }

    /**
     * you should add some code in this method to achieve this lab
     */
    private static void analysis() {
        read_prog();
        //产生式
        generate_production(grammar);
        //生成FIRST集
        generate_first();
        //生成FOLLOW集
        generate_follow();
        //生成LL(1) Table
        construct_table();
        //初始化
        initial();
        //开始解析
        parse();
        //输出结果
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

    /**
     * 内部类 Node 用于存储词的层级
     */
    private static class Node {
        String token;
        int level;

        //构造函数
        public Node(String s, int l) {
            token = s;
            level = l;
        }
    }
}
