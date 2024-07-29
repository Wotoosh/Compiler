package frontend.node;

public class FormatString implements Node {
    private String formatString;

    public FormatString(String formatString) {
        this.formatString = formatString;
    }

    public String getFormatString() {
        return formatString;
    }
}
