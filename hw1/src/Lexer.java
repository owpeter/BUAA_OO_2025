public class Lexer {
    private final String input;
    private int pos = 0;
    private String curToken;

    public Lexer(String input) {
        this.input = input; // preprocess
        this.pos = pos;
    }

    public void next() {

    }

    public String peek() {
        return this.curToken;
    }
}
