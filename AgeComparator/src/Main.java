public class Main {
    public static void main(String[] args) {

        int vasyaAge = 14;
        int katyaAge = 35;
        int mishaAge = 45;

        int min = -1; // минимальный возраст
        int middle = -1; // средний возраст
        int max = -1; // максимальный возраст

        if (vasyaAge <= katyaAge && vasyaAge <= mishaAge) {
            min = vasyaAge;
            if (katyaAge <= mishaAge) {
                middle = katyaAge;
                max = mishaAge;
            } else {
                middle = mishaAge;
                max = katyaAge;
            }
        } else if (katyaAge <= vasyaAge && katyaAge <= mishaAge) {
            min = katyaAge;
            if (vasyaAge <= mishaAge) {
                middle = vasyaAge;
                max = mishaAge;
            } else {
                middle = mishaAge;
                max = vasyaAge;
            }
        } else {
            min = mishaAge;
            if (vasyaAge <= katyaAge) {
                middle = vasyaAge;
                max = katyaAge;
            } else {
                middle = katyaAge;
                max = vasyaAge;
            }
        }

        String warning = "Значение должно быть в диапазоне от 0 до 120";
        if (min < 0 || min > 120) {
            System.out.println(warning);
        } else {
            System.out.println("Minimal age: " + min);
        }

        if (middle < 0 || middle > 120) {
            System.out.println(warning);
        } else {
            System.out.println("Middle age: " + middle);
        }

        if (max < 0 || max > 120) {
            System.out.println(warning);
        } else {
            System.out.println("Maximal age: " + max);
        }
    }
}