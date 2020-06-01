import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OneFourArrayTest {
    OneFourArray ofa;
    @BeforeEach
    public void init() {
        ofa = new OneFourArray();
    }

    @Test
    public void testOk() {
        Assertions.assertEquals(true, ofa.check(new int[] {1, 1, 1, 4, 4, 1, 4, 4}));
    }

    @Test
    public void noOnes() {
        Assertions.assertEquals(false, ofa.check(new int[] {4, 4, 4, 4}));
    }

    @Test
    public void noFores() {
        Assertions.assertEquals(false, ofa.check(new int[] {1, 1, 1, 1, 1, 1}));
    }

    @Test
    public void otherNumbers() {
        Assertions.assertEquals(false, ofa.check(new int[] {1, 4, 4, 1, 1, 4, 3}));
    }
}
