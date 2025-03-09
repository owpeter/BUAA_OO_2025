import random
import re
import threading
import sympy

# thread_local = threading.local()
# globalPointer_lock = threading.Lock()

# def get_thread_local_random():
#     if not hasattr(thread_local, "rng"):
#         seed = random.randint(0, 2**32-1)
#         thread_local.rng = random.Random(seed)
#     return thread_local.rng

intPool = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
           10, 11, 12, 13, 14, 15, 16,
           2147483647, 5223333333,
           5423333333,
           1145141919810,
           23333333234212332333,
           23333333233335467543,
           23495723459823752039
           ]                        # 常量池
hasWhiteSpace = True                # 是否加入空白字符
hasLeadZeros = True                 # 数字是否有前导零，如果传入sympy的表达式中数字有前导零，sympy将无法识别
maxTerm = 4                        # 表达式中的最大项数
maxFactor = 4                      # 项中最大因子个数
maxExp = 5
specialData = ["1", "x-x", "-1", "(x-1)**4-(x**2-2*x+1)**2"]    # 可以放一些特殊数据
dataCost = [1, 2, 2, 32]
globalPointer = 0
maxDepth = 5
depth = 0


def rd(a, b):
    return random.randint(a, b)

# def rd(a, b):
#     return get_thread_local_random().randint(a, b)

def getWhiteSpace():
    if not hasWhiteSpace:
        return ""
    blankTerm = ""
    cnt = rd(0, 2)
    for i in range(cnt):
        type = rd(0, 1)
        if type == 0:
            blankTerm = blankTerm + " "
        else:
            blankTerm = blankTerm + "\t"
    return blankTerm


def getSymbol():
    if rd(0, 1) == 1:
        return "+"
    else:
        return "-"


def getNum(posOnly):
    result = ""
    integer = intPool[rd(0, len(intPool) - 1)]
    cost = len(str(integer))
    iszero = rd(0, 2)
    for i in range(iszero):
        result = result + "0"
    if not hasLeadZeros:
        result = ""
    result = result + str(integer)
    if rd(0, 1) == 1:
        if posOnly:
            result = "+" + result
        else:
            result = getSymbol() + result
            # print("num:"+result)
        cost += 1
    return result, cost


def getExponent():
    result = "**"
    result = result + getWhiteSpace()
    case = rd(0, maxExp)
    cost = len(str(case))
    if rd(0, 1) == 1:
        result = result + "+"
        cost += 1
    result = result + str(case)
    # print("exponent: " + result)
    return result, cost


def getPower():
    result = "x"
    if rd(0, 1) == 1:
        toAdd, _ = getExponent()
        result = result + getWhiteSpace() + toAdd
    # print("Power:"+result)
    return result, 1

def getTri():
    cost = 1
    result = ""
    result += random.choice(["sin", "cos"])
    result += getWhiteSpace() + "("
    toAdd, factorCost = getFactor(True)
    cost += factorCost
    result += getWhiteSpace() + toAdd + getWhiteSpace()
    result += ")"
    if rd(0, 1) == 1:
        toAdd, expCost = getExponent()
        result += getWhiteSpace() + toAdd
        cost = 2 ** expCost + cost
    return result, cost


def getFactor(genExpr):
    global depth
    if (depth > maxDepth):
        genExpr = False
    factor = rd(0, 3)
    result = ""
    if factor == 0:
        result, factorCost = getNum(False)
    elif factor == 1:
        result, factorCost = getPower()
    elif factor == 2 and genExpr:
        depth += 1
        result, factorCost = getExpr(True)
        depth -= 1
    elif factor == 3:
        result, factorCost = getTri() 
    else:
        result = "0"
        factorCost = 1
    return result, factorCost

def getTerm(genExpr):
    factorNum = rd(1, maxFactor)
    result = ""
    cost = 1
    if rd(0, 1) == 1:
        result = getSymbol() + getWhiteSpace()
    for i in range(factorNum):
        toAdd, factorCost = getFactor(genExpr);
        result = result + toAdd
        cost *= factorCost
        if i < factorNum - 1:
            result = result + getWhiteSpace() + "*" + getWhiteSpace()
            # print("term:"+result)
    return result, cost


def getExpr(isFactor):
    termNum = rd(1, maxTerm)
    result = getWhiteSpace()
    cost = 0
    genExpr = True
    if isFactor:
        genExpr = False
    for i in range(termNum):
        toAdd, termCost = getTerm(genExpr)
        result = result + getSymbol() + getWhiteSpace() + toAdd + getWhiteSpace()
        cost += termCost
    if isFactor:
        result = "(" + result + ")"
        if rd(0, 1) == 1:
            toAdd, expCost = getExponent()
            result = result + getWhiteSpace() + toAdd
            cost = max(cost, 2) ** max(expCost, 1)
            # print("Expr:"+result)
    return result, cost


def genData():
    global globalPointer
    if globalPointer < len(specialData):
        expr = specialData[globalPointer]
        cost = dataCost[globalPointer]
        globalPointer = globalPointer + 1
    else:
        expr, cost = getExpr(False)
    # print(expr)
    return str(expr), cost
# def genData():
#     # 安全访问全局指针
#     with globalPointer_lock:
#         if not hasattr(thread_local, 'local_globalPointer'):
#             thread_local.local_globalPointer = 0  # 从主线程初始化
            
#         if thread_local.local_globalPointer < len(specialData):
#             idx = thread_local.local_globalPointer
#             thread_local.local_globalPointer += 1
#             return specialData[idx], dataCost[idx]
    
    # 正常生成逻辑
    expr, cost = getExpr(False)
    return expr, cost


if __name__ == '__main__':
    while True:
        poly,  cost = genData()
        # if(len(poly) >= 50 or cost >)
        # print(poly)
        # print(cost)
        # print('----------')
        # input()
