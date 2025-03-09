import random
import re
import sympy
import threading

# --------------------- 线程安全配置 ---------------------
thread_local = threading.local()
globalPointer_lock = threading.Lock()

def get_thread_local_random():
    if not hasattr(thread_local, "rng"):
        seed = random.randint(0, 2**32-1)
        thread_local.rng = random.Random(seed)
    return thread_local.rng

# --------------------- 常量定义 ---------------------
intPool = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
           10, 11, 12, 13, 14, 15, 16,
           2147483647, 5223333333,
           5423333333,
           1145141919810,
           23333333234212332333,
           23333333233335467543,
           23495723459823752039]

hasWhiteSpace = True
hasLeadZeros = True
maxTerm = 4
maxFactor = 4
maxExp = 5
specialData = ["1", "x-x", "-1", "(x-1)**4-(x**2-2*x+1)**2"]
dataCost = [1, 2, 2, 32]
maxDepth = 2

# --------------------- 工具函数 ---------------------
def rd(a, b):
    return get_thread_local_random().randint(a, b)

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

# --------------------- 表达式生成核心 ---------------------
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

def getFactor(genExpr, depth):
    if depth > maxDepth:
        genExpr = False
    
    choice = rd(0, 3)
    if choice == 0:
        return getNum(False)
    elif choice == 1:
        return getPower()
    elif choice == 2 and genExpr:
        expr, cost = getExpr(True, depth+1)
        return f"({expr})", cost+2
    else:
        return getTri(depth)

def getTerm(genExpr, depth):
    factors = []
    cost = 1
    for _ in range(rd(1, maxFactor)):
        factor, factor_cost = getFactor(genExpr, depth)
        factors.append(factor)
        cost *= factor_cost
    
    symbol = getSymbol() + getWhiteSpace() if rd(0, 1) else ''
    return f"{symbol}{'*'.join(factors)}", cost

def getExpr(isFactor, depth=0):
    terms = []
    cost = 0
    for _ in range(rd(1, maxTerm)):
        term, term_cost = getTerm(not isFactor, depth)
        terms.append(f"{getSymbol()}{getWhiteSpace()}{term}")
        cost += term_cost
    
    expr = ''.join(terms)
    if isFactor:
        expr = f"({expr})"
        if rd(0, 1):
            exp, exp_cost = getExponent()
            expr += exp
            cost = max(cost, 2)**max(exp_cost, 1)
    return expr, cost

# --------------------- 数据生成入口 ---------------------
def genData():
    # 安全访问全局指针
    with globalPointer_lock:
        if not hasattr(thread_local, 'local_globalPointer'):
            thread_local.local_globalPointer = 0  # 从主线程初始化
            
        if thread_local.local_globalPointer < len(specialData):
            idx = thread_local.local_globalPointer
            thread_local.local_globalPointer += 1
            return specialData[idx], dataCost[idx]
    
    # 正常生成逻辑
    expr, cost = getExpr(False)
    return expr, cost

# --------------------- 测试代码 ---------------------
# if __name__ == '__main__':
#     # 初始化主线程状态
#     get_thread_local_random()
    
#     # 测试多线程生成
#     from concurrent.futures import ThreadPoolExecutor
    
#     def test_generation(_):
#         expr, cost = genData()
#         return f"{expr.ljust(30)} (cost: {cost})"
    
#     with ThreadPoolExecutor(max_workers=4) as executor:
#         results = list(executor.map(test_generation, range(20)))
    
#     print("\n".join(results))
#     print("\nSpecial cases used:", thread_local.local_globalPointer)