public class StringTriple {
    private final String string1;
    private final String string2;
    private final String string3;

    public StringTriple(String string, String string2) {
        this(string, string2, null);
    }

    public StringTriple(String string, String string2, String string3) {
        this.string1 = string;
        this.string2 = string2;
        this.string3 = string3;
    }

    public String getString1() {
        return this.string1;
    }

    public String getString2() {
        return this.string2;
    }

    public String getString3() {
        return this.string3;
    }
}