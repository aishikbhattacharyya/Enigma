package enigma;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.HashMap;

/**
 * Class that represents a complete enigma machine.
 *
 * @author Aishik Bhattacharyya
 */
class Machine {

    /**
     * A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     * and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     * available rotors.
     */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        _allRotors = allRotors;
        _currRotors = new ArrayList<>();
    }

    /**
     * Return the number of rotor slots I have.
     */
    int numRotors() {
        return _numRotors;
    }

    /**
     * Return the number pawls (and thus rotating rotors) I have.
     */
    int numPawls() {
        return _pawls;
    }

    /**
     * Return Rotor #K, where Rotor #0 is the reflector, and Rotor
     * #(numRotors()-1) is the fast Rotor.  Modifying this Rotor has
     * undefined results.
     */
    Rotor getRotor(int k) {
        int index = 0;
        Rotor result = null;
        for (Rotor r : _currRotors) {
            if (index == k) {
                result = r;
                break;
            }
            index += 1;
        }
        return result;
    }

    Alphabet alphabet() {
        return _alphabet;
    }

    /**
     * Set my rotor slots to the rotors named ROTORS from my set of
     * available rotors (ROTORS[0] names the reflector).
     * Initially, all rotors are set at their 0 setting.
     */
    void insertRotors(String[] rotors) {
        int rotorNo = 0;
        int movRotors = 0;
        _currRotors = new ArrayList<>();
        List<String> rotorsList = Arrays.asList(rotors);
        HashMap<String, Integer> map = new HashMap<>();
        for (String r : rotors) {
            if (map.containsKey(r)) {
                throw new EnigmaException("Muliple rotors with same name.");
            }
            for (Rotor r2 : _allRotors) {
                if (r2.name().equals(r)) {
                    if (!r2.reflecting() && rotorNo == 0) {
                        throw new EnigmaException("First rotor not reflector");
                    }
                    map.put(r, 1);
                    if (r2.rotates()) {
                        movRotors++;
                    }
                    _currRotors.add(r2);
                    rotorNo++;
                }
            }
        }
        if (movRotors > _pawls) {
            throw new EnigmaException("Too many moving rotors.");
        }
    }

    /**
     * Set my rotors according to SETTING, which must be a string of
     * numRotors()-1 characters in my alphabet. The first letter refers
     * to the leftmost rotor setting (not counting the reflector).
     */
    void setRotors(String setting) {
        for (int i = 0; i < setting.length(); i++) {
            if (!_alphabet.contains(setting.charAt(i))) {
                throw new EnigmaException("Bad character in setting");
            }
            Rotor r = _currRotors.get(i + 1);
            r._pos = setting.charAt(i);
        }
    }

    /**
     * Return the current plugboard's permutation.
     */
    Permutation plugboard() {
        return _plugboard;
    }

    /**
     * Set the plugboard to PLUGBOARD.
     */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /**
     * Returns the result of converting the input character C (as an
     * index in the range 0..alphabet size - 1), after first advancing
     * the machine.
     */
    int convert(int c) {
        advanceRotors();
        if (Main.verbose()) {
            System.err.printf("[");
            for (int r = 1; r < numRotors(); r += 1) {
                System.err.printf("%c",
                        alphabet().toChar(getRotor(r).setting()));
            }
            System.err.printf("] %c -> ", alphabet().toChar(c));
        }
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(c));
        }
        c = applyRotors(c);
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c%n", alphabet().toChar(c));
        }
        return c;
    }

    /**
     * Advance all rotors to their next position.
     */
    private void advanceRotors() {
        ArrayList<Rotor> toMove = new ArrayList<>();
        toMove.add(_currRotors.get(_currRotors.size() - 1));

        for (int i = _currRotors.size() - 2;
             i > _currRotors.size() - _pawls - 1; i--) {
            Rotor next = _currRotors.get(i + 1);
            Rotor curr = _currRotors.get(i);

            if (next.atNotch()) {
                if (!toMove.contains(next)) {
                    toMove.add(next);

                }
                if (!toMove.contains(curr)) {
                    toMove.add(curr);
                }
            }
        }

        for (Rotor r : toMove) {
            r.advance();
        }
    }

    /**
     * Return the result of applying the rotors to the character C (as an
     * index in the range 0..alphabet size - 1).
     */
    private int applyRotors(int c) {
        for (int i = _currRotors.size() - 1; i > 0; i--) {
            Rotor curr = _currRotors.get(i);
            c = curr.convertForward(c);
        }

        c = _currRotors.get(0).permutation().permute(c);

        for (int i = 1; i < _currRotors.size(); i++) {
            Rotor curr = _currRotors.get(i);
            c = curr.convertBackward(c);
        }

        return c;
    }

    /**
     * Returns the encoding/decoding of MSG, updating the state of
     * the rotors accordingly.
     */
    String convert(String msg) {
        String result = "";
        for (int i = 0; i < msg.length(); i++) {
            char c1 = msg.charAt(i);
            if (_alphabet.contains(c1)) {
                int converted = convert(_alphabet.toInt(c1));
                char c2 = _alphabet.toChar(converted);
                result += c2;
            } else {
                result += c1;
            }
        }
        return result;
    }

    /**
     * Common alphabet of my rotors.
     */
    private final Alphabet _alphabet;

    /**
     * Number of rotors.
     */
    private final int _numRotors;

    /**
     * Number of pawls.
     */
    private final int _pawls;

    /**
     * Collection of all possible rotors.
     */
    private final Collection<Rotor> _allRotors;

    /**
     * ArrayList of current rotors in use.
     */
    private ArrayList<Rotor> _currRotors;

    /**
     * The alphabet in use.
     */
    private Permutation _plugboard;

    /**
     * Setting the current ring setting.
     * @param temp ring setting.
     */
    public void setRings(String temp) {
        for (int i = 1; i < _currRotors.size(); i++) {
            Rotor c = _currRotors.get(i);
            c.setRings(temp.charAt(i - 1));
        }
    }
}
