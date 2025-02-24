package expr;

import java.util.ArrayList;

// 项 | 表达式 op 项

public class Expression {
    private final ArrayList<Term> terms;
    private final ArrayList<String> operators;

    public Expression() {
        this.terms = new ArrayList<Term>();
        this.operators = new ArrayList<String>();
    }

    public void addTerm(Term term) {
        this.terms.add(term);
    }




}
