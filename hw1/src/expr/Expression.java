package expr;

import Poly.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;

// 项 | 表达式 op 项

public class Expression implements Factor {
    private final ArrayList<Term> terms;
//    private final ArrayList<String> operators;
    private String exponential;

    public Expression() {
        this.terms = new ArrayList<Term>();
//        this.operators = new ArrayList<String>();
    }

    public void addTerm(Term term) {
        this.terms.add(term);
    }

    public void setExponential(String exponential) {
        this.exponential = exponential;
    }

//    public ArrayList<String> getOperators() {
//        return this.operators;
//    }

    @Override
    public Poly toPoly() {
        Poly poly = new Poly();
        if (this.exponential.equals("0")) {
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

        // debug???
//        Simpler simpler = new Simpler(poly);
//        poly = simpler.simplerPoly();
        return poly;
    }

    @Override
    public Mono toMono() {
        return null;
    }

//    public String toString() {
//        Iterator<Term> iter = terms.iterator();
//        StringBuilder sb = new StringBuilder();
//
//
//        sb.append(iter.next().toString());
//        int i = 0;
//        while (iter.hasNext()) {
//            sb.append(iter.next().toString());
//            String op = operators.get(i);
//            sb.append(op);
//            i++;
//        }
////        if (!exponential.equals("1")) {
////            // 指数不为1，不可忽略
////            sb.append("^");
////            sb.append(exponential);
////        }
//        return sb.toString();
//    }


}
