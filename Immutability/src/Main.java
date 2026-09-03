public class Main {
    public static void main(String[] args) {

        Book book = new Book(
                "Война и мир",
                "Л.Н.Толстой",
                1274,
                "978-0-1916-1254-1"
        );
        System.out.println("Название: " + book.getName());
        System.out.println("Автор: " + book.getAuthor());

        System.out.println("Кол-во страниц: " + book.getPages());
        System.out.println("ISNB номер: " + book.getISNB());
    }
}