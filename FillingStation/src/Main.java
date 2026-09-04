public class Main {
    public static void main(String[] args) {
        System.out.println("Система расчета стоимости топлива");

        int fuelType = 95;
        int amount = 100;
        int maxAmount = 400;

        double fuel92price = 60.2;
        double fuel95price = 67.33;

        double fuelPrice = 0;
        if (fuelType == 92) {
            fuelPrice = fuel92price;
        }
        else if (fuelType == 95) {
            fuelPrice = fuel95price;
        } else {
            System.out.println("Указан неверный тип топлива");
        }

        if (amount < 1) {
            System.out.println("Указано слишком малое количество топлива");
            amount = 0;
        }

        if (amount > maxAmount) {
            System.out.println("Указанное количество топлива превышает максимально допустимое");
            amount = maxAmount;
        }

        System.out.println("Цена выбранного топлива: " + fuelPrice + " руб.");

        double totalPrice = fuelPrice * amount;
        System.out.println("Общая стоимость заправки: " + totalPrice + " руб.");
    }
}