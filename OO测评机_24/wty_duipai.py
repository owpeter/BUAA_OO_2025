import subprocess
import threading
import os
import glob
import time

def run_jar(jar_path, input_data, results, index):
    try:
        start_time = time.time()  # 记录开始时间
        proc = subprocess.Popen(
            ["java", "-jar", jar_path],
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
        )
        
        # 写入输入并关闭标准输入
        proc.stdin.write(input_data.encode('utf-8'))
        proc.stdin.close()
        
        try:
            # 设置10秒超时
            stdout, stderr = proc.communicate(timeout=10)
            elapsed = time.time() - start_time  # 计算耗时
            output = stdout.decode('utf-8', errors='replace') + stderr.decode('utf-8', errors='replace')
            results[index] = (output, elapsed)  # 保存输出和耗时
        except subprocess.TimeoutExpired:
            proc.kill()  # 终止超时进程
            stdout, stderr = proc.communicate()  # 获取已产生的输出
            output = "TLE (超过10秒未响应)\n" + stdout.decode('utf-8', errors='replace') + stderr.decode('utf-8', errors='replace')
            results[index] = (output, None)  # None表示超时
    except Exception as e:
        results[index] = (f"Error executing {jar_path}: {str(e)}", None)

def get_user_input():
    """获取用户的多行输入，以空行结束"""
    print("请输入多行内容（输入空行结束）：")
    lines = []
    while True:
        try:
            line = input()
            if line.strip() == "":
                break
            lines.append(line)
        except EOFError:
            break
    return '\n'.join(lines)

def get_jar_paths():
    """自动获取当前目录及子目录下所有jar包的路径"""
    # 获取当前工作目录
    current_dir = os.getcwd()
    
    # 使用glob匹配所有jar文件（包含子目录）
    jar_pattern = os.path.join(current_dir, "**", "*.jar")
    jars = glob.glob(jar_pattern, recursive=True)
    
    # 过滤结果，确保只保留文件（排除目录）
    jars = [jar for jar in jars if os.path.isfile(jar)]
    
    if not jars:
        raise FileNotFoundError("当前目录及子目录中未找到任何jar文件")
    
    # print("发现以下jar文件：")
    # for i, jar in enumerate(jars, 1):
    #     print(f"{i}. {jar}")
    
    return jars

def main():
    # 获取用户输入
    input_data = get_user_input()
    
    # 获取jar包路径
    jars = get_jar_paths()
    
    # 初始化结果列表
    results = [None] * len(jars)
    
    # 创建并启动线程
    threads = []
    for i, jar in enumerate(jars):
        thread = threading.Thread(
            target=run_jar,
            args=(jar, input_data, results, i)
        )
        thread.start()
        threads.append(thread)
    
    # 等待所有线程完成
    for thread in threads:
        thread.join()
    
    # 打印结果
    print("\n运行结果：")
    for i, (output, elapsed) in enumerate(results):
        print(f"=== {jars[i]} 的输出 ===")
        if elapsed is not None:
            print(f"[运行时间：{elapsed:.2f}秒]")
        print(output)
        print()

if __name__ == "__main__":
    main()