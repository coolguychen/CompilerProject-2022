import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static java.lang.Character.isAlphabetic;
import static java.lang.Character.isDigit;

public class Java_LexAnalysis {

    private static StringBuffer prog = new StringBuffer();

    /**
     * this method is to read the standard input
     */
    private static void read_prog() {
        Scanner sc = new Scanner(System.in);
        while(sc.hasNextLine())
        {
            prog.append(sc.nextLine());
        }
        //test
//        prog.append("int main()\n" +
//                "\n" +
//                "{\n" +
//                "\n" +
//                "\tprintf(\"HelloWorld\");\n" +
//                "\n" +
//                "\treturn 0;\n" +
//                "\n" +
//                "\t}");
//        prog.append("int main()\n" +
//                "{\n" +
//                "\tint i = 0;// 注释 test\n" +
//                "\tfor (i = 0; i != 10; ++i)\n" +
//                "\t{\n" +
//                "\t\tprintf(\"%d\",i);\n" +
//                "\t}\n" +
//                "\treturn 0;\n" +
//                "}");
    }


    // add your method here!!

    //第一类：标识符 81
    static int idNum = 81;
    //第二类：常数 80
    static int constNum = 80;
    //第三类：关键字 1~32(32)
    static String[] keywords = {"",
            "auto", "break", "case", "char", "const", "continue",
            "default", "do", "double", "else", "enum", "extern",
            "float", "for", "goto", "if", "int", "long", "register",
            "return", "short", "signed", "sizeof", "static", "struct", " switch"
            , "typedef", "union", "unsigned", "void", "volatile", "while"};

    //第四类：操作符 & 第五类：界符 33~78(46)
    static String[] symbols = {"-", "--", "-=", "->", "!", "!=", "%", "%=", "&", "&&", "&=", "(",
            ")", "*", "*=", ",", ".", "/", "/=", ":", ";", "?", "[", "]",
            "^", "^=", "{", "|", "||", "|=", "}", "~", "+", "++", "+=", "<",
            "<<", "<<=", "<=", "=", "==", ">", ">=", ">>", ">>=", "\""};

    /**
     * 获取token的下标
     *
     * @param token
     * @return
     */
    private static int getKeyIndex(String token) {
        for (int i = 1; i < 33; i++) {
            if (keywords[i].equals(token)) return i;
        }
        return 0;
    }

    private static int getSymbolIndex(String token) {
        for (int i = 0; i < 46; i++) {
            if (symbols[i].equals(token)) return i;
        }
        return -1;
    }

    //计数
    static int cnt = 0;

    private static void print_res(String token, int key) {
        System.out.println(++cnt + ": " + "<" + token + "," + key + ">");
    }

    /**
     * you should add some code in this method to achieve this lab
     * 进行词法分析
     */
    private static void analysis() {
        read_prog();
        //开始词法分析
        lexical_analysis();
    }

    private static void lexical_analysis() {
        String token = "";
        int length = prog.length(); //程序源代码字符数
        //开始遍历
        for (int i = 0; i < length - 1; ) {
            //current 表示目前指针指向的字符
            char current = prog.charAt(i);
            //遇到换行、空格，下一个
            if (current == ' ' || current == '\n' || current == '\t') {
                i++;
                continue;
            }
            //如果是字母，看下一个(C语言的标识符以字母或下划线开头，下划线另外讨论）
            if (isAlphabetic(current)) {
                int j = i + 1;
                //下一个可以是字母/数字/下划线
                while (isDigit(prog.charAt(j)) || isAlphabetic(prog.charAt(j)) || prog.charAt(j) == '_') j++;
                //获取i~j的字符串
                token = prog.substring(i, j);
//                System.out.println(token);
                i = j; //更新i
                //获取对应的下标 如果key>0则为关键字否则为标识符
                int key = getKeyIndex(token);
                if (key > 0) print_res(token, key);
                else print_res(token, idNum);
            }
            //下划线开头的标识符
            else if (current == '_') {
                int j = i + 1;
                //下一个可以是字母/数字/下划线
                while (isDigit(prog.charAt(j)) || isAlphabetic(prog.charAt(j)) || prog.charAt(j) == '_') j++;
                token = prog.substring(i, j);
                i = j; //更新i
                print_res(token, idNum);
            }
            //如果是数字(常量)
            else if (isDigit(current)) {
                int j = i + 1;
                //下一个可以是数字/小数点
                while (isDigit(prog.charAt(j)) || prog.charAt(j) == '.') j++;
                //获取i~j-1的字符串
                token = prog.substring(i, j);
                i = j; //更新i
                print_res(token, constNum);
            }
            //其他情况 界符或运算符
            else {
                int key = 0;
                int next_c = prog.charAt(i + 1);
                //注释的情况
                if (current == '/' && (next_c == '/' || next_c == '*')) {
                    int j = i + 2;
                    key = 79;
                    //如果是单行注释 定位到换行符 i = j
                    if (next_c == '/') {
                        // 单行注释
                        while (true) {
                            if (prog.charAt(j) == '\n' || prog.charAt(j) == '\0' || prog.charAt(j) == '\t')
                                break;
                            j++;
                        }
                    }
                    //如果是多行注释 定位到 '*/' j+=2 i = j
                    else {
                        while (true) {
                            if (prog.charAt(j) == '*' && prog.charAt(j + 1) == '/') break;
                            j++;
                        }
                        j += 2;
                    }
                    token = prog.substring(i, j);
                    i = j;
                    print_res(token, key);
                }
                else if(current == '%' && isAlphabetic(next_c)) {
                    //%d, %f等情况
                    int j = i + 2;
                    while (isAlphabetic(prog.charAt(j)) || isDigit(prog.charAt(j)))
                        j++;
                    token = prog.substring(i, j);
                    i = j;
                    print_res(token, idNum);
                }
                //其他符号 在symbols里进行match 最长为3个字符
                else {
                    String[] cnt = {"","",""}; //用于储存符号匹配情况
                    for (int j = 1; j <= 3; j++) {
                        if(i + j < length){
                            token = prog.substring(i, i + j);
                            int index = getSymbolIndex(token);
                            if (index >= 0) cnt[j - 1] = symbols[index]; //如果获取到下标 说明匹配 在cnt中存储下来
                        }
                        else break; //符号长度出界
                    }
                    int max = 0;
                    //遍历cnt数组，长度最长的那个说明匹配度最高
                    for (int j = 0; j < 3; j++) {
                        //不为空
                        if (cnt[j]!="" && cnt[j].length() > max) {
                            max = cnt[j].length(); //最长的匹配度
                            key = getSymbolIndex(cnt[j]);  //更新下标 这里是根据cnt[j]又get了一次，还有一种方法 用map存储string-index 这样不用重新get一遍
                        }
                    }
                    if (key > 0) {
                        token = prog.substring(i, i + max); //更新token
                        print_res(token, key + 33); //注意+33
                        //更新i
                        i= i+max;
                    }
                    else i++;
                }
            }
        }
        //最后一个符号 一定是'}'
        print_res("}", 63);
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
