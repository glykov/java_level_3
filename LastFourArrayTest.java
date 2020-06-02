import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class LastFourArrayTest {
    LastFourArray lfa;

    @BeforeEach
    public void init() {
         lfa = new LastFourArray();
    }

    @Test
    public void test01() {
        Assertions.assertArrayEquals(new int[]{1, 7}, lfa.lastFourArray(new int[]{1, 2, 4, 4, 2, 3, 4, 1, 7}));
    }

    @Test
    public void test02() {
        Assertions.assertArrayEquals(new int[]{}, lfa.lastFourArray(new int[]{1, 2, 4, 4, 2, 3, 4, 1, 7, 4}));
    }

    @Test
    public void test03() {
        Assertions.assertArrayEquals(new int[]{1, 2, 2, 3, 1, 7}, lfa.lastFourArray(new int[]{4, 1, 2, 2, 3, 1, 7}));
    }

    @Test
    public void exceptionTesting() {
        Exception e = Assertions.assertThrows(RuntimeException.class, new Executable() {
            public void execute() {
                lfa.lastFourArray(new int[]{1, 2, 3, 5});
            }});
        Assertions.assertEquals("Number 4 is not found", e.getMessage());
    }
}
