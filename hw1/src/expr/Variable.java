package expr;

public class  Variable implements Factor {
    private final String exp;

    public Variable(String exp) {
        this.exp = exp;
    }
}
