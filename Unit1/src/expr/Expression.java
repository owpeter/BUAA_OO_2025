package expr;

import poly.Poly;
import poly.Mono;
import poly.Add;
import poly.Power;

import java.math.BigInteger;
import java.util.ArrayList;

// 项 | 表达式 op 项

public class Expression implements Factor {
    private final ArrayList<Term> terms;
    private BigInteger exponential;
    // 已将String改成BigInteger

    public Expression() {
        this.terms = new ArrayList<Term>();
        this.exponential = BigInteger.ONE;
    }

    public Expression(ArrayList<Term> terms, BigInteger exponential) {
        this.terms = terms;
        this.exponential = exponential;
    }

    public void addTerm(Term term) {
        this.terms.add(term);
    }

    public ArrayList<Term> getTerms() {
        return this.terms;
    }

    public BigInteger getExponential() {
        return this.exponential;
    }

    public void setExponential(String exponential) {
        this.exponential = new BigInteger(exponential);
    }

    @Override
    public Poly toPoly() {
        Poly poly = new Poly();
        if (this.exponential.equals(BigInteger.ZERO)) {
            // 如果是0次方，直接返回1
            poly.addMono(new Mono(BigInteger.ONE, BigInteger.ZERO));
        } else {
            for (Term term : terms) {
                Add.polyAdd(poly, term.toPoly());
            }
            if (!this.exponential.equals(BigInteger.ONE)) {
                Power.polyPower(poly, this.exponential);
            }
        }
        return poly;
    }

    @Override
    public Mono toMono() {
        return null;
    }
}
