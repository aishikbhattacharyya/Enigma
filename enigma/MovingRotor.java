package enigma;

import static enigma.EnigmaException.*;

/**
 * Class that represents a rotating rotor in the enigma machine.
 *
 * @author Aishik Bhattacharyya
 */
class MovingRotor extends Rotor {

    /**
     * A rotor named NAME whose permutation in its default setting is
     * PERM, and whose notches are at the positions indicated in NOTCHES.
     * The Rotor is initally in its 0 setting (first character of its
     * alphabet).
     */
    private String _notches;

    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        _notches = notches;
    }

    @Override
    void advance() {
        this.set(alphabet().toChar(alphabet().toInt(_pos) + 1));
    }

    @Override
    String notches() {
        return _notches;
    }

    @Override
    boolean rotates() {
        return true;
    }
}
