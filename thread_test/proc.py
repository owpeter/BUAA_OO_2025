import sys

def classify_elevator_output():
    # 创建一个字典来存储不同电梯的输出
    elevator_outputs = {}

    # 从标准输入读取数据
    for line in sys.stdin:
        line = line.strip()  # 去除首尾的空白字符
        if not line:
            continue

        # 提取时间戳和事件描述
        timestamp_end = line.find(']')
        if timestamp_end == -1:
            print(f"警告：跳过格式错误的行 - {line}", file=sys.stderr)
            continue

        timestamp = line[1:timestamp_end].strip()  # 提取时间戳
        event_desc = line[timestamp_end+1:].strip()  # 提取事件描述

        # 从事件描述中提取电梯ID（假设电梯ID是事件描述的最后一部分）
        parts = event_desc.split('-')
        if len(parts) < 1:
            print(f"警告：跳过格式错误的行 - {line}", file=sys.stderr)
            continue

        elevator_id = parts[-1]  # 电梯ID是事件描述的最后一部分

        # 如果电梯ID不存在于字典中，则添加它
        if elevator_id not in elevator_outputs:
            elevator_outputs[elevator_id] = []

        # 将整行数据添加到对应电梯ID的列表中
        elevator_outputs[elevator_id].append(line)

    # 输出每个电梯的分类结果
    for elevator_id, outputs in elevator_outputs.items():
        print(f"电梯 {elevator_id} 的输出：")
        for output in outputs:
            print(output)
        print()  # 添加一个空行以分隔不同电梯的输出

if __name__ == "__main__":
    classify_elevator_output()