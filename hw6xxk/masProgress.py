import json
import multiprocessing.managers
import os
import subprocess
import multiprocessing
import shutil
import time
import random
from tqdm import tqdm
import mygenerator
import ast
from multiprocessing import Manager

DEBUG = False
FROM_FILE = True
INPUT_FILE = "stdin.txt"
FEED_PROGRAM = 'datainput_student_win64.exe'
CACHE_PATH = "cache"
PROCESS_COUNT = 48
ITERATIONS = 10000
CHECKER = 'checker.py'
PYTHON = 'python'


def run_iteration(iteration, tell, args, shared_JAR_NAME:multiprocessing.managers.ValueProxy, shared_LAST_TIME:multiprocessing.managers.ValueProxy, shared_MT:multiprocessing.managers.ValueProxy, shared_W:multiprocessing.managers.ValueProxy, shared_cnt: multiprocessing.managers.ValueProxy):
    JAR_NAME = shared_JAR_NAME.value
    cache_folder = os.path.join(CACHE_PATH, f"iteration_{iteration}")
    os.makedirs(cache_folder, exist_ok=True)

    # genData
    stdin_path = os.path.join(cache_folder, f"stdin.txt")
    if not FROM_FILE:
        stdins = mygenerator.genData()
    else:
        stdins = open(INPUT_FILE, "r", encoding="utf-8").read()
    # print(stdins)
    with open(stdin_path, "w", encoding="utf-8") as file:
        file.writelines(stdins)
    shutil.copy(f"./{FEED_PROGRAM}", cache_folder)

    # run your program
    stdout_path = os.path.join(cache_folder, f"stdout.txt")
    with open(stdout_path, "w") as stdout_file:
        datainput_proc = subprocess.Popen([f"./{FEED_PROGRAM}"], cwd=cache_folder, stdout=subprocess.PIPE,
                                          stderr=subprocess.STDOUT)
        if args:
            java_proc = subprocess.Popen(["java", "-jar", JAR_NAME],args=args, stdin=datainput_proc.stdout, stdout=stdout_file)
        else:
            java_proc = subprocess.Popen(["java", "-jar", JAR_NAME],stdin=datainput_proc.stdout, stdout=stdout_file)
        java_proc.wait()
    if DEBUG:
        shutil.copy(stdout_path, os.path.join(cache_folder, "stdout.out.back"))
        stdouts = open(stdout_path, "r", encoding="utf-8").readlines()
        stdouts = [stdout for stdout in stdouts if stdout[0] == '[']
        open(stdout_path, "w", encoding="utf-8").writelines(stdouts)
    # 运行 checker，传递 stdin.txt 和 stdout.txt 的路径作为命令行参数
    checker_output = subprocess.run([PYTHON, CHECKER, stdin_path, stdout_path], capture_output=True).stdout.strip()
    try:
        # 如果是 bytes，先解码
        if isinstance(checker_output, bytes):
            checker_output = checker_output.decode('utf-8')
        # 使用 ast.literal_eval 解析（兼容单引号）
        checker_output = ast.literal_eval(checker_output)
        
        # 或者用 json.loads（需要确保是标准 JSON）
        # checker_data = json.loads(checker_output.replace("'", '"'))
    except (ValueError, SyntaxError) as e:
        print(f"解析失败！原始数据: {checker_output}")
        raise
    datainput_proc.wait()
    shared_cnt.value += 1
    if checker_output["result"] == "Fail":
        os.makedirs("err", exist_ok=True)
        local_time = time.localtime()
        formatted_time = time.strftime("%Y%m%d_%H%M%S", local_time)
        shutil.copy(os.path.join(cache_folder, "stdin.txt"), os.path.join("err", f"{iteration}_{formatted_time}"))
        with open("run.log", "a", encoding="utf-8") as file:
            file.write(f"Iteration {iteration} FAILED !!!\n")
            file.writelines(checker_output["errors"])
            file.write("\n\n")
            tell = file.tell()
    else:
        last_time = checker_output["performance"]["T_final"]
        mt = checker_output["performance"]["WT_weighted_time"]
        w = checker_output["performance"]["W_energy"]
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


def run(args):
    global LAST_TIME, MT, W, JAR_NAME
    tell = 0
    with open("run.log", "a", encoding="utf-8") as file:
        tell = file.tell()
    with Manager() as manager:
        shared_LAST_TIME = manager.Value('d', 0)
        shared_MT = manager.Value('d', 0)
        shared_W = manager.Value('d', 0)
        shared_cnt = manager.Value('d', 0)
        shared_JAR_NAME = manager.Value('c_char_p', bytes(JAR_NAME, encoding="utf-8"))
        pool = multiprocessing.Pool(processes=PROCESS_COUNT)

        iterations = range(1, ITERATIONS + 1)

        from functools import partial
        run_iteration_partial = partial(run_iteration, 
                                        tell=tell,
                                        args=args,
                                        shared_JAR_NAME=shared_JAR_NAME,
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

if __name__ == "__main__":
    files_and_dirs = os.listdir('.')
    jar_names = [f for f in files_and_dirs if f.endswith('.jar')]
    args = None
    print(jar_names)
    for name in jar_names:
        global LAST_TIME, MT, W, JAR_NAME
        LAST_TIME = 0
        MT = 0
        W = 0
        print(f"check {name} start")
        JAR_NAME = name
        with open("run.log", "a", encoding="utf-8") as file:
            file.write(f"__________{JAR_NAME}_________\n")
            local_time = time.localtime()
            formatted_time = time.strftime("%Y.%m.%d %H:%M:%S", local_time)
            file.write(f"TIME: {formatted_time}\n")
            file.write("----------BEGIN-------------\n")
        run(args)

