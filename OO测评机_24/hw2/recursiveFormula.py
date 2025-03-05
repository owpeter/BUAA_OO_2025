import re
from sympy import symbols, sympify, Function, simplify, expand, Symbol, Wild

x, y, n = symbols('x y n')

class RecursiveFunction(Function):
    @classmethod
    def eval(cls, k, x_val, y_val):
        if k.is_Number and k >= 0:
            return None  # 保持符号形式

class RecursiveSystem:
    def __init__(self):
        self.base = {0: None, 1: None}
        self.recursive = None
        self.memo = {}
    
    def add_base(self, k, expr):
        self.base[k] = expr
    
    def add_recursive(self, expr):
        self.recursive = expr
    
    def _parse_call(self, expr_str):
        # 将f{n-1}(...)转换为RecursiveFunction(n-1, x, y)
        return re.sub(r'f\{([^\}]+)\}\s*\(([^)]+)\)', 
                     lambda m: f'RecursiveFunction({m.group(1)}, {m.group(2)})', 
                     expr_str)
    
    def _eval_step(self, k, x_val, y_val, depth=0):
        indent = "  " * depth
        steps = []
        
        if (k, x_val, y_val) in self.memo:
            return self.memo[(k, x_val, y_val)], steps
        
        if k in [0, 1]:
            expr = self.base[k]
            result = expr.subs({x: x_val, y: y_val})
            steps.append(f"{indent}f{{{k}}}({x_val}, {y_val}) = {expr} = {result}")
            self.memo[(k, x_val, y_val)] = result
            return result, steps
        
        # 替换递推模板中的n为当前k值
        expr = self.recursive.subs(n, k)
        expr = expr.subs({x: x_val, y: y_val})
        
        steps.append(f"{indent}展开 f{{{k}}}({x_val}, {y_val}):")
        steps.append(f"{indent}递推表达式: {expr}")
        
        # 查找所有递归调用
        calls = expr.find(RecursiveFunction)
        replacements = {}
        for call in calls:
            called_k = simplify(call.args[0])
            arg_x = simplify(call.args[1].subs({x: x_val, y: y_val}))
            arg_y = simplify(call.args[2].subs({x: x_val, y: y_val}))
            
            sub_result, sub_steps = self._eval_step(called_k, arg_x, arg_y, depth+1)
            steps.extend(sub_steps)
            steps.append(f"{indent}代入 f{{{called_k}}}({arg_x}, {arg_y}) → {sub_result}")
            replacements[call] = sub_result
        
        final_expr = expr.subs(replacements)
        result = simplify(final_expr)
        steps.append(f"{indent}简化结果: {result}")
        
        self.memo[(k, x_val, y_val)] = result
        return result, steps

def parse_definition(line):
    line = re.sub(r'\s+', '', line)
    
    # 解析基例
    base_match = re.match(r'f\{(0|1)}\(x,y\)=(.+)', line)
    if base_match:
        k = int(base_match.group(1))
        expr_str = base_match.group(2)
        return 'base', k, sympify(expr_str)
    
    # 解析递推模板
    rec_match = re.match(r'f\{n}\(x,y\)=(.+)', line)
    if rec_match:
        expr_str = rec_match.group(1)
        # 预处理函数调用
        processed = re.sub(r'f\{n-(\d+)\}', r'RecursiveFunction(n-\1, x, y)', expr_str)
        return 'recursive', None, sympify(processed, {'RecursiveFunction': RecursiveFunction})
    
    raise ValueError(f"无效定义: {line}")

# 主程序
system = RecursiveSystem()

n_groups = int(input())
for _ in range(n_groups):
    group = [input().strip() for _ in range(3)]
    
    base_count = 0
    for line in group:
        def_type, k, expr = parse_definition(line)
        if def_type == 'base':
            system.add_base(k, expr)
            base_count += 1
        else:
            system.add_recursive(expr)
    
    if base_count != 2:
        raise ValueError("每组必须包含两个基例定义")

# 解析函数调用
call_line = input().strip()
call_match = re.match(r'f\{(\d+)}\s*\(\s*([^,]+)\s*,\s*([^)]+)\s*\)', call_line)
if not call_match:
    raise ValueError("无效函数调用格式")

target_k = int(call_match.group(1))
arg_x = sympify(call_match.group(2))
arg_y = sympify(call_match.group(3))

result, steps = system._eval_step(target_k, arg_x, arg_y)
print("\n".join(steps))
print(f"最终结果: {result}")