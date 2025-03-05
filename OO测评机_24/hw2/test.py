# test.py
import os
import subprocess
import sympy
from sympy import symbols, expand, Poly
import concurrent.futures
import time

import gen
# from hw_2 import gen

class JarTester:
    def __init__(self):
        self.__jar_files = []
        self.__finder_executed = False
    
    def __find_jar_files(self):
        """Search for JAR files in the current directory"""
        if not self.__finder_executed:
            self.__jar_files = [f for f in os.listdir('.') if f.endswith('.jar')]
            self.__finder_executed = True
        return len(self.__jar_files) > 0
    
    def __run_jar_file(self, jar_path, input_expr):
        """Run a JAR file and get its output"""
        try:
            start_time = time.time()
            
            process = subprocess.Popen(['java', '-jar', jar_path], 
                                      stdin=subprocess.PIPE,
                                      stdout=subprocess.PIPE,
                                      stderr=subprocess.PIPE,
                                      text=True)
            
            stdout, stderr = process.communicate(input=input_expr, timeout=10)

            print("run cmplt")
            
            execution_time = time.time() - start_time
            
            if process.returncode == 0:
                return jar_path, stdout.strip(), execution_time, None
            else:
                return jar_path, None, execution_time, f"JAR execution error: {stderr}"
        except subprocess.TimeoutExpired:
            process.kill()
            return jar_path, None, 10, "JAR execution timeout"
        except Exception as e:
            return jar_path, None, 0, f"Error running JAR: {e}"

    def __compare_expressions(self, sympy_expr, jar_output):
        """Compare sympy expression with jar output for equivalence"""
        if sympy_expr is None or jar_output is None:
            return False
        
        jar_output = jar_output.replace('^', '**')
        
        try:
            x = symbols('x')

            math_funcs = {
                "sin": sympy.sin,
                "cos": sympy.cos,
                "x": x,
                "sympy": sympy
            }

            jar_expr = eval(jar_output, {"x": x, "__builtins__": {}}, math_funcs)
            jar_expr = expand(jar_expr)
            
            diff = expand(sympy_expr - jar_expr)
            
            if diff == 0:
                return True
            
            p1 = Poly(sympy_expr, x)
            p2 = Poly(jar_expr, x)
            
            return p1 == p2
        except Exception as e:
            print(f"Error comparing expressions: {e}")
            return False

    def __process_jar(self, jar_file, input_expr, sympy_expr):
        """Process a single JAR file"""
        jar_path, jar_result, execution_time, error = self.__run_jar_file(jar_file, input_expr)

        
        result = {
            "jar_file": jar_file,
            "execution_time": execution_time,
            "success": False,
            "output": None,
            "matches_sympy": False,
            "error": error
        }
        
        if jar_result:
            result["success"] = True
            result["output"] = jar_result
            result["matches_sympy"] = self.__compare_expressions(sympy_expr, jar_result)



        print("compare cplt")
        
        return result

    def __clear_screen(self):
        """Clear the terminal screen"""
        if os.name == 'nt':  # Windows
            os.system('cls')
        else:  # macOS, Linux, Unix
            os.system('clear')

    def __display_results(self, results, sympy_expr):
        """Display and validate test results"""
        flag = True
        for result in results:
            if result["success"]:
                if not result["matches_sympy"]:
                    print(f"\n{result['jar_file']}:")
                    print(f"  jar output: {result['output']}")
                    print(f"sympy output: {sympy_expr}")
                    print("  ✗ Result doesn't match Sympy")
                    flag = False
            else:
                print(f"\n{result['jar_file']}:")
                print(f"  ✗ Execution failed: {result['error']}")
                flag = False
        
        if not flag:
            print(f"{'JAR':<30} | {'Time(s)':<10} | {'Run':<5} | {'Correct':<10}")
            print("-" * 60)
            
            for result in results:
                jar_name = result["jar_file"]
                time_str = f"{result['execution_time']:.3f}"
                success = "✓" if result["success"] else "✗"
                matches = "✓" if result["matches_sympy"] else "✗"
                
                print(f"{jar_name:<30} | {time_str:<10} | {success:<5} | {matches:<10}")
            
            return False
        return True

    def run_tests(self):
        """Run tests on all JAR files"""
        if not self.__find_jar_files():
            print("No JAR files found in the current directory")
            return
        
        print(f"Found {len(self.__jar_files)} JAR files, press Enter to begin concurrent processing...")
        input()
        
        cnt = 0
        while True:
            cnt += 1
            self.__clear_screen()
            print(cnt)
            
            input_expr, sympy_expr = gen.TestGenerator.genData()
            results = []
            
            with concurrent.futures.ThreadPoolExecutor() as executor:
                future_to_jar = {
                    executor.submit(self.__process_jar, jar_file, input_expr, sympy_expr): jar_file 
                    for jar_file in self.__jar_files
                }
                
                for future in concurrent.futures.as_completed(future_to_jar):
                    jar_file = future_to_jar[future]
                    try:
                        results.append(future.result())
                    except Exception as e:
                        results.append({
                            "jar_file": jar_file,
                            "execution_time": 0,
                            "success": False,
                            "output": None,
                            "matches_sympy": False,
                            "error": f"Processing exception: {e}"
                        })
            
            # Sort results by execution time
            results.sort(key=lambda x: x["execution_time"])
            
            if not self.__display_results(results, sympy_expr):
                break


def main():
    tester = JarTester()
    try:
        tester.run_tests()
    except KeyboardInterrupt:
        print("\nProgram interrupted by user")
    except Exception as e:
        print(f"Program error: {e}")


if __name__ == "__main__":
    main()