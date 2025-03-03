import os
import subprocess
import sympy
from sympy import symbols, expand, simplify, Poly
import re
import sys
import concurrent.futures
import time
from gendata import genData

def parse_expression_with_sympy(expr_str):
    """使用sympy解析表达式并展开"""
    # 将^替换为**以适应sympy语法
    expr_str = expr_str.replace('^', '**')
    
    # 定义符号x
    x = symbols('x')
    
    # 解析并展开表达式
    try:
        expr = eval(expr_str, {"x": x, "__builtins__": {}}, {"sympy": sympy})
        expanded_expr = expand(expr)
        return expanded_expr
    except Exception as e:
        print(f"Sympy解析错误: {e}")
        return None

def run_jar_file(jar_path, input_expr):
    """运行JAR文件并获取输出"""
    try:
        # 记录开始时间
        start_time = time.time()
        
        # 直接使用subprocess.Popen以便能够写入标准输入
        process = subprocess.Popen(['java', '-jar', jar_path], 
                                  stdin=subprocess.PIPE,
                                  stdout=subprocess.PIPE,
                                  stderr=subprocess.PIPE,
                                  text=True)
        
        # 写入输入并获取输出
        stdout, stderr = process.communicate(input=input_expr, timeout=10)
        
        # 计算执行时间
        execution_time = time.time() - start_time
        
        if process.returncode == 0:
            return jar_path, stdout.strip(), execution_time, None
        else:
            return jar_path, None, execution_time, f"JAR运行错误: {stderr}"
    except subprocess.TimeoutExpired:
        process.kill()
        return jar_path, None, 10, "JAR运行超时"
    except Exception as e:
        return jar_path, None, 0, f"运行JAR出错: {e}"

def compare_expressions(sympy_expr, jar_output):
    """比较sympy表达式和jar输出的表达式是否等价"""
    if sympy_expr is None or jar_output is None:
        return False
    
    # 将jar输出转换为sympy可以解析的格式
    jar_output = jar_output.replace('^', '**')
    
    try:
        # 解析jar输出
        x = symbols('x')
        jar_expr = eval(jar_output, {"x": x, "__builtins__": {}}, {"sympy": sympy})
        jar_expr = expand(jar_expr)
        
        # 检查两个表达式是否等价
        diff = expand(sympy_expr - jar_expr)
        
        # 如果差为0，则表达式等价
        if diff == 0:
            return True
        
        # 转换为多项式并比较
        p1 = Poly(sympy_expr, x)
        p2 = Poly(jar_expr, x)
        
        return p1 == p2
    except Exception as e:
        print(f"比较表达式时出错: {e}")
        return False

def process_jar(jar_file, input_expr, sympy_expr):
    """处理单个JAR文件的完整流程"""
    #print(f"开始处理 {jar_file}...")
    jar_path, jar_result, execution_time, error = run_jar_file(jar_file, input_expr)
    
    result = {
        "jar_file": jar_file,
        "execution_time": execution_time,
        "success": False,
        "output": None,
        "matches_sympy": False,
        "error": error
    }
    
    if jar_result:
        result["success"] = True
        result["output"] = jar_result
        result["matches_sympy"] = compare_expressions(sympy_expr, jar_result)
    
    return result

def clear_screen():
    # 根据操作系统选择命令
    if os.name == 'nt':  # Windows
        os.system('cls')
    else:  # macOS, Linux, Unix
        os.system('clear')

def main():
    # 获取用户输入的表达式
    #print("请输入一个多项式表达式 (例如: (x^8+1)*(x^7-2)):")
    #input_expr = input().strip()
    #print(input_expr)
    
    # 使用sympy解析表达式
    #print("正在使用Sympy解析表达式...")
    # sympy_expr = parse_expression_with_sympy(input_expr)
    
    # 查找同目录下的所有jar文件
    jar_files = [f for f in os.listdir('.') if f.endswith('.jar')]
    if not jar_files:
        print("当前目录下没有找到JAR文件")
        return
    
    print(f"找到 {len(jar_files)} 个JAR文件，按 Enter 开始并发处理...")
    input()
    cnt = 0
    while(True):
        cnt += 1
        clear_screen()
        print(cnt)
        cost = 10001
        while(cost > 10000):
            input_expr, _, cost = genData()
        input_expr = input_expr.replace("**", "^")
        sympy_expr = parse_expression_with_sympy(re.sub(r'\b0+(\d+)\b', r'\1', input_expr))
        # print(input_expr, sympy_expr)
        # 使用线程池并发运行所有JAR文件
        results = []
        with concurrent.futures.ThreadPoolExecutor() as executor:
            # 提交所有任务
            future_to_jar = {
                executor.submit(process_jar, jar_file, input_expr, sympy_expr): jar_file 
                for jar_file in jar_files
            }
            
            # 收集结果
            for future in concurrent.futures.as_completed(future_to_jar):
                jar_file = future_to_jar[future]
                try:
                    results.append(future.result())
                except Exception as e:
                    print(f"处理 {jar_file} 时出现异常: {e}")
                    results.append({
                        "jar_file": jar_file,
                        "execution_time": 0,
                        "success": False,
                        "output": None,
                        "matches_sympy": False,
                        "error": f"处理异常: {e}"
                    })
        
        # 按执行时间排序结果
        results.sort(key=lambda x: x["execution_time"])
        
        # 输出结果摘要
        # print("执行结果摘要:")
        
        # 输出详细结果
        #print("\n详细结果:")
        flag = True
        for result in results:
            #print(f"\n{result['jar_file']}:")
            #print(f"  执行时间: {result['execution_time']:.3f}秒")
            
            if result["success"]:
                if result["matches_sympy"]:
                    pass
                    #print("  ✓ 结果与Sympy一致")
                else:
                    print(f"\n{result['jar_file']}:")
                    print(f"  jar 输出: {result['output']}")
                    print(f"sympy 输出: {sympy_expr}")
                    print("  ✗ 结果与Sympy不一致")
                    flag = False
            else:
                print(f"\n{result['jar_file']}:")
                print(f"  ✗ 执行失败: {result['error']}")
                flag = False
        
        if (flag == False) :
            print(f"{'JAR':<30} | {'Time(s)':<10} | {'Run':<5} | {'Correct':<10}")
            print("-" * 60)
            
            for result in results:
                jar_name = result["jar_file"]
                time_str = f"{result['execution_time']:.3f}"
                success = "✓" if result["success"] else "✗"
                matches = "✓" if result["matches_sympy"] else "✗"
                
                print(f"{jar_name:<30} | {time_str:<10} | {success:<5} | {matches:<10}")

            break

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n程序被用户中断")
    except Exception as e:
        print(f"程序出错: {e}")
