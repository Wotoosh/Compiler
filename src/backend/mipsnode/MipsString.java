package backend.mipsnode;

public class MipsString extends MipsData {
    private String value;

    @Override
    public int getSize() {
        String s=value.replaceAll("\\\\","");
        if ((s.length() + 1) % 4 == 0) {
            return s.length() + 1;
        } else {
            return s.length() + 5 - (s.length() + 1) % 4;
        }
    }

    public MipsString(String s, String value, int num) {
        size = num;
        StringBuilder sb = new StringBuilder();
        name = s;
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) != '\\') {
                sb.append(value.charAt(i));
            } else {
                if (i < value.length() - 2) {
                    if (value.charAt(i + 1) == '0' && value.charAt(i + 2) == 'A') {
                        sb.append("\\n");
                        i += 2;
                    }
                }
            }
        }
        this.value = sb.toString();
    }

    @Override
    public String toString() {
        return name.substring(1) + ":.asciiz \"" + value + "\"" + "\n" + " .align 2" + "\n";
    }

}
