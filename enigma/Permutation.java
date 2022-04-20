package enigma;

import java.util.HashMap;

/**
 * Represents a permutation of a range of integers starting at 0 corresponding
 * to the characters of an alphabet.
 *
 * @author Aishik Bhattacharyya
 */
class Permutation {

    /**
     * Set this Permutation to that specified by CYCLES, a string in the
     * form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     * is interpreted as a permutation in cycle notation.  Characters in the
     * alphabet that are not included in any cycle map to themselves.
     * Whitespace is ignored.
     */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _forward = new HashMap<>();
        _backward = new HashMap<>();
        String currGroup = "";
        int count = 0;
        for (int i = 0; i < cycles.length(); i++) {
            if (cycles.charAt(i) == '(') {
                count++;
            } else if (cycles.charAt(i) == ')') {
                count--;
            }
        }
        if (count != 0) {
            throw new EnigmaException("Invalid permuation cycle");
        }
        for (int i = 0; i < cycles.length(); i++) {
            char curr = cycles.charAt(i);
            if (curr == '(') {
                currGroup = "";
            } else if (curr != ')' && curr != ' ') {
                currGroup += curr;
            } else if (curr == ')') {
                addCycle(currGroup);
            }
        }
    }

    /**
     * Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     * c0c1...cm.
     */
    private void addCycle(String cycle) {
        StringBuilder sb = new StringBuilder(cycle);
        String reversed = sb.reverse().toString();
        for (int i = 0; i < cycle.length() - 1; i++) {
            _forward.put(cycle.charAt(i), cycle.charAt(i + 1));
            _backward.put(reversed.charAt(i), reversed.charAt(i + 1));
        }
        _forward.put(cycle.charAt(cycle.length() - 1), cycle.charAt(0));
        _backward.put(reversed.charAt(cycle.length() - 1), reversed.charAt(0));
    }

    /**
     * Return the value of P modulo the size of this permutation.
     */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /**
     * Returns the size of the alphabet I permute.
     */
    int size() {
        return _alphabet.size();
    }

    /**
     * Return the result of applying this permutation to P modulo the
     * alphabet size.
     */
    int permute(int p) {
        int modded = ((p % _alphabet.size())
                + _alphabet.size()) % _alphabet.size();
        char val = _alphabet.toChar(modded);
        if (!_forward.containsKey(val)) {
            return modded;
        }
        char next = _forward.get(val);
        return _alphabet.toInt(next);
    }

    /**
     * Return the result of applying the inverse of this permutation
     * to  C modulo the alphabet size.
     */
    int invert(int c) {
        int modded = ((c % _alphabet.size())
                + _alphabet.size()) % _alphabet.size();
        char val = _alphabet.toChar(modded);
        if (!_backward.containsKey(val)) {
            return modded;
        }
        char next = _backward.get(val);
        return _alphabet.toInt(next);
    }

    /**
     * Return the result of applying this permutation
     * to the index of P in ALPHABET, and converting
     * the result to a character of ALPHABET.
     */
    char permute(char p) {
        if (!_forward.containsKey(p)) {
            return p;
        }
        return _forward.get(p);
    }

    /**
     * Return the result of applying the inverse of this permutation to C.
     */
    char invert(char c) {
        if (!_backward.containsKey(c)) {
            return c;
        }
        return _backward.get(c);
    }

    /**
     * Return the alphabet used to initialize this Permutation.
     */
    Alphabet alphabet() {
        return _alphabet;
    }

    void setAlphabet(Alphabet a) {
        _alphabet = a;
    }

    /**
     * Return true iff this permutation is a derangement (i.e., a
     * permutation for which no value maps to itself).
     */
    boolean derangement() {
        for (char c : _forward.keySet()) {
            if (_forward.get(c) == c) {
                return false;
            }
        }

        for (char c : _backward.keySet()) {
            if (_backward.get(c) == c) {
                return false;
            }
        }

        return _forward.keySet().size() == _alphabet.size();
    }

    /**
     * Alphabet of this permutation.
     */
    private Alphabet _alphabet;

    /**
     * Keeps track of all forward permutations.
     */
    private HashMap<Character, Character> _forward;

    /**
     * Keeps track of all backward permutations.
     */
    private HashMap<Character, Character> _backward;
}
