public class Main {
    public static void main(String[] args) {
        int certCount = 1000;
        int winnersRate = 100;
        int[] certNumbers = new int[certCount];
        boolean[] certIsWin = new boolean[certCount];
        for(int i = 0; i < certNumbers.length; i++) {
            certNumbers[i] = 1000000 + (int) Math.round(8999999 * Math.random());
            certIsWin[i] = i % winnersRate == 0;
        }

        for (int i = 0; i < certIsWin.length; i++) {
            if (certIsWin[i] == true) {
                System.out.println(i + " : " + certIsWin[i]);
            }
        }

    }
}