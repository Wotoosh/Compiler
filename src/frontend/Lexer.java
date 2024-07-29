package frontend;

import frontend.errorchecker.ErrorChecker;
import frontend.node.Token;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class Lexer {
    String source;
    String token;
    int pos;
    int line;
    ArrayList<Token> tokens;
    HashMap<String, String> reserveWords;
    HashMap<String, String> ops;
    ErrorChecker errorChecker;
    boolean errorTesting;

    public Lexer(String s, ErrorChecker e, boolean hasError) {
        source = s;
        pos = 0;
        line = 1;
        token = new String();
        errorChecker = e;
        reserveWords = new HashMap<String, String>();
        reserveWords.put("main", "MAINTK");
        reserveWords.put("const", "CONSTTK");
        reserveWords.put("int", "INTTK");
        reserveWords.put("break", "BREAKTK");
        reserveWords.put("continue", "CONTINUETK");
        reserveWords.put("if", "IFTK");
        reserveWords.put("else", "ELSETK");
        reserveWords.put("for", "FORTK");
        reserveWords.put("getint", "GETINTTK");
        reserveWords.put("printf", "PRINTFTK");
        reserveWords.put("return", "RETURNTK");
        reserveWords.put("void", "VOIDTK");
        tokens = new ArrayList<>();
        ops = new HashMap<>();
        ops.put("+", "PLUS");
        ops.put("-", "MINU");
        ops.put("*", "MULT");
        ops.put("%", "MOD");
        ops.put(";", "SEMICN");
        ops.put(",", "COMMA");
        ops.put("(", "LPARENT");
        ops.put(")", "RPARENT");
        ops.put("[", "LBRACK");
        ops.put("]", "RBRACK");
        ops.put("{", "LBRACE");
        ops.put("}", "RBRACE");
        source = source.replaceAll("\r", "");
        errorTesting = hasError;
    }

    public void print(FileOutputStream fout) {
        for (Token item : tokens) {
            try {
                fout.write(item.toString().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void cat() {
        token += source.charAt(pos);
    }

    public void clearToken() {
        token = new String();
    }

    public boolean isReserve() {
        if (reserveWords.containsKey(token)) {
            return true;
        } else {
            return false;
        }
    }

    public String peek() {
        return this.token;
    }

    public boolean isSpace() {
        return " ".equals(getChar());
    }

    public boolean isNewline() {
        return "\n".equals(getChar());
    }

    public boolean isTab() {
        return "\t".equals(getChar());
    }

    public boolean isLetter() {
        return Character.isLetter(source.charAt(pos));
    }

    public boolean isNondigit() {
        return Character.isLetter(source.charAt(pos)) || "_".equals(getChar());
    }

    public String getChar() {
        return source.substring(pos, pos + 1);
    }

    public void next() {
        pos++;
    }

    public boolean isDigit() {
        return Character.isDigit(source.charAt(pos));
    }

    public boolean isEnd() {
        return pos >= source.length();
    }

    public void getWord() {
        Token t;
        while (!isEnd() && (isNondigit() || isDigit())) {
            cat();
            next();
        }
        if (isReserve()) {
            t = new Token(reserveWords.get(token), token, line);
            tokens.add(t);
        } else {
            t = new Token("IDENFR", token, line);
            tokens.add(t);
        }
    }

    public void getNum() {
        while (!isEnd() && isDigit()) {
            cat();
            next();
        }
        Token t = new Token("INTCON", token, line);
        tokens.add(t);
    }

    public void getFormatString() {
        next();
        while (!isEnd() && !"\"".equals(getChar())) {
            cat();
            next();
        }
        cat();
        next();
        Token t = new Token("STRCON", token, line);
        tokens.add(t);
    }

    public void jumpOneLine() {
        while (!isEnd() && !isNewline()) {
            next();
        }
        line++;
        next();
    }

    public void jumpMulLine() {
        while (!(source.charAt(pos) == '*' && source.charAt(pos + 1) == '/')) {
            next();
            if (isNewline()) {
                line++;
            }
        }
        next();
        next();
    }

    public void analyze() {
        while (!isEnd()) {
            clearToken();
            while (!isEnd() && (isSpace() || isNewline() || isTab())) {
                if (isNewline()) {
                    line++;
                }
                next();
            }
            if (isEnd()) {
                break;
            }
            if (isNondigit()) {
                getWord();
            } else if (isDigit()) {
                getNum();
            } else if ("\"".equals(getChar())) {
                cat();
                getFormatString();
            } else if ("/".equals(getChar())) {
                if (source.charAt(pos + 1) == '/') {
                    jumpOneLine();
                } else if (source.charAt(pos + 1) == '*') {
                    jumpMulLine();
                } else {
                    cat();
                    Token t = new Token("DIV", "/", line);
                    tokens.add(t);
                    next();
                }
            } else if (ops.containsKey(getChar())) {
                cat();
                Token t = new Token(ops.get(getChar()), token, line);
                tokens.add(t);
                next();
            } else if ("&".equals(getChar())) {
                cat();
                next();
                if (!isEnd() && "&".equals(getChar())) {
                    cat();
                    Token t = new Token("AND", token, line);
                    tokens.add(t);
                    next();
                }
                //否则错误
            } else if ("|".equals(getChar())) {
                cat();
                next();
                if (!isEnd() && "|".equals(getChar())) {
                    cat();
                    Token t = new Token("OR", token, line);
                    tokens.add(t);
                    next();
                }
                //否则错误
            } else if ("<".equals(getChar())) {
                cat();
                next();
                if (!isEnd() && "=".equals(getChar())) {
                    cat();
                    Token t = new Token("LEQ", token, line);
                    tokens.add(t);
                    next();
                } else {
                    Token t = new Token("LSS", token, line);
                    tokens.add(t);
                }
            } else if (">".equals(getChar())) {
                cat();
                next();
                if (!isEnd() && "=".equals(getChar())) {
                    cat();
                    Token t = new Token("GEQ", token, line);
                    tokens.add(t);
                    next();
                } else {
                    Token t = new Token("GRE", token, line);
                    tokens.add(t);
                }
            } else if ("=".equals(getChar())) {
                cat();
                next();
                if (!isEnd() && "=".equals(getChar())) {
                    cat();
                    Token t = new Token("EQL", token, line);
                    tokens.add(t);
                    next();
                } else {
                    Token t = new Token("ASSIGN", token, line);
                    tokens.add(t);
                }
            } else if ("!".equals(getChar())) {
                cat();
                next();
                if (!isEnd() && "=".equals(getChar())) {
                    cat();
                    Token t = new Token("NEQ", token, line);
                    tokens.add(t);
                    next();
                } else {
                    Token t = new Token("NOT", token, line);
                    tokens.add(t);
                }
            }
        }
    }

    public ArrayList<Token> getTokens() {
        return tokens;
    }
}
