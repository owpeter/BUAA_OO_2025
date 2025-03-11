import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FunctionParser {

    public static class FunctionInfo {
        private String name;
        private ArrayList<String> parameters;

        public FunctionInfo(String name, ArrayList<String> parameters) {
            this.name = name;
            this.parameters = parameters;
        }

        public String getName() {
            return name;
        }

        public ArrayList<String> getParameters() {
            return parameters;
        }

        @Override
        public String toString() {
            return "FunctionInfo{" +
                    "name='" + name + '\'' +
                    ", parameters=" + parameters +
                    '}';
        }
    }

    public static FunctionInfo parseFunction(String functionStr) {
        // 定义正则表达式匹配函数名和参数列表
        String regex = "([a-zA-Z]+)\\((.*)\\)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(functionStr);

        if (matcher.matches()) {
            String name = matcher.group(1); // 获取函数名
            String paramsStr = matcher.group(2); // 获取参数列表字符串

            ArrayList<String> parameters = new ArrayList<>();
            if (!paramsStr.trim().isEmpty()) {
                // 按逗号分隔参数
                String[] params = paramsStr.split(",");
                for (String param : params) {
                    parameters.add(param.trim()); // 去除参数两边的空格
                }
            }

            return Function newInfo(name, parameters);
        } else {
            throw new IllegalArgumentException("Invalid function format: " + functionStr);
        }
    }

    public static void main(String[] args) {
        String functionStr = "h(x,y)";
        try {
            FunctionInfo functionInfo = parseFunction(functionStr);
            System.out.println("Function name: " + functionInfo.getName());
            System.out.println("Parameters: " + functionInfo.getParameters());
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
    }
}