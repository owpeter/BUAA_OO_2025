package expr;

import processString.FunctionDefinitionParser;
import processString.preProcess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class RecursiveFunc {
    private static HashMap<String, HashMap<String, String>> funcMap = new HashMap<>(); // funName -> (n -> f{n})
    private static HashMap<String, ArrayList<String>> paraMap = new HashMap<>(); // funName -> paras[]

    public static void setFunc(Scanner scanner) {
        Integer num = scanner.nextInt();
        scanner.nextLine();
        HashMap<String, String> innerMap = new HashMap<>();
        for (int i = 0; i < num * 3; i++) {
            String preScribe = scanner.nextLine();
            FunctionDefinitionParser.ParsedResult result = FunctionDefinitionParser.parse(preScribe);

            String funcName = result.getFunctionName();
            String sequence = result.getSequenceNumber();
            ArrayList<String> params = result.getParameters();
            String expr = result.getExpression();

            innerMap.put(sequence, expr);
            funcMap.put(funcName, innerMap);
            paraMap.put(funcName, params);
        }
    }

    public static Integer paraLength (String funcName) {
        return paraMap.get(funcName).size();
    }

    public static String callFunc (String funcName, Integer n, ArrayList<Factor> factors) {
        // 代入n
        String funcN = "";
        if (n > 1) {
            funcN = funcMap.get(funcName).get("n");
            funcN = funcN.replaceAll("n-1", String.valueOf(n - 1));
            funcN = funcN.replaceAll("n-2", String.valueOf(n - 2));
        } else {
            funcN = funcMap.get(funcName).get(n.toString());
        }

        // 代入实参
        String result = funcN;
        for (int i=0; i<paraMap.get(funcName).size(); i++) {
            result = funcN.replace(paraMap.get(funcName).get(i), factors.get(i).toPoly().toString());
        }
        return result;
    }
}
