import expr.Expression;
import expr.Factor;
import sun.lwawt.macosx.CSystemTray;

import java.util.Scanner;

//TIP 要<b>运行</b>代码，请按 <shortcut actionId="Run"/> 或
// 点击装订区域中的 <icon src="AllIcons.Actions.Execute"/> 图标。
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.next();

        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Expression expression = parser.parseExpression();
        String expr = expression.toString();

    }
}