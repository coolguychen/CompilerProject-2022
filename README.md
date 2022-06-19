# CompilerProject-2022
这是2022编译原理实践项目
## Lexical Analysis 词法分析
### 定义
词法分析器的功能输入源程序，按照构词规则分解成一系列单词符号。单词是语言中具有独立意义的最小单位，
包括关键字、标识符、运算符、界符和常量等。

1. 关键字 是由程序语言定义的具有固定意义的标识符。例如，Pascal 中的begin，end，if，while都是保留字。这些字通常不用作一般标识符。
2. 标识符 用来表示各种名字，如变量名，数组名，过程名等等。
3. 常数 常数的类型一般有整型、实型、布尔型、文字型等。
4. 运算符 如+、-、*、/等等。
5. 界符 如逗号、分号、括号、等等。 

本实训需要完成一个简单的C语言词法分析器，在具体关卡中规定了标准输入输出

样例输入放在prog.txt文件中

### 样例1输入  

``` c
int main()  
{  
    printf("HelloWorld");  
    return 0;  
}  
```

输出要满足以下要求  :

- 计数: <符号名,符号标号>  
- 注意，冒号后边有一个空格

### 样例1输出  
```
1: <int,17>  
2: <main,81>  
3: <(,44>  
4: <),45>  
5: <{,59>  
6: <printf,81>  
7: <(,44>  
8: <",78>  
9: <HelloWorld,81>  
10: <",78>  
11: <),45>  
12: <;,53>  
13: <return,20>  
14: <0,80>  
15: <;,53>  
16: <},63>  
```

注意，输出不能有多余的空格，回车等符号。

请注意样例输出最后一行后是没有回车的！输出的符号都是英文的半角符号。

### 错误处理
实训不考虑错误处理，我保证输入的所有代码块是合法的 C 语言代码。


## LL(1)解析器
在创建解析器之前，你应该创建一个下面文法的LL(1)分析表。

### 实验文法定义  
```
program -> compoundstmt  
stmt ->  ifstmt  |  whilestmt  |  assgstmt  |  compoundstmt  
compoundstmt ->  { stmts }  
stmts ->  stmt stmts   |   E  
ifstmt ->  if ( boolexpr ) then stmt else stmt  
whilestmt ->  while ( boolexpr ) stmt  
assgstmt ->  ID = arithexpr ;  
boolexpr  ->  arithexpr boolop arithexpr  
boolop ->   <  |  >  |  <=  |  >=  | ==  
arithexpr  ->  multexpr arithexprprime  
arithexprprime ->  + multexpr arithexprprime  |  - multexpr arithexprprime  |   E  
multexpr ->  simpleexpr  multexprprime  
multexprprime ->  * simpleexpr multexprprime  |  / simpleexpr multexprprime  |   E  
simpleexpr ->  ID  |  NUM  |  ( arithexpr )  
```

起始符:Program

保留字
```
{ }  
if ( ) then else  
while ( )  
ID =   
> < >= <= ==  
+ -  
* /  
ID NUM  
E 是'空'  
```

### 错误处理

本实验需要考虑错误处理，如果程序不正确（包含语法错误），它应该打印语法错误消息（与行号一起），并且程序应该修正错误，并继续解析。
例如：  
```
语法错误,第4行,缺少";"  
```
### 输入

要求：在同一行中每个输入字符用一个空格字符分隔，无其余无关符号。  

样例1输入
```
{  
ID = NUM ;  
}  
```
样例2输入
```
{   
If E1   
then  
s1  
else  
If E2  
Then  
S2  
else   
S3  
}
```

并没有E1，E2等符号，这只是指代表达式  

### 输出
样例1输出
输出要求：在语法树同一层的叶子节点，在以下格式中有相同的缩进，用tab来控制缩减。如样例所示，相同颜色表示在语法树种他们在同一层。
![image](https://user-images.githubusercontent.com/84166461/174464789-d5f427eb-96f3-48c1-8046-6d60c6e212b7.png)

## LR分析器
在动手设计分析器之前，你应该先设计好下面文法的LR(1)分析表。

### 实验文法定义
同LL(1) Parser

### 错误处理
同LL(1) Parser

### 输入
同LL(1) Parser

### 输出
样例一输出

对于正确的程序，输出该程序的最右推导过程

对于有错误的的程序，输出错误问题并改正，继续输出正确的最右推导

每一组串之间均有一个空格符相隔开，分号，括号，=>符号前后均有一个空格符隔开，每一句推导只占一行

```
program =>   
compundstmt =>   
{ stmts } =>   
{ stmt stmts } =>   
{ stmt } =>   
{ assgstmt } =>   
{ ID = arithexpr ; } =>   
{ ID =  multexpr arithexprprime ; } =>   
{ ID = multexpr ; } =>   
{ ID = simpleexpr multexprprime ; } =>   
{ ID = simpleexpr ; } =>   
{ ID = NUM ; }   
```

## Translation Schema
在动手设计之前，你应该先做好Translation Schema的相关准备工作。

你应该在你的程序中进行类型检查，以便对算术表达式(无论是整数算术还是实数算术)执行正确的操作。
### 实验文法定义
```
program -> decls compoundstmt
decls -> decl ; decls | E
decl -> int ID = INTNUM | real ID = REALNUM
stmt -> ifstmt | assgstmt | compoundstmt
compoundstmt -> { stmts }
stmts -> stmt stmts | E
ifstmt -> if ( boolexpr ) then stmt else stmt
assgstmt -> ID = arithexpr ;
boolexpr -> arithexpr boolop arithexpr
boolop -> < | > | <= | >= | ==
arithexpr -> multexpr arithexprprime
arithexprprime -> + multexpr arithexprprime | - multexpr arithexprprime | E
multexpr -> simpleexpr multexprprime
multexprprime -> * simpleexpr multexprprime | / simpleexpr multexprprime | E
simpleexpr -> ID | INTNUM | REALNUM | ( arithexpr )
```
### 错误处理
本实验需要考虑错误处理，如果程序不正确，它应该输出语义错误信息（与行号一起）并退出，不需要进行错误改正。
例如：
```
error message:line 1,realnum can not be translated into int type
```
### 输入
要求：在同一行中每个输入字符之间用一个空格字符分隔，无其余无关符号，输入输出全部为英文状态下字符。

样例输入：
``` c
int a = 1 ; int b = 2 ; real c = 3.0 ;  
{  
a = a + 1 ;  
b = b * a ;  
if ( a < b ) then c = c / 2 ; else c = c / 4 ;  
}  
```
输出
```
a: 2

b: 4

c: 1.5
```
输出变量名及其数值，中间相隔一个空格
