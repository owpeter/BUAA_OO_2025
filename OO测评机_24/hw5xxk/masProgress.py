import multiprocessing.managers
import os
import subprocess
import multiprocessing
import shutil
import time
from tqdm import tqdm
import re
import generator
import datetime
from multiprocessing import Manager

FEED_PROGRAM = 'datainput_student_win64.exe'
CACHE_PATH = "cache"
PROCESS_COUNT = 48
ITERATIONS = 1000
C_NAME = 'checker'
PYTHON = 'python3'


def run_iteration(iteration, tell, JAR_NAME, shared_LAST_TIME:multiprocessing.managers.ValueProxy, shared_MT:multiprocessing.managers.ValueProxy, shared_W:multiprocessing.managers.ValueProxy, shared_cnt: multiprocessing.managers.ValueProxy):
    cache_folder = os.path.join(CACHE_PATH, f"iteration_{iteration}")
    os.makedirs(cache_folder, exist_ok=True)

    # genData
    stdin_path = os.path.join(cache_folder, f"stdin.txt")
    stdins = generator.genData()
    # print(stdins)
    with open(stdin_path, "w", encoding="utf-8") as file:
        file.writelines(stdins)
    shutil.copy(f"./{FEED_PROGRAM}", cache_folder)

    # run your program
    stdout_path = os.path.join(cache_folder, f"stdout.txt")
    with open(stdout_path, "w") as stdout_file:
        datainput_proc = subprocess.Popen([f"./{FEED_PROGRAM}"], cwd=cache_folder, stdout=subprocess.PIPE,
                                          stderr=subprocess.STDOUT)
        java_proc = subprocess.Popen(["java", "-jar", JAR_NAME], stdin=datainput_proc.stdout, stdout=stdout_file)
        java_proc.wait()

    # 运行 checker，传递 stdin.txt 和 stdout.txt 的路径作为命令行参数
    stdouts = []
    with open(stdout_path, "r", encoding="utf-8") as file:
        stdouts = file.readlines()
    new_stdouts = []
    for stdout in stdouts:
        new = re.sub(r"F(\d)", lambda m: f"{int(m.group(1)) + 4}",stdout)
        new = re.sub(r"B(\d)", lambda m: f"{-int(m.group(1)) + 5}", new)
        new_stdouts.append(new)
    with open(stdout_path, "w", encoding="utf-8") as file:
        file.writelines(new_stdouts)
    checker_output = subprocess.run([f"./{C_NAME}", stdin_path, stdout_path], capture_output=True,
                                    text=True).stdout.strip()
    # print(f"Iteration {iteration} completed.")
    datainput_proc.wait()
    shared_cnt.value += 1
    if checker_output != "Correct.":
        with open("run.log", "a", encoding="utf-8") as file:
            file.write(f"Iteration {iteration} completed with checker output: {checker_output}\n")
            tell = file.tell()
    else:
        last_time, mt, w = cal_performance(stdin_path, stdout_path)
        shared_LAST_TIME.value += last_time
        shared_MT.value += mt
        shared_W.value += w
        
        with open("run.log", "r+", encoding="utf-8") as file:
            file.seek(tell)
            file.write(f"Iteration {iteration} finished: AVG_End in {shared_LAST_TIME.value / shared_cnt.value:.3f} s, AVG_MT is {shared_MT.value / shared_cnt.value:.5f} s, AVG_W is {shared_W.value / shared_cnt.value:.3f}     ")
            file.flush()

        for _ in range(5):
            try:
                shutil.rmtree(cache_folder, ignore_errors=True)
                time.sleep(0.2)
            except:
                pass
    return f"Iteration {iteration} completed."


def run():
    global LAST_TIME, MT, W, JAR_NAME
    tell = 0
    with open("run.log", "a", encoding="utf-8") as file:
        tell = file.tell()
    with Manager() as manager:
        shared_LAST_TIME = manager.Value('d', 0)
        shared_MT = manager.Value('d', 0)
        shared_W = manager.Value('d', 0)
        shared_cnt = manager.Value('d', 0)
        shared_JAR_NAME = manager.Value('c_char_p', bytes(JAR_NAME,"utf-8"))
        pool = multiprocessing.Pool(processes=PROCESS_COUNT)

        iterations = range(1, ITERATIONS + 1)

        from functools import partial
        run_iteration_partial = partial(run_iteration, 
                                        tell=tell,
                                        JAR_NAME=JAR_NAME,
                                       shared_LAST_TIME=shared_LAST_TIME,
                                       shared_MT=shared_MT,
                                       shared_W=shared_W,
                                       shared_cnt=shared_cnt)
        with tqdm(total=len(iterations), desc="Iterations") as pbar:
            for result in pool.imap_unordered(run_iteration_partial, iterations):
                pbar.update()
        LAST_TIME = shared_LAST_TIME.value
        MT = shared_MT.value
        W = shared_W.value
        pool.close()
        pool.join()

    with open("run.log", "a", encoding="utf-8") as file:
        file.write("\n")
        file.write(f"AVG_LAST_TIME: {LAST_TIME / ITERATIONS}\n")
        file.write(f"AVG_MT: {MT / ITERATIONS}\n")
        file.write(f"AVG_W: {W / ITERATIONS}\n")
        file.write("-----------END--------------\n")

def cal_performance(stdin, stdout):
    requests = {}
    W_ARRIVE = 0.4
    W_OPEN = 0.1
    W_CLOSE = 0.1

    N_ARRIVE = 0
    N_OPEN = 0
    N_CLOSE = 0
    pattern = re.compile(r"\[(\d+\.\d+)](\d+)-PRI-(\d+)-FROM-([BF]\d+)-TO-([BF]\d+)")
    pri_map = {}
    with open(stdin, 'r') as file:
        for line in file:
            match = pattern.match(line)
            if match:
                time_stamp, request_id, priority, start_floor, dest_floor = match.groups()
                # start_floor = int(start_floor)
                # dest_floor = int(dest_floor)
                priority = int(priority)

                # expectation = ET(start_floor, dest_floor)
                request_id = int(request_id)
                pri_map[request_id] = priority
                time_stamp = float(time_stamp)
                requests[request_id] = time_stamp

    pattern = re.compile(r"\[(\d+\.\d+)]OUT-(\d+)")
    with open(stdout, 'r') as file:
        for line in file:
            if 'ARRIVE' in line:
                N_ARRIVE += 1
            elif 'OPEN' in line:
                N_OPEN += 1
            elif 'CLOSE' in line:
                N_CLOSE += 1
            elif 'OUT' in line:  # TODO 先假设第一次下电梯就是到站
                match = pattern.match(re.sub(r"\s+", "", line))
                time_stamp = float(match.group(1))
                passenger = int(match.group(2))
                requests[passenger] = time_stamp - requests[passenger]
        mt = (sum([requests[i] * pri_map[i] for i in pri_map.keys()]) / sum(pri_map.values()))

    with open(stdout, 'rb') as file:
        pattern = re.compile(r"\[(\d+\.\d+)]")
        file.seek(-2, 2)
        while file.read(1) != b'\n':
            file.seek(-2, 1)
        last_line = file.readline().decode('utf-8')
        match = pattern.match(re.sub(r"\s+", "", last_line))
        last_time = float(match.group(1))

    w = W_ARRIVE * N_ARRIVE + W_OPEN * N_OPEN + W_CLOSE * N_CLOSE

    return last_time, mt, w


if __name__ == "__main__":
    files_and_dirs = os.listdir('.')
    jar_names = [f for f in files_and_dirs if f.endswith('.jar')]

    print(jar_names)
    for name in jar_names:
        global LAST_TIME, MT, W
        LAST_TIME = 0
        MT = 0
        W = 0
        print(f"check {name} start")
        global JAR_NAME
        JAR_NAME = name
        with open("run.log", "a", encoding="utf-8") as file:
            file.write(f"__________{JAR_NAME}_________\n")
            local_time = time.localtime()
            formatted_time = time.strftime("%Y.%m.%d %H:%M:%S", local_time)
            file.write(f"TIME: {formatted_time}\n")
            file.write("----------BEGIN-------------\n")
        run()

