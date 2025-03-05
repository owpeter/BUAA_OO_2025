# hw_2/gen.py
import random
import re
from sympy import symbols, expand
import sympy

class TestGenerator:
    @staticmethod
    def genData():
        """公开方法，返回generate_test_case的结果"""
        # return TestGenerator.__generate_test_case()
        que_str, exp_str = TestGenerator.__generate_test_case()
        ans_str = TestGenerator._parse_expression_with_sympy(exp_str)
        return que_str, ans_str
    
    @staticmethod
    def __generate_integer(allow_negative=True):
        """生成带符号整数"""
        value = random.randint(0, 20)
        if allow_negative and random.choice([True, False]):
            return f"-{value}"
        elif random.choice([True, False]):
            return f"+{value}"
        else:
            return str(value)

    @staticmethod
    def __generate_exponent():
        """生成非负指数，不超过8"""
        return str(random.randint(0, 8))

    @staticmethod
    def __generate_space():
        """随机生成空白字符"""
        if random.random() < 0.7:  # 70%概率生成空白
            spaces = random.randint(0, 3)
            return ' ' * spaces
        return ''

    @staticmethod
    def __generate_variable_factor(depth=0, max_depth=3, param_count=None, include_function=False):
        """生成变量因子"""
        if depth > max_depth:
            return 'x'
        
        # 根据是否包含函数决定分支选择
        choice_range = 3 if include_function else 2  # 关键修改：当不包含函数时不选择第三个分支
        choice = random.randint(1, choice_range)
        
        if choice == 1:  # 幂函数
            if random.choice([True, False]):
                return f"x{TestGenerator.__generate_space()}^{TestGenerator.__generate_space()}{TestGenerator.__generate_exponent()}"
            else:
                return "x"
        elif choice == 2:  # 三角函数
            trig = random.choice(['sin', 'cos'])
            inner = TestGenerator.__generate_factor(depth + 1, max_depth, param_count, include_function)
            if random.choice([True, False]):
                return f"{trig}{TestGenerator.__generate_space()}({TestGenerator.__generate_space()}{inner}{TestGenerator.__generate_space()}){TestGenerator.__generate_space()}^{TestGenerator.__generate_space()}{TestGenerator.__generate_exponent()}"
            else:
                return f"{trig}{TestGenerator.__generate_space()}({TestGenerator.__generate_space()}{inner}{TestGenerator.__generate_space()})"
        else:  # 只有当include_function=True时才可能进入这个分支
            seq = random.randint(0, 5)
            
            if param_count is not None:
                params = []
                for _ in range(param_count):
                    param = TestGenerator.__generate_factor(depth+1, max_depth, param_count, include_function)
                    params.append(
                        f"{TestGenerator.__generate_space()}{param}{TestGenerator.__generate_space()}"
                    )
                param_str = ",".join(params)
                return f"f{{{seq}}}{TestGenerator.__generate_space()}({param_str})"
            else:
                # 理论上当include_function=True时param_count应该不为None
                # 此处保留容错逻辑
                # factor1 = TestGenerator.__generate_factor(depth+1, max_depth, param_count, include_function)
                # if random.choice([True, False]):
                #     factor2 = TestGenerator.__generate_factor(depth+1, max_depth, param_count, include_function)
                #     return f"f{{{seq}}}{TestGenerator.__generate_space()}({factor1},{factor2})"
                # else:
                #     return f"f{{{seq}}}{TestGenerator.__generate_space()}({factor1})"
                return ""

    @staticmethod
    def __generate_constant_factor():
        """生成常数因子"""
        return TestGenerator.__generate_integer()

    @staticmethod
    def __generate_expression_factor(depth=0, max_depth=3, param_count=None, include_function=False):
        """生成表达式因子"""
        if depth > max_depth:
            return f"({TestGenerator.__generate_space()}x{TestGenerator.__generate_space()})"
        
        expr = TestGenerator.__generate_expression(depth + 1, max_depth, param_count, include_function)
        if random.choice([True, False]):
            return f"({TestGenerator.__generate_space()}{expr}{TestGenerator.__generate_space()}){TestGenerator.__generate_space()}^{TestGenerator.__generate_space()}{TestGenerator.__generate_exponent()}"
        else:
            return f"({TestGenerator.__generate_space()}{expr}{TestGenerator.__generate_space()})"

    @staticmethod
    def __generate_factor(depth=0, max_depth=3, param_count=None, include_function=False):
        """生成因子"""
        if depth > max_depth:
            choice = random.randint(1, 2)
            if choice == 1:
                return TestGenerator.__generate_constant_factor()
            else:
                return TestGenerator.__generate_variable_factor(depth, max_depth, param_count, include_function)
        
        choice = random.randint(1, 3)
        if choice == 1:
            return TestGenerator.__generate_variable_factor(depth, max_depth, param_count, include_function)
        elif choice == 2:
            return TestGenerator.__generate_constant_factor()
        else:
            return TestGenerator.__generate_expression_factor(depth, max_depth, param_count, include_function)

    @staticmethod
    def __generate_term(depth=0, max_depth=3, param_count=None, include_function=False):
        """生成项"""
        if depth > max_depth:
            return f"{TestGenerator.__generate_factor(depth, max_depth, param_count, include_function)}"
        
        factor = TestGenerator.__generate_factor(depth, max_depth, param_count, include_function)
        if random.choice([True, False]):
            sign = random.choice(['+', '-'])
            return f"{sign}{TestGenerator.__generate_space()}{factor}"
        
        # 随机决定是否生成多因子项
        if random.choice([True, False]) and depth < max_depth - 1:
            num_factors = random.randint(1, 3)
            term = factor
            for _ in range(num_factors):
                term += f"{TestGenerator.__generate_space()}*{TestGenerator.__generate_space()}{TestGenerator.__generate_factor(depth + 1, max_depth, param_count, include_function)}"
            return term
        else:
            return factor

    @staticmethod
    def __generate_expression(depth=0, max_depth=3, param_count=None, include_function=False):
        """生成表达式"""
        if depth > max_depth:
            return TestGenerator.__generate_term(depth, max_depth, param_count, include_function)
        
        num_terms = random.randint(1, 3)
        first_term = TestGenerator.__generate_term(depth, max_depth, param_count, include_function)
        if random.choice([True, False]):
            sign = random.choice(['+', '-'])
            expr = f"{sign}{TestGenerator.__generate_space()}{first_term}"
        else:
            expr = first_term
        
        for _ in range(num_terms - 1):
            op = random.choice(['+', '-'])
            expr += f"{TestGenerator.__generate_space()}{op}{TestGenerator.__generate_space()}{TestGenerator.__generate_term(depth + 1, max_depth, param_count, include_function)}"
        
        return expr

    @staticmethod
    def __generate_function_expression(params):
        """生成函数表达式 (不含任何函数调用)"""
        # 简化版的表达式生成，确保不包含函数调用
        terms = []
        for _ in range(random.randint(1, 3)):
            factors = []
            for _ in range(random.randint(1, 2)):
                factor_type = random.randint(1, 3)
                if factor_type == 1:  # 常数
                    factors.append(TestGenerator.__generate_integer())
                elif factor_type == 2:  # 参数
                    param = random.choice(params)
                    if random.choice([True, False]):
                        exp = TestGenerator.__generate_exponent()
                        factors.append(f"{param}^{exp}")
                    else:
                        factors.append(param)
                else:  # 表达式因子
                    inner_expr = random.choice(params)
                    if random.choice([True, False]):
                        factors.append(f"({inner_expr}^{TestGenerator.__generate_exponent()})")
                    else:
                        factors.append(f"({inner_expr})")
            
            term = " * ".join(factors)
            if random.choice([True, False]):
                term = random.choice(['+', '-']) + " " + term
            terms.append(term)
        
        expression = " + ".join(terms).replace("+ -", "- ")
        return expression

    @staticmethod
    def __generate_recursive_expression(params):
        """生成递推表达式 (含f{n-1}和f{n-2}的调用)"""
        const1 = TestGenerator.__generate_integer(False)  # 正整数
        const2 = TestGenerator.__generate_integer(False)  # 正整数
        
        # 为f{n-1}生成参数
        fn1_params = []
        for param in params:
            param_expr = random.choice([param, f"{param}^2", f"sin({param})"])
            fn1_params.append(param_expr)
        
        # 为f{n-2}生成参数
        fn2_params = []
        for param in params:
            param_expr = random.choice([param, f"{param}^2", f"sin({param})"])
            fn2_params.append(param_expr)
        
        fn1_call = f"f{{n-1}}({', '.join(fn1_params)})"
        fn2_call = f"f{{n-2}}({', '.join(fn2_params)})"
        
        # 可能添加额外的常数项
        additional_term = ""
        if random.choice([True, False]):
            additional_term = f" + {TestGenerator.__generate_integer()}"
        
        op = random.choice(['+', '-'])
        expr = f"{const1}*{fn1_call} {op} {const2}*{fn2_call}{additional_term}"
        return expr

    @staticmethod
    def __generate_recursive_function_definition():
        """生成自定义递推函数定义"""
        # 随机选择参数的数量 (1-2)
        param_count = random.randint(1, 2)
        params = ['x'] if param_count == 1 else ['x', 'y']
        
        # 生成定义时强制统一参数数量
        f0_def = f"f{{0}}({', '.join(params)}) = {TestGenerator.__generate_function_expression(params)}"
        f1_def = f"f{{1}}({', '.join(params)}) = {TestGenerator.__generate_function_expression(params)}"
        fn_def = f"f{{n}}({', '.join(params)}) = {TestGenerator.__generate_recursive_expression(params)}"
        
        definitions = [f0_def, f1_def, fn_def]
        random.shuffle(definitions)
        return '\n'.join(definitions), param_count  # 返回参数数量


    @staticmethod
    def __generate_test_case():
        """生成完整测试用例"""
        # 决定是否包含自定义递推函数
        # include_function = random.choice([0, 1])
        include_function=0
        
        output = [str(include_function)]

        param_count = None

        if include_function == 1:
            # 生成函数定义
            function_def, param_count = TestGenerator.__generate_recursive_function_definition()
            output.extend(function_def.split('\n'))
        
        # 生成待展开的表达式
        expression = TestGenerator.__generate_expression(0, 6, param_count, include_function)
        
        # 确保表达式不超过有效长度限制
        while len(expression.replace(' ', '').replace('\t', '')) > 1000:
            expression = TestGenerator.__generate_expression(0, 4, param_count, include_function)
        
        output.append(expression)
        
        return '\n'.join(output), expression
    
    def _parse_expression_with_sympy(expr_str):
        """使用sympy解析表达式并展开"""
        print(expr_str)
        expr_str = expr_str.replace('^', '**')
        
        x = symbols('x')

        math_funcs = {
            "sin": sympy.sin,
            "cos": sympy.cos,
            "x": x,
            "sympy": sympy
        }
        
        try:
            expr = eval(expr_str, {"x": x, "__builtins__": {}}, math_funcs)
            expanded_expr = expand(expr)
            return expanded_expr
        except Exception as e:
            print(f"Sympy解析错误: {e}")
            return None

# 使用示例
if __name__ == "__main__":

#     print(TestGenerator.genData())
# 测试生成100个用例检查参数一致性
    for _ in range(100):
       print(TestGenerator.genData())
        # if case[0] == '1':
        #     params_count = len(case[1].split('(')[1].split(')')[0].split(','))
        #     # 检查所有函数调用
        #     for line in case[4:]:  # 假设表达式在第5行之后
        #         calls = re.findall(r'f{\d+}\s*\(([^)]+)', line)
        #         for call in calls:
        #             args = call.split(',')
        #             assert len(args) == params_count, f"参数数量不符: {line}"