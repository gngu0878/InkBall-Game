public class Calculator {
    public double add(double a, double b) {
        return a + b;
    }

    // Subtraction
    public double subtract(double a, double b) {
        return a - b;
    }

    // Multiplication
    public double multiply(double a, double b) {
        return a * b;
    }

    // Division (with zero check)
    public double divide(double a, double b) {
        if (b == 0) {
            throw new ArithmeticException("Error: Division by zero is undefined.");
        }
        return a / b;
    }

    // Main method to test the calculator
    public static void main(String[] args) {
    }
}