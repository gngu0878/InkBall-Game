import static org.junit.Assert.*;
import org.junit.*;

public class CalculatorTest {
    private Calculator calc;
    private static final double EPS = 1e-9; // tolerance for floating-point comparisons

    @BeforeEach
    void setUp() {
        calc = new Calculator();
    }

    // 1) Addition of two positives
    @Test
    void testAddTwoPositives() {
        assertEquals(15.0, calc.add(10.0, 5.0), EPS);
    }

    @Test
    void testAddWithNegative() {
        assertEquals(7.0, calc.add(10.0, -3.0), EPS);
    }

    @Test
    void testSubtractResultNegative() {
        assertEquals(-5.0, calc.subtract(5.0, 10.0), EPS);
    }

    @Test
    void testMultiplyByZero() {
        assertEquals(0.0, calc.multiply(12345.678, 0.0), EPS);
    }

    @Test
    void testMultiplyWithNegative() {
        assertEquals(-24.0, calc.multiply(-4.0, 6.0), EPS);
    }

    @Test
    void testDivideExact() {
        assertEquals(2.0, calc.divide(10.0, 5.0), EPS);
    }

    @Test
    void testDivideFractional() {
        assertEquals(1.0 / 3.0, calc.divide(1.0, 3.0), EPS);
    }

    @Test
    void testDivideByZeroThrows() {
        assertThrows(ArithmeticException.class, () -> calc.divide(10.0, 0.0));
    }
}