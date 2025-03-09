import json
import re
import sympy
import subprocess
from tqdm import tqdm
from gendata import genData
from subprocess import STDOUT, PIPE

from func import genFunc


def execute_java(stdin, fname):
    # input_lns = stdin.encode()
    cmd = ['java', '-jar', fname]
    proc = subprocess.Popen(
        cmd,
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        bufsize=0  # 设置缓冲区为 0，确保实时交互
    )
    
    # 逐步传递输入
    for input_str in stdin:
        # 将输入字符串转换为字节流并发送
        input_bytes = (input_str + '\n').encode('utf-8')  # 添加换行符
        proc.stdin.write(input_bytes)
        proc.stdin.flush()  # 确保数据立即发送
    
    output_line = proc.stdout.readline().decode().strip()

    proc.stdin.close()
    proc.wait()

    return output_line


def main(fname, times=100):
    exprDict = dict()
    error = 0
    for _ in tqdm(range(times)):
        use_poly, cost = genData()
        f0, f1, fn, ultilize,result = genFunc()
        print(f'{f0}\n{f1}\n{fn}\n')

        poly = f'{ultilize} + {use_poly.replace("**", "^")}'

        input_lns = ['1', f0, f1, fn, poly]
        print(poly)
        # while cost > 10000:
        #     poly, ans, cost = genData()
        # print(poly)
        # with open("now.txt", "w", encoding="utf-8") as file:
        #     file.write(input_lns)


        forSympy = use_poly.replace("\t", "")
        forSympy = forSympy.replace(" ", "")
        forSympy = re.sub(r'\b0+(\d+)\b', r'\1', forSympy)
        f = sympy.sympify(forSympy)
        sympy_result = sympy.expand(f) + sympy.expand(result)
        ans = str(sympy_result).replace("**", "^")
        # poly = "0\n" + poly
        strr = execute_java(input_lns, fname)
        # print(strr)
        try:
            g = sympy.sympify(sympy.parse_expr(strr.replace("^", "**")))
            g = sympy.expand(g)
            if sympy_result == g:
                # print("AC : " + str(cnt))
                exprDict[poly] = (1.0, ans)
                # print("x: {:.6f}".format(len(strr) / len(ans)))
                pass
            elif sympy.simplify(sympy.trigsimp(sympy_result - g)) == 0:
                strr = str(g).replace("**", "^")
                exprDict[poly] = (len(strr) / len(ans), ans)
                pass
            else:
                print("!!WA!! with " + "poly : " + poly.replace("**", "^"))
                with open("wrong.txt", "w", encoding="utf-8") as file :
                    file.write(poly.replace("**", "^"))
                print("yours: " + strr)
                print("sympy: ", end="")
                print(ans)
                return
        except Exception as e:
            print("Some error in the test_checker")
            print(e)
            # print("!!WA!! with " + "poly : " + poly.replace("**", "^"))
            # with open("wrong.txt", "w", encoding="utf-8") as file :
            #         file.write(poly.replace("**", "^"))
            # print("yours: " + strr)
            # print("sympy: ", end="")
            # print(ans)
            error += 1
            with open("timeout.txt", "a+", encoding="utf-8") as file:
                file.write(f"--------{error}----------\n")
                file.write(poly.replace("**", "^"))
                file.write(f"--------{error}----------\n")
                file.write(e)
                file.write("\n")
            pass
    sorted_exprDict = sorted(exprDict.items(), key=lambda x: x[1][0], reverse=True)
    print("worst score (x): " + str(sorted_exprDict[0][1][0]))
    print("best score (x): " + str(sorted_exprDict[-1][1][0]))
    output = list()
    for i in range(10):
        tmpdict = dict()
        tmpdict['generated_expression'] = sorted_exprDict[i][0].replace("**", "^")
        tmpdict['sympy_simplified'] = sorted_exprDict[i][1][1]
        tmpdict['score'] = sorted_exprDict[i][1][0]
        output.append(tmpdict)

        # print(sorted_exprDict[i][0].replace("**", "^"))
        # print(sorted_exprDict[i][1][1])
        # print(sorted_exprDict[i][1][0])
    with open('output.json', 'w') as f:
        json.dump(output, f)


if __name__ == '__main__':
    main('xxkcpj_hw2\hw1.jar', 10)
