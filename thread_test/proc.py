import sys
import argparse
import io # 导入 io 模块，用于类型提示 (可选但推荐)

def classify_elevator_output(input_source: io.TextIOBase):
    """
    从指定的输入源读取电梯日志，并按电梯ID分类和排序。
    输出直接打印到当前的 sys.stdout (该流可能已被重定向到文件)。

    Args:
        input_source: 一个可迭代的文本IO对象，用于逐行读取输入
                      (例如文件对象或 sys.stdin)。
    """
    # 创建一个字典来存储不同电梯的输出
    elevator_outputs = {}
    line_number = 0 # 添加行号用于更清晰的警告

    # 从指定的输入源读取数据
    try:
        for line in input_source:
            line_number += 1
            line = line.strip()  # 去除首尾的空白字符
            if not line:
                continue

            # 提取时间戳和事件描述
            timestamp_end = line.find(']')
            if timestamp_end == -1 or not line.startswith('['):
                # 警告信息打印到 stderr，这样即使用户重定向了 stdout 也能看到
                print(f"警告 (行 {line_number}): 跳过格式错误的行 (缺少时间戳 '[...]') - {line}", file=sys.stderr)
                continue

            timestamp_str = line[1:timestamp_end]
            event_desc = line[timestamp_end+1:].strip()

            # 从事件描述中提取电梯ID
            parts = event_desc.split('-')
            if len(parts) < 1 or not parts[-1]:
                print(f"警告 (行 {line_number}): 跳过格式错误的行 (无法提取电梯ID) - {line}", file=sys.stderr)
                continue

            elevator_id = parts[-1].strip()

            if not elevator_id:
                 print(f"警告 (行 {line_number}): 跳过格式错误的行 (提取的电梯ID为空) - {line}", file=sys.stderr)
                 continue

            if elevator_id not in elevator_outputs:
                elevator_outputs[elevator_id] = []

            elevator_outputs[elevator_id].append(line)

    except Exception as e:
        # 读取输入时的错误也打印到 stderr
        print(f"读取输入时发生错误 (大约在行 {line_number} 附近): {e}", file=sys.stderr)
        # 根据需要决定是否在出错后继续处理已读取的数据
        # return # 如果希望错误时完全不输出，取消注释此行

    # --- 排序 ---
    elevator_ids = list(elevator_outputs.keys())
    try:
        sorted_elevator_ids = sorted(elevator_ids, key=int)
    except ValueError:
        # 提示信息打印到 stderr
        print("提示：部分电梯ID不是纯数字，将按字符串顺序排序。", file=sys.stderr)
        sorted_elevator_ids = sorted(elevator_ids)

    # --- 输出到当前的 sys.stdout (可能已被重定向到文件) ---
    if not sorted_elevator_ids:
        # “未找到数据”的消息也发送到 stdout (即文件)
        print("未找到有效的电梯输出数据。")
        return

    print("--- 电梯输出分类结果 ---") # 这会写入文件
    for elevator_id in sorted_elevator_ids:
        outputs = elevator_outputs[elevator_id]
        print(f"电梯 {elevator_id} 的输出：") # 这会写入文件
        for output in outputs:
            print(output) # 这会写入文件
        print() # 这会写入文件 (空行)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='对电梯日志进行分类和排序输出，并将结果写入 stdout.txt。')
    parser.add_argument('-f', '--file', type=str, help='指定要读取的输入文件名。如果省略，则从标准输入读取。')

    args = parser.parse_args()

    # 定义输出文件名
    output_filename = "stdout.txt"

    # 保存原始的标准输出流
    original_stdout = sys.stdout
    output_file_stream = None

    try:
        # 打开（或创建）输出文件 'stdout.txt'，模式为 'w' (写入，覆盖)
        output_file_stream = open(output_filename, 'w', encoding='utf-8')

        # --- 重定向标准输出 ---
        sys.stdout = output_file_stream

        # --- 处理输入并调用核心函数 ---
        # 现在的 print() 调用（在 classify_elevator_output 内部，
        # 且没有指定 file=sys.stderr）都会写入 output_file_stream
        if args.file:
            try:
                # 使用 'with' 确保输入文件正确关闭
                with open(args.file, 'r', encoding='utf-8') as file_input:
                    classify_elevator_output(file_input)
            except FileNotFoundError:
                # 文件未找到的错误打印到原始控制台 (stderr)
                print(f"错误：输入文件 '{args.file}' 未找到。", file=sys.stderr)
                sys.exit(1) # 退出程序
            except Exception as e:
                # 其他读取错误打印到原始控制台 (stderr)
                print(f"错误：无法读取文件 '{args.file}': {e}", file=sys.stderr)
                sys.exit(1) # 退出程序
        else:
            # 从标准输入读取时的提示信息打印到原始控制台 (stderr)
            print("正在从标准输入读取数据... (按 Ctrl+D 或 Ctrl+Z 后回车 结束输入)", file=sys.stderr)
            classify_elevator_output(sys.stdin)

    except Exception as e:
        # 捕获打开输出文件或其他意外错误
        # 确保错误信息打印到原始控制台 (stderr)
        print(f"处理过程中发生严重错误: {e}", file=sys.stderr)
        sys.exit(1) # 退出程序
    finally:
        # --- 恢复标准输出 ---
        # 无论处理是否成功或发生异常，都确保恢复原始的 stdout
        # 并且关闭我们打开的文件流
        if output_file_stream:
            sys.stdout = original_stdout # 恢复 stdout
            output_file_stream.close()   # 关闭文件
            # 可以在恢复后打印一条完成消息到控制台
            print(f"输出已成功写入到文件: {output_filename}", file=sys.stderr)
        else:
            # 如果连输出文件都没能打开，仍然尝试恢复（尽管可能没变）
             sys.stdout = original_stdout