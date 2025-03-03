import os
import subprocess
from time import time

def get_jar_files(path):
    """获取当前目录下所有JAR文件"""
    return [f for f in os.listdir(path)
           if f.endswith('.jar')]

def run_jar(jar_path, input_data, timeout=10):
    """运行单个JAR文件并返回结果"""
    result = {
        'jar': jar_path,
        'output': '',
        'error': '',
        'returncode': None,
        'timeout': False,
        'duration': 0
    }
    
    try:
        start_time = time()
        proc = subprocess.Popen(
            ['java', '-jar', jar_path],
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True
        )
        
        try:
            # 发送输入并等待完成
            stdout, stderr = proc.communicate(
                input=input_data,
                timeout=timeout
            )
            result['output'] = stdout.strip()
            result['error'] = stderr.strip()
            
        except subprocess.TimeoutExpired:
            proc.kill()
            stdout, stderr = proc.communicate()
            result['timeout'] = True
            result['error'] = f"Timeout after {timeout}s | {stderr.strip()}"
            
        result['returncode'] = proc.returncode
        result['duration'] = round(time() - start_time, 2)
        
    except Exception as e:
        result['error'] = str(e)
    
    return result

def print_results(results):
    """格式化打印结果"""
    for res in results:
        print(f"\n🔨 JAR: {res['jar']}")
        # print(f"⏱️  Duration: {res['duration']}s")
        # print(f"🟢 Return Code: {res['returncode']}")
        
        if res['timeout']:
            print("⛔ Execution timed out")
        
        if res['error']:
            print(f"❌ Error:\n{res['error']}")
        
        if res['output']:
            print(f"📄 Output:\n{res['output']}")
        
        print("━" * 50)

def main():
    # 配置参数
    input_data = input()
    # 示例输入内容
    timeout = 15  # 秒
    
    # 获取并运行所有JAR
    path = '.'
    jars = get_jar_files(path)
    if not jars:
        print("❌ No JAR files found in current directory")
        return
    
    print(f"🔍 Found {len(jars)} JAR files:")
    print("\n".join(f"• {jar}" for jar in jars))
    
    # 运行所有JAR
    results = []
    for jar in jars:
        print(f"\n🚀 Running {jar}...")
        results.append(run_jar(jar, input_data, timeout))
    
    # 显示结果
    print("\n" + "="*50)
    print("🏁 Final Results:")
    print_results(results)

if __name__ == "__main__":
    main()
