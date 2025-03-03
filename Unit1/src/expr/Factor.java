package expr;

import poly.Poly;
import poly.Mono;

public interface Factor {
    public Poly toPoly();

    public Mono toMono();

    public String toString();
}
