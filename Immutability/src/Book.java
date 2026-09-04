public class Book {
    private final String name;
    private final String author;
    private final int pages;
    private final String ISNB;

    public Book(String name, String author, int pages, String ISNB) {
        this.name = name;
        this.author = author;
        this.pages = pages;
        this.ISNB = ISNB;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public int getPages() {
        return pages;
    }

    public String getISNB() {
        return ISNB;
    }
}
