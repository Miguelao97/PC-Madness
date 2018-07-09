public class TestingIdeas {

    public static void main(String[] args) {
        byte value = 1;

        System.out.println("Running");
        System.out.println(value & ((byte)1));

        System.out.println("Shutting");
        byte shutting = (byte) (value << 1);
        System.out.println(shutting );
        byte shutdown = (byte) (value << 2);
        System.out.println("Shutdown");
        System.out.println(shutdown);
    }
}
