package processString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FunctionDefinitionParser {

    public static class ParsedResult {
        private final String functionName;
        private final String sequenceNumber;
        private final ArrayList<String> parameters;
        private final String expression;

        public ParsedResult(String functionName, String sequenceNumber,
                            ArrayList<String> parameters, String expression) {
            this.functionName = functionName;
            this.sequenceNumber = sequenceNumber;
            this.parameters = parameters;
            this.expression = expression;
        }

        public String getFunctionName() { return functionName; }
        public String getSequenceNumber() { return sequenceNumber; }
        public ArrayList<String> getParameters() { return parameters; }
        public String getExpression() { return expression; }
    }

    public static ParsedResult parse(String input) throws IllegalArgumentException {
        String[] parts = input.split("=", 2);
        if (parts.length != 2) throw new IllegalArgumentException("Missing '='");

        String definition = parts[0].trim();
        String expression = parts[1].trim();

        // 修正后的正则表达式（关键修改点）
        Pattern pattern = Pattern.compile(
                "([a-zA-Z])\\s*\\{\\s*(.+?)\\s*}\\s*\\(\\s*(.*?)\\s*\\)"
        );
        Matcher matcher = pattern.matcher(definition);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid function definition format");
        }

        String funcName = matcher.group(1).trim();
        String sequence = matcher.group(2).trim();
        String paramsStr = matcher.group(3).trim();

        ArrayList<String> params = new ArrayList<>();
        if (!paramsStr.isEmpty()) {
            params.addAll(Arrays.asList(paramsStr.split("\\s*,\\s*")));
        }

        return new ParsedResult(funcName, sequence, params, expression);
    }
}