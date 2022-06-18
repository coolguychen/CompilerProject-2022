# CompilerProject-2022
这是2022编译原理实践项目
## Lexical Analysis 词法分析
定义：
词法分析器的功能输入源程序，按照构词规则分解成一系列单词符号。单词是语言中具有独立意义的最小单位，包括关键字、标识符、运算符、界符和常量等
1. 关键字 是由程序语言定义的具有固定意义的标识符。例如，Pascal 中的begin，end，if，while都是保留字。这些字通常不用作一般标识符。
2. 标识符 用来表示各种名字，如变量名，数组名，过程名等等。
3. 常数 常数的类型一般有整型、实型、布尔型、文字型等。
4. 运算符 如+、-、*、/等等。
5. 界符 如逗号、分号、括号、等等。 
本实训需要完成一个简单的C语言词法分析器，在具体关卡中规定了标准输入输出
