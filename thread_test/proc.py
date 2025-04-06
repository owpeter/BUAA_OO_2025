import sys
import argparse
import io # For type hinting
from collections import defaultdict
import re

# --- Updated Regular Expression for Timestamp ---
# Matches:
#   ^\[       Start with '['
#   \s*       Optional leading whitespace
#   (         Start capturing group 1 (the full number)
#     \d+     One or more digits (integer part)
#     (\.\d+)? Optional: a literal dot followed by one or more digits (fractional part) - captured in group 2
#   )         End capturing group 1
#   \s*       Optional trailing whitespace
#   \]        Ending ']'
#   (.*)      Capture the rest of the line as the event part (group 3)
TIMESTAMP_PATTERN = re.compile(r'^\[\s*(\d+(\.\d+)?)\s*\](.*)')

# --- Person ID Classification Function ---
def classify_by_person(input_source: io.TextIOBase):
    """
    从指定的输入源读取电梯日志，按 personID 分类和排序（按浮点数时间戳）。
    结果直接打印到当前的 sys.stdout (可能已被重定向到文件)。

    Args:
        input_source: 一个可迭代的文本IO对象，用于逐行读取输入。
    """
    person_data = defaultdict(list)
    line_number = 0

    print("--- Person ID 输出分类结果 ---") # Header for the output file

    try:
        for line in input_source:
            line_number += 1
            line = line.strip()
            if not line:
                continue

            match = TIMESTAMP_PATTERN.match(line) # Use the updated pattern
            if not match:
                print(f"警告 (行 {line_number}): 跳过格式错误的行 (无法匹配时间戳格式 '[ time ]') - {line}", file=sys.stderr)
                continue

            # Group 1 contains the full number string (e.g., "11.5720", "10")
            # Group 3 contains the event part after the timestamp
            time_str = match.group(1)
            event_part = match.group(3).strip() # Get event part and strip spaces

            try:
                # Convert timestamp string to float for accurate sorting
                time_float = float(time_str)
            except ValueError:
                print(f"警告 (行 {line_number}): 跳过格式错误的行 (无效的时间数值 '{time_str}') - {line}", file=sys.stderr)
                continue

            parts = event_part.split('-')
            person_id = None

            # Determine personID based on event type
            try:
                event_type = parts[0]
                if event_type == 'RECEIVE' and len(parts) >= 3:
                    person_id = parts[1]
                elif event_type == 'IN' and len(parts) >= 4:
                    person_id = parts[1]
                elif event_type == 'OUT' and len(parts) >= 5 and parts[1] in ('S', 'F'):
                    person_id = parts[2]
                # Add more rules here if needed for other person-related events
            except IndexError:
                 pass # Silently ignore lines without personID in this mode

            # Store if person_id was found
            if person_id is not None and person_id.strip(): # Check if ID is not empty
                 person_id = person_id.strip()
                 # Store the float time and original line
                 person_data[person_id].append((time_float, line))
            # else:
                 # Pass silently for lines without personID
                 pass

    except Exception as e:
        print(f"读取输入时发生错误 (大约在行 {line_number} 附近): {e}", file=sys.stderr)

    # --- Sorting and Outputting Person Data ---
    if not person_data:
        print("未找到包含 Person ID 的有效数据。") # Writes to output file
        return

    # Sort person IDs (numerically if possible)
    sorted_person_ids = []
    try:
        sorted_person_ids = sorted(person_data.keys(), key=int)
    except ValueError:
        print("提示：部分 Person ID 不是纯数字，将按字符串顺序排序。", file=sys.stderr)
        sorted_person_ids = sorted(person_data.keys())

    # Print sorted data
    first_person = True
    for pid in sorted_person_ids:
        if not first_person:
             print() # Add a blank line between persons in the output file
        first_person = False

        print(f"接下来要输出的内容为personID为{pid}的内容") # Writes to output file
        records = person_data[pid]
        # Sort by float timestamp (first element of tuple)
        records.sort()
        for _, original_line in records:
            print(original_line) # Writes to output file

# --- Elevator ID Classification Function ---
def classify_by_elevator(input_source: io.TextIOBase):
    """
    从指定的输入源读取电梯日志，并按电梯ID分类和排序。
    输出直接打印到当前的 sys.stdout (该流可能已被重定向到文件)。
    注意：此模式内部不对每个电梯的行进行时间排序。

    Args:
        input_source: 一个可迭代的文本IO对象，用于逐行读取输入。
    """
    elevator_outputs = defaultdict(list)
    line_number = 0

    print("--- 电梯输出分类结果 ---") # Header for the output file

    try:
        for line in input_source:
            line_number += 1
            line = line.strip()
            if not line:
                continue

            match = TIMESTAMP_PATTERN.match(line) # Use the updated pattern
            if not match:
                print(f"警告 (行 {line_number}): 跳过格式错误的行 (无法匹配时间戳格式 '[ time ]') - {line}", file=sys.stderr)
                continue

            # Group 3 contains the event part after the timestamp
            event_part = match.group(3).strip() # Get event part

            # Extract elevator ID (assuming it's the last part)
            parts = event_part.split('-')
            if not parts:
                print(f"警告 (行 {line_number}): 跳过格式错误的行 (事件描述为空) - {line}", file=sys.stderr)
                continue

            elevator_id = parts[-1].strip()

            if not elevator_id:
                 print(f"警告 (行 {line_number}): 跳过格式错误的行 (提取的电梯ID为空) - {line}", file=sys.stderr)
                 continue

            # Store the original line associated with this elevator ID
            elevator_outputs[elevator_id].append(line)

    except Exception as e:
        print(f"读取输入时发生错误 (大约在行 {line_number} 附近): {e}", file=sys.stderr)

    # --- Sorting and Outputting Elevator Data ---
    if not elevator_outputs:
        print("未找到有效的电梯输出数据。") # Writes to output file
        return

    # Sort elevator IDs (numerically if possible)
    elevator_ids = list(elevator_outputs.keys())
    try:
        sorted_elevator_ids = sorted(elevator_ids, key=int)
    except ValueError:
        print("提示：部分电梯ID不是纯数字，将按字符串顺序排序。", file=sys.stderr)
        sorted_elevator_ids = sorted(elevator_ids)

    # Print sorted data
    first_elevator = True
    for elevator_id in sorted_elevator_ids:
        if not first_elevator:
            print()
        first_elevator = False

        outputs = elevator_outputs[elevator_id]
        # Reminder: Lines within each elevator are NOT sorted by time in this mode.
        print(f"电梯 {elevator_id} 的输出：")
        for output_line in outputs:
            print(output_line)


# --- Main Execution Block ---
if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='根据选择的模式（电梯ID或Person ID）对电梯日志进行分类和排序，并将结果写入 stdout.txt。')
    parser.add_argument('-f', '--file', type=str, help='指定要读取的输入文件名。如果省略，则从标准输入读取。')
    parser.add_argument('--mode', choices=['elevator', 'person'], required=True,
                        help='选择分类模式: "elevator" (按电梯ID) 或 "person" (按Person ID)。')

    args = parser.parse_args()

    output_filename = "stdout.txt"
    original_stdout = sys.stdout
    output_file_stream = None
    input_file_stream = None

    try:
        output_file_stream = open(output_filename, 'w', encoding='utf-8')
        sys.stdout = output_file_stream # Redirect standard output

        actual_input_source = None
        if args.file:
            try:
                input_file_stream = open(args.file, 'r', encoding='utf-8')
                actual_input_source = input_file_stream
            except FileNotFoundError:
                print(f"错误：输入文件 '{args.file}' 未找到。", file=sys.stderr) # Error to stderr
                sys.exit(1)
            except Exception as e:
                print(f"错误：无法读取文件 '{args.file}': {e}", file=sys.stderr) # Error to stderr
                sys.exit(1)
        else:
            print("正在从标准输入读取数据... (按 Ctrl+D 或 Ctrl+Z 后回车 结束输入)", file=sys.stderr) # Prompt to stderr
            actual_input_source = sys.stdin

        if actual_input_source:
            if args.mode == 'elevator':
                classify_by_elevator(actual_input_source)
            elif args.mode == 'person':
                classify_by_person(actual_input_source)
            else:
                print(f"错误：无效的模式 '{args.mode}'。", file=sys.stderr) # Error to stderr
                sys.exit(1)

    except Exception as e:
        print(f"处理过程中发生严重错误: {e}", file=sys.stderr) # Error to stderr
        # Try to restore stdout even if error happens before the main finally block
        if sys.stdout is not original_stdout:
            sys.stdout = original_stdout
        sys.exit(1)
    finally:
        # --- Restore standard output and close files ---
        if sys.stdout is not original_stdout:
             sys.stdout = original_stdout # Restore stdout

        if output_file_stream:
            try:
                output_file_stream.close()
                # Confirmation message goes to stderr (console)
                print(f"输出已成功写入到文件: {output_filename}", file=sys.stderr)
            except Exception as e:
                print(f"关闭输出文件 '{output_filename}' 时出错: {e}", file=sys.stderr)


        if input_file_stream: # Close the input file if we opened it
             try:
                 input_file_stream.close()
             except Exception as e:
                 print(f"关闭输入文件时出错: {e}", file=sys.stderr)