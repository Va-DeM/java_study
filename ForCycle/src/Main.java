import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        System.out.println("Введите число:");
        int value = new Scanner(System.in).nextInt();

        for (int i = 0; i <= value; i++) {
            for (int j = value; j >= 0; j--) {
                if (i*j == value) {
                    System.out.println(i + "*" + j);
                }
            }
        }
    }
}