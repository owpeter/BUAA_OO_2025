package poly;

import expr.Expression;
import expr.Term;
import expr.Variable;
import expr.Factor;
import expr.Constant;
import expr.TrigonometricFunction;

import java.math.BigInteger;
import java.util.ArrayList;

public class Derivate {
    public static Expression dExpr(Expression expr) {
        // f(x)^n -> n*f(x)^(n-1)*f'(x)
        // f'(x) -> t1' + t2' + ...
        Term dfTerm = new Term(true);
        if (expr.getExponential().equals(BigInteger.ZERO)) {
            Term term = new Term(true, new Constant(BigInteger.ZERO));
            return new Expression(term, BigInteger.ONE);
        } else if (!expr.getExponential().equals(BigInteger.ONE)) {
            dfTerm.addFactor(new Constant(expr.getExponential()));
            Expression gexpr =
                new Expression(expr.getTerms(), expr.getExponential().subtract(BigInteger.ONE));
            dfTerm.addFactor(gexpr);
        }

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
            ArrayList<Factor> dfirstfactors = new ArrayList<>();
            Factor dfirstFactor = dFactor(term.getFactors().get(0));
            dfirstfactors.add(dfirstFactor);
            // 将term中剩下的factor都加入dFirstFactors中
            for (int i = 1; i < term.getFactors().size(); i++) {
                dfirstfactors.add(term.getFactors().get(i));
            }
            ArrayList<Term> dterms = new ArrayList<>();
            Term dfirsterm = new Term(true, dfirstfactors);
            dterms.add(dfirsterm);

            // f(x)*g'(x)
            ArrayList<Factor> otherFactors =
                new ArrayList<>(term.getFactors().subList(1, term.getFactors().size()));
            Term otherTerms = new Term(true, otherFactors);
            Term dotherterm = dTerm(otherTerms);
            dotherterm.addFactor(term.getFactors().get(0));
            dterms.add(dotherterm);

            Expression expression = new Expression(dterms, BigInteger.ONE);
            ArrayList<Factor> dfactors = new ArrayList<>();
            dfactors.add(expression);
            return new Term(term.getSign(), dfactors);
        }
    }

    public static Factor dFactor(Factor factor) {
        if (factor instanceof Expression) {
            if (((Expression) factor).getExponential().equals(BigInteger.ZERO)) {
                return new Constant(BigInteger.ZERO);
            }
            return dExpr((Expression) factor);
        } else if (factor instanceof Constant) {
            return new Constant(BigInteger.ZERO);
        } else if (factor instanceof Variable) {
            if (((Variable) factor).getExp().equals(BigInteger.ZERO)) {
                return new Constant(BigInteger.ZERO);
            }
            ArrayList<Factor> factors = new ArrayList<>();
            Factor dvar = new Variable((((Variable) factor).getExp().subtract(BigInteger.ONE)));
            Factor n = new Constant((((Variable) factor).getExp()));
            factors.add(dvar);
            factors.add(n);
            Term term = new Term(true, factors);
            ArrayList<Term> terms = new ArrayList<>();
            terms.add(term);
            return new Expression(terms, BigInteger.ONE);
        } else if (factor instanceof TrigonometricFunction) {
            if (((TrigonometricFunction) factor).getExponent().equals(BigInteger.ZERO)
                || ((TrigonometricFunction) factor).getFactor() instanceof Constant) {
                return new Constant(BigInteger.ZERO);
            }
            return dTrig((TrigonometricFunction) factor);
        } else if (factor instanceof Term) {
            return dTerm((Term) factor);
        }
        return null;
    }

    private static Term dTrig(TrigonometricFunction trig) {
        switch (trig.getType()) {
            case "sin":
                // sin(f(x))^n -> n*sin(f(x))^(n-1)*f'(x)*cos(f(x))
                Term dsin = new Term(true);
                dsin.addFactor(new Constant(trig.getExponent()));
                dsin.addFactor(new TrigonometricFunction("sin", trig.getFactor(),
                    trig.getExponent().subtract(BigInteger.ONE)));
                dsin.addFactor(new TrigonometricFunction("cos", trig.getFactor(), BigInteger.ONE));
                dsin.addFactor(dFactor(trig.getFactor()));
                return dsin;

            case "cos":
                // cos(f(x))^n -> -n*cos(f(x))^(n-1)*f'(x)*sin(f(x))
                Term dcos = new Term(true);
                dcos.addFactor(new Constant(trig.getExponent().negate()));
                dcos.addFactor(new TrigonometricFunction("cos", trig.getFactor(),
                    trig.getExponent().subtract(BigInteger.ONE)));
                dcos.addFactor(new TrigonometricFunction("sin", trig.getFactor(), BigInteger.ONE));
                dcos.addFactor(dFactor(trig.getFactor()));
                return dcos;
            default:
                return null;
        }
    }
}
