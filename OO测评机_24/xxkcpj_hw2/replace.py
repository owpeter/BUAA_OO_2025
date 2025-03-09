import sympy
import re

sp = "386547056460*x^12 + 1159641169380*x^9*sin(x^5)^2*cos(x^2) + 1159641169380*x^8*sin(1)*sin(105*cos(13)*cos(x^2))*cos(1) + 1159641169380*x^6*sin(x^5)^4*cos(x^2)^2 + 2319282338760*x^5*sin(1)*sin(x^5)^2*sin(105*cos(13)*cos(x^2))*cos(1)*cos(x^2) + x^5*sin(9)*cos(x)^3 + x^5*sin(9) + 1159641169380*x^4*sin(1)^2*sin(105*cos(13)*cos(x^2))^2*cos(1)^2 + 386547056460*x^3*sin(x^5)^6*cos(x^2)^3 + 1159641169380*x^2*sin(1)*sin(x^5)^4*sin(105*cos(13)*cos(x^2))*cos(1)*cos(x^2)^2 + 1159641169380*x*sin(1)^2*sin(x^5)^2*sin(105*cos(13)*cos(x^2))^2*cos(1)^2*cos(x^2) + 386547056460*sin(1)^3*sin(105*cos(13)*cos(x^2))^3*cos(1)^3"
my = "386547056460*x^3*sin(x^5)^6*cos(x^2)^3+1159641169380*x^4*sin((105*cos(x^2)*cos(13)))^2*sin(1)^2*cos(1)^2+x^5*sin(9)+1159641169380*x^8*sin((105*cos(x^2)*cos(13)))*sin(1)*cos(1)+1159641169380*x^6*sin(x^5)^4*cos(x^2)^2+1546188225840*x^5*sin(x^5)^2*sin((105*cos(x^2)*cos(13)))*sin(1)*cos(x^2)*cos(1)+1159641169380*x^9*sin(x^5)^2*cos(x^2)+386547056460*x^12+1159641169380*x^2*sin(x^5)^4*sin((105*cos(x^2)*cos(13)))*sin(1)*cos(x^2)^2*cos(1)+386547056460*x^5*sin(x^5)^2*sin((105*cos(x^2)*cos(13)))*sin(2)*cos(x^2)+1159641169380*x*sin(x^5)^2*sin((105*cos(x^2)*cos(13)))^2*sin(1)^2*cos(x^2)*cos(1)^2+386547056460*sin((105*cos(x^2)*cos(13)))^3*sin(1)^3*cos(1)^3+x^5*sin(9)*cos(x)^3"
poly = "0 -x*sin  ( +009)* (-x * x^ +2*0+- x^+3 -x^ +3 *cos ( x)^3)  *  x ++2147483647*(- - x^4*sin  (x ) ^ 0  + sin((+ 0*0  )  ^ 0 )^+1 *sin ((+  0-+-004*0* x*  x  -cos(-13 )* cos(x ^ +2)* -007 * +015 -0*0 *cos (cos ( (- -006 *cos(007 ) ^+3 * sin ( +5223333333 )*cos(+003) ^ 5+ +x  *cos( ( -  -  x * 00--0  +cos( (+- x ^ +1*  07 *  0++cos ( (+- sin(23333333233335467543) ++x *0  -x*9++cos ( x)  *5423333333* x *003) )  ^ +4*cos (+13 )* -009))^ +3* cos(5423333333 )*+011))  ^ +3 *x++  cos(cos (x^+3) ^3 )*x * 0* x^ +1 ) ^ 4) )^+4)) * cos((+0*0  * +007 )^+0) --x^+1*sin  (x) ^0*cos (x ^ 2  )* sin(x ^5 )  ^+2-5* 0 *0 )^3* +15 *  012"
forSympy = poly.replace("\t", "")
forSympy = forSympy.replace(" ", "")
forSympy = re.sub(r'\b0+(\d+)\b', r'\1', forSympy)
f = sympy.sympify(forSympy)
sympy_result = sympy.expand(f)

g = sympy.sympify(sympy.parse_expr(my.replace("^", "**")))
g = sympy.expand(g)

print(sympy.simplify(sympy.trigsimp(sympy_result - g)))
print(sympy_result)
print('------------')
print(g)




