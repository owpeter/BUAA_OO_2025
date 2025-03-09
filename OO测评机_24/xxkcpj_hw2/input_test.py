import subprocess

def execute_java_with_multiple_inputs(inputs, fname):
    # 构造命令
    cmd = ['java', '-jar', fname]
    
    # 启动子进程
    proc = subprocess.Popen(
        cmd,
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        bufsize=0  # 设置缓冲区为 0，确保实时交互
    )
    
    try:
        # 逐步传递输入
        for input_str in ['0','0']:
            # 将输入字符串转换为字节流，并添加换行符
            input_bytes = (input_str).encode('utf-8')
            proc.stdin.write(input_bytes)
            proc.stdin.flush()  # 确保数据立即发送
            
            # 读取 Java 程序的输出（如果需要）
            while True:
                output_line = proc.stdout.readline().decode('utf-8').strip()
                if output_line:
                    print("Java 程序输出:", output_line)
                else:
                    break
        
        # 关闭输入流，等待 Java 程序结束
        proc.stdin.close()
        proc.wait()
        
        # 获取最终的错误输出（如果有）
        stderr = proc.stderr.read().decode('utf-8').strip()
        if stderr:
            print("Java 程序错误:", stderr)
    
    except Exception as e:
        print("发生错误:", e)
    finally:
        # 确保子进程被正确关闭
        proc.terminate()

# 示例调用
inputs = [
    '1',
    'f0 = 3*y*sin(y + 1)*cos(y)**2',
    'f1 = 0',
    'fn = 1 * f(n-1)(x**3, 2*y**2) + 5 * f(n-2)(3*x, 3*y**3) + 2*x**2*sin(x)*cos(x - 1)**2',
    'f{2}(x**2*sin(x + 1)*cos(x - 1), 0) + 1'
]

execute_java_with_multiple_inputs(inputs, 'xxkcpj_hw2\hw1.jar')