package expr;

import poly.Poly;
import poly.Mono;

import java.math.BigInteger;
import java.util.ArrayList;

// 项 | 表达式 op 项

public class Expression implements Factor {
    private final ArrayList<Term> terms;
    private String exponential;

    public Expression() {
        this.terms = new ArrayList<Term>();
    }

    public void addTerm(Term term) {
        this.terms.add(term);
    }

    public void setExponential(String exponential) {
        this.exponential = exponential;
    }

    @Override
    public Poly toPoly() {
        Poly poly = new Poly();
        if (this.exponential.equals("0")) {
            // 应当改成BigInteger的大小比较，不能直接比较字符串，否则00会有bug！

            // 如果是0次方，直接返回1
            poly.addMono(new Mono(BigInteger.ONE, BigInteger.ZERO));
        } else {
            for (Term term : terms) {
                poly.addPoly(term.toPoly());
            }
            if (!this.exponential.equals("1")) {
                poly.powPoly(this.exponential);
            }
        }
        return poly;
    }

    @Override
    public Mono toMono() {
        return null;
    }
}
