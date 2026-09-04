import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        System.out.println("Угадайте загаданное число(от 0 до 100)");
        int value = new Random().nextInt(100);

        while (true) {
            System.out.println("Введите число:");
            int attempt = new Scanner(System.in).nextInt();
            if (attempt < 0 || attempt > 100) {
                System.out.println("Число должно быть от 0 до 100!");
                continue;
            }

            if (attempt > value) {
                System.out.println("Загаданное число меньше");
            } else if (attempt < value) {
                System.out.println("Загаданное число больше");
            } else {
                System.out.println("Вы угадали!");
                break;
            }
        }
    }
}