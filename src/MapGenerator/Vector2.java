package MapGenerator;

/**
 *
 * @author K1ta
 */
public class Vector2 {

    public double x; //координата по оси x
    public double y; //координата по оси y

    public Vector2() {
        x = 0.0;
        y = 0.0;
    }

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     *
     * @param o объект для сравнения
     * @return true если координаты this равны координатам o; иначе false
     */
    @Override
    public boolean equals(Object o) {
        if (this.getClass() != o.getClass()) {
            return false;
        }
        Vector2 v = (Vector2) o;
        return x == v.x && y == v.y;
    }

    @Override
    public String toString() {
        return "(" + x + ";" + y + ")";
    }

    /**
     *
     * @param v вектор, до которого считается расстояние
     * @return расстояние между векторами this и v
     */
    public double distance(Vector2 v) {
        return Math.sqrt((v.x - x) * (v.x - x) + (v.y - y) * (v.y - y));
    }

    /**
     *
     * @param v вектор для суммирования
     */
    public void plus(Vector2 v) {
        x += v.x;
        y += v.y;
    }

    /**
     *
     * @param v вектор для суммирования
     * @return this + v
     */
    public Vector2 returnSum(Vector2 v) {
        return new Vector2(x + v.x, y + v.y);
    }

    /**
     *
     * @param v вектор для вычитания
     */
    public void minus(Vector2 v) {
        x -= v.x;
        y -= v.y;
    }

    /**
     *
     * @param v вектор для отнимания
     * @return this - v
     */
    public Vector2 returnDifference(Vector2 v) {
        return new Vector2(x - v.x, y - v.y);
    }

    /**
     * Обнуление вектора
     */
    public void reset() {
        x = 0.0;
        y = 0.0;
    }
}
