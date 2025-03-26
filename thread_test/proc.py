import sys

def classify_elevator_output(input_file, output_file):
    elevator_outputs = {}
    
    with open(input_file, 'r') as f:
        for line in f:
            line = line.strip()
            if not line:
                continue

            timestamp_end = line.find(']')
            if timestamp_end == -1:
                print(f"警告：跳过格式错误的行 - {line}", file=sys.stderr)
                continue

            timestamp = line[1:timestamp_end].strip()
            event_desc = line[timestamp_end+1:].strip()
            parts = event_desc.split('-')
            
            if len(parts) < 1:
                print(f"警告：跳过格式错误的行 - {line}", file=sys.stderr)
                continue

            elevator_id = parts[-1]
            
            if elevator_id not in elevator_outputs:
                elevator_outputs[elevator_id] = []
                
            elevator_outputs[elevator_id].append(line)

    with open(output_file, 'w') as f:
        for elevator_id, outputs in elevator_outputs.items():
            f.write(f"电梯 {elevator_id} 的输出：\n")
            for output in outputs:
                f.write(f"{output}\n")
            f.write("\n")  # 添加空行分隔

if __name__ == "__main__":
    input_file = "stdin.txt"  # 输入文件名
    output_file = "output.txt"  # 输出文件名
    classify_elevator_output(input_file, output_file)