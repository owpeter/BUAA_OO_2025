package poly;

import expr.Expression;
import parse.Parser;
import parse.Lexer;
import org.junit.Test;

public class DerivateTest {

    @Test
    public void dExpr() {
<<<<<<< HEAD
        Expression expr = new Parser(new Lexer("dx(sin(x))")).parseExpression();
        System.out.println(Derivate.dExpr(expr).toPoly().toString());
=======
        Expression expr = new Parser(new Lexer("x^2+x+1+(x^8*x)")).parseExpression();
        Expression dExpr = Derivate.dExpr(expr);
        Expression expr2 = new Parser(new Lexer("cos(x^2)^2-sin(x)^2")).parseExpression();
        Expression dExpr2 = Derivate.dExpr(expr2);
>>>>>>> refs/remotes/origin/hw3
    }

    @Test
    public void dTerm() {
    }

    @Test
    public void dFactor() {
    }
}