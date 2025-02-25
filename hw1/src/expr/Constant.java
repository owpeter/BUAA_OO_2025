package expr;

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;

public class Constant implements Factor {
    private String constant;

    public Constant(String constant) {
        this.constant = constant;
    }
}
