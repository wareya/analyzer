import com.atilika.kuromoji.unidic.kanaaccent.Token;

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

    public static String toFurigana(Token token) {
        // Add furigana to kanji words only
        if(token.getSurface().matches("[\\u4e00-\\u9faf]+.*")) {
            String reading = Utils.toHiragana(token.getKana());
            String surface = token.getSurface();

            // 引[きこもり]
            for (int i = reading.length(); i > 0; i--) {
                if (reading.charAt(i - 1) == surface.charAt(surface.length() - 1)) {
                    surface = surface.substring(0, surface.length() - 1);
                    reading = reading.substring(0, reading.length() - 1);
                } else {

                    break;
                }
            }

            String finalReading = Utils.toHiragana(token.getKana());
            String tail = finalReading.substring(reading.length(), finalReading.length());

            return surface + "[" + reading + "]" + tail;
        } else {
            return token.getSurface();
        }
    }

}
