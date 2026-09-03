public class Main {
    public static void main(String[] args) {
        ArithmeticCalculator calculator = new ArithmeticCalculator(25, 25);
        System.out.println("Сложение: " + calculator.calculate(Operation.ADD));
        System.out.println("Вычитание: " + calculator.calculate(Operation.SUBTRACT));
        System.out.println("Умножение: " + calculator.calculate(Operation.MULTIPLY));
    }
}