package expr;

import java.util.ArrayList;

// 因子 | 项 * 因子

public class Term {
    private ArrayList<Factor> factors;

    public Term() {
        this.factors = new ArrayList<Factor>();
    }

    public void addFactor(Factor factor) {
        this.factors.add(factor);
    }
}
