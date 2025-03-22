package expr;

import procstring.FunctionDefinitionParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class SelfFunc {
    private static HashMap<String, String> funcMap = new HashMap<>();
    // funName -> expression
    private static HashMap<String, ArrayList<String>> paraMap = new HashMap<>();
    // funName -> parameters

    public static void setFunc(Scanner scanner) {
        Integer num = scanner.nextInt();
        scanner.nextLine();

        for (int i = 0; i < num; i++) {
            String definition = scanner.nextLine();
            FunctionDefinitionParser.ParseSelfFunc result
                = FunctionDefinitionParser.parseSelfFunc(definition);

            String funcName = result.getFunctionName();
            String expr = result.getExpression();
            ArrayList<String> params = result.getParameters();

            funcMap.put(funcName, expr);
            paraMap.put(funcName, params);
        }
    }

    public static Integer paraLength(String funcName) {
        return paraMap.get(funcName).size();
    }

    public static String callFunc(String funcName, ArrayList<Factor> factors) {
        String funcN = funcMap.get(funcName);
        String result = funcN;
        for (int i = 0; i < paraMap.get(funcName).size(); i++) {
            String factorStr = ("(" + factors.get(i).toPoly().toString() + ")").replace("x", "a");
            result = result.replace(paraMap.get(funcName).get(i), factorStr);
        }
        return result.replace("a", "x");
    }
}
