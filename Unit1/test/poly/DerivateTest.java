package poly;

import expr.Expression;
import parse.Parser;
import parse.Lexer;
import org.junit.Test;

public class DerivateTest {

    @Test
    public void dExpr() {
        Expression expr = new Parser(new Lexer("dx(sin(x))")).parseExpression();
        System.out.println(Derivate.dExpr(expr).toPoly().toString());
    }

    @Test
    public void dTerm() {
    }

    @Test
    public void dFactor() {
    }
}