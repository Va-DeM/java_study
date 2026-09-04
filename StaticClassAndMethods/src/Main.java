public class Main {
    public static void main(String[] args) {

        Product product = new Product("Торт Наполеон", 1000);
        Product product1 = new Product("Молоко", 85);
        System.out.println(Product.getCount());
        System.out.println(Product.getAveragePrice());


    }
}