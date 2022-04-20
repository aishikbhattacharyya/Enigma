package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.ArrayList;

import ucb.util.CommandArgs;

import static enigma.EnigmaException.*;

/**
 * Enigma simulator.
 *
 * @author Aishik Bhattacharyya
 */
public final class Main {

    /**
     * Process a sequence of encryptions and decryptions, as
     * specified by ARGS, where 1 <= ARGS.length <= 3.
     * ARGS[0] is the name of a configuration file.
     * ARGS[1] is optional; when present, it names an input file
     * containing messages.  Otherwise, input comes from the standard
     * input.  ARGS[2] is optional; when present, it names an output
     * file for processed messages.  Otherwise, output goes to the
     * standard output. Exits normally if there are no errors in the input;
     * otherwise with code 1.
     */
    public static void main(String... args) {
        try {
            CommandArgs options =
                    new CommandArgs("--verbose --=(.*){1,3}", args);
            if (!options.ok()) {
                throw error("Usage: java enigma.Main [--verbose] "
                        + "[INPUT [OUTPUT]]");
            }

            _verbose = options.contains("--verbose");
            new Main(options.get("--")).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /**
     * Open the necessary files for non-option arguments ARGS (see comment
     * on main).
     */
    Main(List<String> args) {
        lastUsed = false;
        lastStr = "";
        _config = getInput(args.get(0));

        if (args.size() > 1) {
            _input = getInput(args.get(1));
        } else {
            _input = new Scanner(System.in);
        }

        if (args.size() > 2) {
            _output = getOutput(args.get(2));
        } else {
            _output = System.out;
        }
    }

    /**
     * Return a Scanner reading from the file named NAME.
     */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /**
     * Return a PrintStream writing to the file named NAME.
     */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /**
     * Configure an Enigma machine from the contents of configuration
     * file _config and apply it to the messages in _input, sending the
     * results to _output.
     */
    private void process() {
        try {
            Machine m = readConfig();
            int count = 0;
            while (_input.hasNextLine()) {
                String s = _input.nextLine().strip();
                if (s.length() == 0) {
                    _output.println();
                } else if (s.charAt(0) == '*') {
                    int parenthesis = s.length();
                    if (s.indexOf('(') != -1) {
                        parenthesis = s.indexOf('(');
                    }
                    String[] split = s.substring(0, parenthesis).split("\\s");
                    String[] rotors = new String[m.numRotors()];
                    for (int i = 1; i < rotors.length + 1; i++) {
                        rotors[i - 1] = split[i];
                    }
                    m.insertRotors(rotors);

                    String settings = split[m.numRotors() + 1];
                    if (settings.length() != m.numRotors() - 1) {
                        throw new EnigmaException("Incorrect settings input.");
                    }
                    setUp(m, settings);

                    try {
                        String temp = split[m.numRotors() + 2];
                        m.setRings(temp);
                    } catch (Exception e) {
                        String ignored = "";
                    }


                    if (s.indexOf('(') != -1) {
                        String plugboardPerm = s.substring(s.indexOf('('));
                        m.setPlugboard(new
                                Permutation(plugboardPerm, _alphabet));
                    } else {
                        m.setPlugboard(new Permutation("", _alphabet));
                    }
                } else {
                    String encrypted = m.convert(s);
                    printMessageLine(encrypted);
                }
                count++;
            }
            _output.println();
            _output.flush();
        } catch (Exception e) {
            throw new EnigmaException("Error in processing input or output");
        }
    }

    /**
     * Return an Enigma machine configured from the contents of configuration
     * file _config.
     */
    private Machine readConfig() {
        try {
            int numRotors = 0;
            int pawls = 0;
            Collection<Rotor> allRotors = new ArrayList<>();
            int index = 0;
            while (_config.hasNextLine() | lastUsed) {
                if (index == 0) {
                    String s = _config.nextLine().strip();
                    if (s.contains(" ")) {
                        throw new EnigmaException("Invalid alphabet");
                    }
                    _alphabet = new Alphabet(s);
                } else if (index == 1) {
                    String s = _config.nextLine().strip();
                    String[] nums = s.split(" ");
                    try {
                        numRotors = Integer.parseInt(nums[0]);
                        pawls = Integer.parseInt(nums[1]);
                    } catch (Exception e) {
                        throw new EnigmaException(
                                "Invalid num rotors or num pawls");
                    }
                } else if (index >= 2) {
                    Rotor rotor = readRotor();
                    if (rotor != null) {
                        allRotors.add(rotor);
                    }
                }

                index += 1;
            }
            return new Machine(_alphabet, numRotors, pawls, allRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /**
     * Return a rotor, reading its description from _config.
     */
    private Rotor readRotor() {
        try {
            String s;
            if (lastUsed) {
                s = lastStr;
                lastUsed = false;
            } else {
                s = _config.nextLine().strip();
            }
            while (!lastUsed) {
                if (_config.hasNext()) {
                    String next = _config.nextLine().strip();
                    if (next.charAt(0) == '(') {
                        s += next;
                    } else {
                        lastUsed = true;
                        lastStr = next;
                    }
                } else {
                    break;
                }
            }
            if (s.length() == 0) {
                return null;
            }
            int opening = s.indexOf("(");
            String infoString = s.substring(0, opening);
            String[] info = infoString.split(" ");
            String name = info[0];
            String type = info[1].substring(0, 1);
            Permutation perm = new Permutation(s.substring(opening), _alphabet);

            if (type.equals("M")) {
                MovingRotor m = new
                        MovingRotor(name, perm, info[1].substring(1));
                return m;
            } else if (type.equals("N")) {
                FixedRotor f = new FixedRotor(name, perm);
                return f;
            } else if (type.equals("R")) {
                Reflector r = new Reflector(name, perm);
                return r;
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
        return null;
    }

    /**
     * Set M according to the specification given on SETTINGS,
     * which must have the format specified in the assignment.
     */
    private void setUp(Machine M, String settings) {
        M.setRotors(settings);
    }

    /**
     * Return true iff verbose option specified.
     */
    static boolean verbose() {
        return _verbose;
    }

    /**
     * Print MSG in groups of five (except that the last group may
     * have fewer letters).
     */
    private void printMessageLine(String msg) {
        msg = msg.replaceAll("\\s", "");
        String output = "";
        for (int i = 0; i < msg.length(); i += 5) {
            for (int j = i; j < i + 5 && j < msg.length(); j++) {
                output += msg.substring(j, j + 1);
            }
            output += " ";
        }
        _output.println(output.substring(0, output.length() - 1));
    }

    /**
     * Alphabet used in this machine.
     */
    private Alphabet _alphabet;

    /**
     * Source of input messages.
     */
    private Scanner _input;

    /**
     * Source of machine configuration.
     */
    private Scanner _config;

    /**
     * File for encoded/decoded messages.
     */
    private PrintStream _output;

    /**
     * True if --verbose specified.
     */
    private static boolean _verbose;

    /**
     * Last string read by the scanner.
     */
    private String lastStr;

    /**
     * If last string has been processed.
     */
    private boolean lastUsed;
}
