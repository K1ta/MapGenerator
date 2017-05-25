package MapGenerator;

/**
 *
 * @author K1ta
 */
public class Main {

    public static void main(String[] args) {
        MapGenerator mg = new MapGenerator(20, 5, 15);
        mg.generateMap();
        mg.PrintMap();
        mg.PrintMap("output.txt");
    }
}
