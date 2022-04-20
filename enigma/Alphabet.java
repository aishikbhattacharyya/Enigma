package enigma;

/**
 * An alphabet of encodable characters.  Provides a mapping from characters
 * to and from indices into the alphabet.
 *
 * @author Aishik Bhattacharyya
 */
class Alphabet {
    /**
     * Alphabet string worked on.
     */
    private String _chars;

    /**
     * A new alphabet containing CHARS. The K-th character has index
     * K (numbering from 0). No character may be duplicated.
     */
    Alphabet(String chars) {
        _chars = chars;
    }

    /**
     * A default alphabet of all upper-case characters.
     */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /**
     * Returns the size of the alphabet.
     */
    int size() {
        return _chars.length();
    }

    /**
     * Returns true if CH is in this alphabet.
     */
    boolean contains(char ch) {
        return _chars.contains(String.valueOf(ch));
    }

    /**
     * Returns character number INDEX in the alphabet, where
     * 0 <= INDEX < size().
     */
    char toChar(int index) {
        index = index % size();
        return _chars.charAt(index);
    }

    /**
     * Returns the index of character CH which must be in
     * the alphabet. This is the inverse of toChar().
     */
    int toInt(char ch) {
        return _chars.indexOf(ch);
    }

    Alphabet rotate(char ch) {
        String newAlpha = "";
        int newStart = _chars.indexOf(ch);
        for (int i = newStart; i < _chars.length() + newStart; i++) {
            int pos = i % _chars.length();
            newAlpha += _chars.charAt(pos);
        }
        return new Alphabet(newAlpha);
    }

}
