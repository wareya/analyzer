public class Utils {

    /**
     * Translates this character into the equivalent Hiragana character.
     * The function only operates on Katakana characters
     * If the character is outside the Full width or Half width
     * Katakana then the origianal character is returned.
     */
    public static char toHiragana(char c) {
        return (char) (c - 0x60);
    }

    public static String toHiragana(String s) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            result.append(toHiragana(s.charAt(i)));
        }
        return result.toString();
    }

}
