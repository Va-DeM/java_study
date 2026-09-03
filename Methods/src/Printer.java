public class Printer {
    private String queue = "";
    private int pagesInQueue;
    private int pagesTotalPrint;

    public void append(String text) {
        append(text, "Документ без имени", 1);
    }

    public void append(String text, String name) {
        append(text, name,1);
    }

    public void append(String text, int pagesCount) {
        append(text, "Документ без имени", pagesCount);
    }

    public void append(String text, String name, int pagesCount) {
        this.queue = queue + "\n" + name + " - " + text;
        this.pagesInQueue = this.pagesInQueue + pagesCount;
    }

    public void clear() {
        this.queue = "";
        this.pagesInQueue = 0;
    }

    public void print() {
        System.out.println(this.queue);
        this.pagesTotalPrint = this.pagesTotalPrint + this.pagesInQueue;
        clear();
    }

    public int totalPrintPages() {
        return this.pagesTotalPrint;
    }

    public int getPendingPagesCount() {
        return this.pagesInQueue;
    }

}
