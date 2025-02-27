import expr.Expression;

import java.util.Scanner;
import poly.Poly;

//TIP 要<b>运行</b>代码，请按 <shortcut actionId="Run"/> 或
// 点击装订区域中的 <icon src="AllIcons.Actions.Execute"/> 图标。
public class Main {
    public static void main(String[] args) {
        // 读入带空格字符串？
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();

        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Expression expression = parser.parseExpression();

        Poly poly = expression.toPoly();
        String answer = poly.toString();

        // debug
        // System.out.println("");
        //
        System.out.println(answer);

    }
}