package MapGenerator;

/**
 *
 * @author K1ta
 */
public class Triangle {

    public Vector2 p1; //первая вершина 
    public Vector2 p2; //вторая вершина
    public Vector2 p3; //третья верщина

    public Triangle() {
        p1 = new Vector2();
        p2 = new Vector2();
        p3 = new Vector2();
    }

    public Triangle(Vector2 p1, Vector2 p2, Vector2 p3) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
    }

    @Override
    public String toString() {
        return "{" + p1 + "," + p2 + "," + p3 + "}";
    }

    /**
     * Проверяет, входит ли точка point в описанную окружность треугольника this
     * путем проверки суммы противолежащих углов.
     *
     * @param point точка, для которой проверяется соответствие условию Делоне
     * @return true если точка попадает в описанную окружность; false если нет
     */
    public boolean checkPoint(Vector2 point) {
        double s = ((point.x - p1.x) * (point.y - p3.y) - (point.x - p3.x) * (point.y - p1.y)) * ((p2.x - p3.x) * (p2.x - p1.x) + (p2.y - p3.y) * (p2.y - p1.y)) + ((point.x - p1.x) * (point.x - p3.x) + (point.y - p1.y) * (point.y - p3.y)) * ((p2.x - p3.x) * (p2.y - p1.y) - (p2.x - p1.x) * (p2.y - p3.y));
        return s >= 0;
    }
}
