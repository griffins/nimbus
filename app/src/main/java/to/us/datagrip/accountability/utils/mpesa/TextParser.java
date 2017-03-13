package to.us.datagrip.accountability.utils.mpesa;

import java.util.ArrayList;


class TextParser {
    private ArrayList<String> tokens;
    private String text;

    private TextParser(String text) {
        this.text = text;
        tokenize();
    }

    private TextParser tokenize() {
        tokens = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        char previousChar = '\n';
        for (char c : text.toCharArray()) {
            if (c == ' ' || !isSpannable(c, previousChar)) {
                if (builder.length() > 0) {
                    if (previousChar == '.') {
                        builder.delete(builder.length() - 1, builder.length());
                    }
                    tokens.add(builder.toString());
                    builder = new StringBuilder();
                    if (c != ' ' && c != '.') {
                        builder.append(c);
                        previousChar = '\n';
                    } else {
                        previousChar = '\n';
                    }
                }
            } else {
                builder.append(c);
                previousChar = c;
            }
        }
        if (builder.length() > 0) {
            if (previousChar == '.') {
                builder.delete(builder.length() - 1, builder.length());
            }
            tokens.add(builder.toString());
        }
//        for (String token : tokens) {
//            Main.print(token);
//        }
        return this;
    }

    private boolean isSpannable(char c, char previousChar) {
        if (Character.isDigit(c) && Character.isDigit(previousChar)) {
            return true;
        }
        if (Character.isLetter(c) && Character.isLetter(previousChar)) {
            return true;
        }

        if (Character.isUpperCase(c) && Character.isUpperCase(previousChar)) {
            return true;
        }

        if (Character.isLowerCase(c) && Character.isLowerCase(previousChar)) {
            return true;
        }

        if (Character.isUpperCase(c) && Character.isLowerCase(previousChar)) {
            return true;
        }
        if (Character.isUpperCase(c) && Character.isDigit(previousChar)) {
            return true;
        }

        if (Character.isDigit(c) && Character.isUpperCase(previousChar)) {
            return true;
        }
        if (Character.isDigit(c) && previousChar == '.') {
            return true;
        }

        if (c == '.' && Character.isDigit(previousChar)) {
            return true;
        }

        if (Character.isLetterOrDigit(c) && isSpannableCharacter(previousChar)) {
            return true;
        }
        if (isSpannableCharacter(c) && Character.isLetterOrDigit(previousChar)) {
            return true;
        }

        if (previousChar == '\n') {
            return true;
        }
//        Main.print(String.format("%s and %s failed to span", previousChar, c));
        return false;
    }

    private boolean isSpannableCharacter(char c) {
        char chars[] = {'#', '*', '/', ':', '!', ',', '-'};
        for (char s : chars) {
            if (c == s) {
                return true;
            }
        }
        return false;
    }

    public static TextParser from(String text) {
        return new TextParser(text);
    }

    public ArrayList<String> getTokens() {
        return tokens;
    }
}