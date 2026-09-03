public class Car {
    private String model;
    private int price;
    private double discount;
    private boolean isAutomaticGearBox;

    public Car(String model, int price, boolean isAutomaticGearBox) {
        this.model = model;
        this.price = price;
        this.isAutomaticGearBox = isAutomaticGearBox;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double disscount) {
        this.discount = disscount;
    }

    public boolean isAutomaticGearBox() {
        return isAutomaticGearBox;
    }

    public void setAutomaticGearBox(boolean automaticGearBox) {
        isAutomaticGearBox = automaticGearBox;
    }
}
