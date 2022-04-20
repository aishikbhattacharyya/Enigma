package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;

import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/**
 * The suite of all JUnit tests for the Permutation class.
 *
 * @author
 */
public class PermutationTest {

    /**
     * Testing time limit.
     */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    private Permutation perm;
    private String alpha = UPPER_STRING;

    /**
     * Check that perm has an alphabet whose size is that of
     * FROMALPHA and TOALPHA and that maps each character of
     * FROMALPHA to the corresponding character of FROMALPHA, and
     * vice-versa. TESTID is used in error messages.
     */
    private void checkPerm(String testId,
                           String fromAlpha, String toAlpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, perm.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                    e, perm.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                    c, perm.invert(e));
            int ci = alpha.indexOf(c), ei = alpha.indexOf(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                    ei, perm.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                    ci, perm.invert(ei));
        }
    }

    /* ***** TESTS ***** */
    @Test
    public void checkSize() {
        Permutation perm1 = new Permutation("(CAB) (XZY)", UPPER);
        Permutation perm2 = new Permutation("", UPPER);

        assertEquals(UPPER.size(), perm1.size());
        assertEquals(UPPER.size(), perm2.size());
    }

    @Test
    public void checkPermuteInt() {
        Permutation perm1 = new Permutation("(CAB) (XZY)", UPPER);

        assertEquals(4, perm1.permute(4));
        assertEquals(1, perm1.permute(26));
        assertEquals(24, perm1.permute(-1));
        assertEquals(6, perm1.permute(-20));
        assertEquals(1, perm1.permute(-26));
    }


    @Test
    public void checkPermuteChar() {
        Permutation perm1 = new Permutation("(CAB) (XZY)", UPPER);

        assertEquals('A', perm1.permute('C'));
        assertEquals('B', perm1.permute('A'));
        assertEquals('C', perm1.permute('B'));
        assertEquals('W', perm1.permute('W'));
        assertEquals('X', perm1.permute('Y'));
    }

    @Test
    public void checkInvertInt() {
        Permutation perm1 = new Permutation("(CAB) (XZY)", UPPER);

        assertEquals(4, perm1.invert(4));
        assertEquals(2, perm1.invert(0));
        assertEquals(23, perm1.invert(25));
        assertEquals(6, perm1.invert(-20));
        assertEquals(24, perm1.invert(-3));
    }

    @Test
    public void checkInvertChar() {
        Permutation perm1 = new Permutation("(CAB) (XZY)", UPPER);

        assertEquals('B', perm1.invert('C'));
        assertEquals('C', perm1.invert('A'));
        assertEquals('A', perm1.invert('B'));
        assertEquals('W', perm1.invert('W'));
        assertEquals('Y', perm1.invert('X'));
    }

    @Test
    public void checkDerangement() {
        Permutation perm1 = new Permutation("(CAB) (XZY)", UPPER);
        Permutation perm2 = new Permutation("(ABCDEF)"
                + " (GIHJK) (LMNOP) (TUV) (RSQ) (YXWZ)", UPPER);
        Permutation perm3 = new Permutation("(ABCDEF)"
                + " (GIHJK) (LMNOP) (TUV) (RSQ) (YXW)", UPPER);

        assertFalse(perm1.derangement());
        assertTrue(perm2.derangement());
        assertFalse(perm3.derangement());
    }

    @Test
    public void checkIdTransform() {
        perm = new Permutation("", UPPER);
        checkPerm("identity", UPPER_STRING, UPPER_STRING);
    }
}
