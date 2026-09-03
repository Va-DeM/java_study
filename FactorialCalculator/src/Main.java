import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        System.out.println("Введите число от 1 до 12 для расчета факториала:");
        int value = new Scanner(System.in).nextInt();

        int factorial = 1;
        for (int i = 1; i <= value; i++) {
            factorial = factorial * i;
        }
        System.out.println(factorial);
    }
}