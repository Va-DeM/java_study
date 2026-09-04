public class Main {
    public static void main(String[] args) {

        Engine engine = new Engine(EngineType.DIESEL,3.5,256);
        Car car = new Car(engine, CarType.SUV, 2300);
        System.out.println("Тип автомобиля: " + car.getType());
        System.out.println("Масса двигателя: " + car.getWeight());
        System.out.println("Тип двигателя: " + car.getEngine().getType());
        System.out.println("Объём двигателя: " + car.getEngine().getVolume());
        System.out.println("Мощность двигателя: " + car.getEngine().getPower());
    }
}