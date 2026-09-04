public class Main {
    public static void main(String[] args) {

        int coffeeAmount = 2330;
        int milkAmount = 3210;
        int skimmedMilkAmount = 1290;

        int cappucinoMilkRequired = 60;
        int cappucinoCoffeeRequired = 15;

        if (skimmedMilkAmount >= cappucinoMilkRequired ||
            milkAmount >= cappucinoMilkRequired) {
            System.out.println("Молока достаточно");
        } else {
            System.out.println("Молока недостаточно");
        }

//        System.out.println("Hello world!");
    }
}