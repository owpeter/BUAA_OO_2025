package expr;

import Poly.*;

public interface Factor {
    public Poly toPoly();

    public Mono toMono();

    public String toString();
}
