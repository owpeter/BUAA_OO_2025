package poly;

import expr.*;

import java.math.BigInteger;
import java.util.ArrayList;

public class Derivate {
    public static Expression dExpr(Expression expr) {
        // f(x)^n -> n*f(x)^(n-1)*f'(x)
        // f'(x) -> t1' + t2' + ...
        Term dfTerm = new Term(true); // TODO: 符号？
        dfTerm.addFactor(new Constant(expr.getExponential()));
        Expression gExpr = new Expression(expr.getTerms(), expr.getExponential().subtract(BigInteger.ONE));
        dfTerm.addFactor(gExpr);
        // f'(x)
        Expression dfExpr = new Expression();
        for (Term term : expr.getTerms()) {
            dfExpr.addTerm(dTerm(term));
        }
        dfTerm.addFactor(dfExpr);

        ArrayList<Term> terms = new ArrayList<>();
        terms.add(dfTerm);
        return new Expression(terms, BigInteger.ONE);
    }

    public static Term dTerm(Term term) {
        ArrayList<Factor> factors = new ArrayList<>();
        if (term.getFactors().size() == 1) {
            factors.add(dFactor(term.getFactors().get(0)));
            return new Term(term.getSign(), factors);
        } else {

            // f'(x)*g(x)
            ArrayList<Factor> dFirstFactors = new ArrayList<>();
            Factor dfirstFactor = dFactor(term.getFactors().get(0));
            dFirstFactors.add(dfirstFactor);
            // 将term中剩下的factor都加入dFirstFactors中
            for (int i = 1; i < term.getFactors().size(); i++) {
                dFirstFactors.add(term.getFactors().get(i));
            }
            ArrayList<Term> dTerms = new ArrayList<>();
            Term dFirstTerm = new Term(true, dFirstFactors);
            dTerms.add(dFirstTerm);

            // f(x)*g'(x)
            ArrayList<Factor> otherFactors = new ArrayList<>(term.getFactors().subList(1, term.getFactors().size()));
            Term otherTerms = new Term(true, otherFactors);
            Term dOtherTerm = dTerm(otherTerms);
            dOtherTerm.addFactor(term.getFactors().get(0));
            dTerms.add(dOtherTerm);

            Expression expression = new Expression(dTerms, BigInteger.ONE);
            ArrayList<Factor> dFactors = new ArrayList<>();
            dFactors.add(expression);
            return new Term(term.getSign(), dFactors);
        }
    }

    public static Factor dFactor(Factor factor) {
        if (factor instanceof Expression) {
            if (((Expression) factor).getExponential().equals(BigInteger.ZERO)) {
                return new Constant(BigInteger.ONE);
            }
            return dExpr((Expression) factor);
        } else if (factor instanceof Constant) {
            return new Constant(BigInteger.ZERO);
        } else if (factor instanceof Variable) {
            if (((Variable) factor).getExp().equals(BigInteger.ZERO)) {
                return new Constant(BigInteger.ONE);
            }
            ArrayList<Factor> factors = new ArrayList<>();
            Factor dVar = new Variable((((Variable) factor).getExp().subtract(BigInteger.ONE)));
            Factor n = new Constant((((Variable) factor).getExp()));
            factors.add(dVar);
            factors.add(n);
            Term term = new Term(true, factors);
            ArrayList<Term> terms = new ArrayList<>();
            terms.add(term);
            return new Expression(terms, BigInteger.ONE);
        } else if (factor instanceof TrigonometricFunction) {
            if (((TrigonometricFunction) factor).getExponent().equals(BigInteger.ZERO)) {
                return new Constant(BigInteger.ONE);
            }
            return dTrig((TrigonometricFunction) factor);
        }
        return null;
    }

    private static Term dTrig(TrigonometricFunction trig) {
//        if (trig.getExponent().equals(BigInteger.ZERO)) {
//            return new Term(true);
//        }
        switch (trig.getType()) {
            case "sin":
                // sin(f(x))^n -> n*sin(f(x))^(n-1)*f'(x)*cos(f(x))
                Term dSin = new Term(true);
                dSin.addFactor(new Constant(trig.getExponent()));
                dSin.addFactor(new TrigonometricFunction("sin", trig.getFactor(), trig.getExponent().subtract(BigInteger.ONE)));
                dSin.addFactor(new TrigonometricFunction("cos", trig.getFactor(), BigInteger.ONE));
                dSin.addFactor(dFactor(trig.getFactor()));
                return dSin;

            case "cos":
                // cos(f(x))^n -> -n*cos(f(x))^(n-1)*f'(x)*sin(f(x))
                Term dCos = new Term(true);
                dCos.addFactor(new Constant(trig.getExponent().negate()));
                dCos.addFactor(new TrigonometricFunction("cos", trig.getFactor(), trig.getExponent().subtract(BigInteger.ONE)));
                dCos.addFactor(new TrigonometricFunction("sin", trig.getFactor(), BigInteger.ONE));
                dCos.addFactor(dFactor(trig.getFactor()));
                return dCos;
            default:
                return null;
        }
    }
}
