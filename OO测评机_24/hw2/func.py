import sympy as sp
import random

# 定义符号变量
x, y = sp.symbols('x y')

def generate_simple_expression(variable):
    a = random.randint(0, 3)  # 随机整数系数
    b = random.randint(0, 3)  # 随机幂次

    complex_expression = a * (variable ** b)
    return complex_expression

# 复杂函数生成器
def generate_complex_expression(variable):
    """
    生成一个复杂的符号表达式，形式为：
    Σ(a * variable^b * Π(sin(expr_i)^c_i) * Π(cos(expr_i)^d_i))
    """
    # 随机生成系数和幂次
    a = random.randint(0, 3)  # 随机整数系数
    b = random.randint(0, 3)  # 随机幂次

    # 生成随机三角表达式
    expr1 = sp.sin(variable + random.randint(0, 1)) ** random.randint(1, 2)
    expr2 = sp.cos(variable - random.randint(0, 1)) ** random.randint(1, 2)

    # 组合成复杂表达式
    complex_expression = a * (variable ** b) * expr1 * expr2
    # complex_expression = a * (variable ** b)
    return complex_expression

# 定义递推关系中的函数 g, h, g', h', i
g = generate_simple_expression(x)
h = generate_simple_expression(y)
g_prime = generate_simple_expression(x)
h_prime = generate_simple_expression(y)
i = generate_complex_expression(x)

# 定义初始条件 f0(x, y) 和 f1(x, y)
f0 = generate_complex_expression(x) + generate_complex_expression(y)
f1 = generate_complex_expression(x) * generate_complex_expression(y)

# 随机生成系数 a 和 b
a = random.randint(1, 5)
b = random.randint(1, 5)

# 打印递推函数定义
print("递推函数定义：")
print(f"f0 = {f0}")
print(f"f1 = {f1}")
print(f"fn = {a} * f(n-1)({g}, {h}) + {b} * f(n-2)({g_prime}, {h_prime}) + {i}")

# 定义递推函数
def recursive_function(n, x_expr, y_expr):
    """
    实现递推关系：
    f_n(x, y) = a * f_{n-1}(g(x, y), h(x, y)) + b * f_{n-2}(g'(x, y), h'(x, y)) + i(x, y)
    """
    if n == 0:
        return f0.subs({x: x_expr, y: y_expr})
    elif n == 1:
        return f1.subs({x: x_expr, y: y_expr})
    else:
        # 递归调用
        f_n_minus_1 = recursive_function(
            n - 1,
            g.subs({x: x_expr, y: y_expr}),
            h.subs({x: x_expr, y: y_expr}),
        )
        f_n_minus_2 = recursive_function(
            n - 2,
            g_prime.subs({x: x_expr, y: y_expr}),
            h_prime.subs({x: x_expr, y: y_expr}),
        )
        return a * f_n_minus_1 + b * f_n_minus_2 + i.subs({x: x_expr, y: y_expr})

# 嵌套函数调用生成器
def generate_nested_expression(n, max_depth=2):
    """
    生成嵌套递推函数调用作为 x_expr 或 y_expr。
    n: 当前递推函数的层数。
    max_depth: 嵌套的最大深度。
    """
    # 如果达到最大嵌套深度，则返回一个普通复杂表达式
    if max_depth == 0:
        return generate_complex_expression(x)
    
    # 随机选择是否嵌套递推函数调用
    # if random.choice([True, False]):
    #     # 嵌套递推函数调用
    #     nested_n = random.randint(0, max(n - 1, 1))  # 随机选择递推函数层数
    #     return recursive_function(nested_n, generate_nested_expression(nested_n, max_depth - 1), generate_nested_expression(nested_n, max_depth - 1))
    # else:
        # 返回普通复杂表达式
    return generate_complex_expression(x)

# 测试递推函数
n = 2  # 计算 f3(x, y)

# 定义嵌套复杂表达式作为参数
x_expr = generate_nested_expression(n)
y_expr = generate_nested_expression(n)

print("\n测试递推函数：")
print(f"x_expr = {x_expr}")
print(f"y_expr = {y_expr}")

# 计算递推函数
result = recursive_function(n, x_expr, y_expr)
print(result)
print(f"f_{n}({x_expr}, {y_expr}) = {sp.expand(result)}")