import json
import re
import sympy
import subprocess
from tqdm import tqdm
from gendata import genData
from subprocess import STDOUT, PIPE


def execute_java(stdin, fname):
    cmd = ['java', '-jar', fname]
    proc = subprocess.Popen(cmd, stdin=PIPE, stdout=PIPE, stderr=STDOUT)
    stdout, stderr = proc.communicate(stdin.encode())
    return stdout.decode().strip()


def main(fname, times=100):
    exprDict = dict()
    error = 0
    for _ in tqdm(range(times)):
        poly, cost = genData()
        # while cost > 10000:
        #     poly, ans, cost = genData()
        # print(poly)
        with open("now.txt", "w", encoding="utf-8") as file:
            file.write(poly)
        forSympy = poly.replace("\t", "")
        forSympy = forSympy.replace(" ", "")
        forSympy = re.sub(r'\b0+(\d+)\b', r'\1', forSympy)
        f = sympy.sympify(forSympy)
        sympy_result = sympy.expand(f)
        ans = str(sympy_result).replace("**", "^")
        poly = "0\n" + poly
        strr = execute_java(poly.replace("**", "^"), fname)
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
    main('hw1.jar', 10000)
