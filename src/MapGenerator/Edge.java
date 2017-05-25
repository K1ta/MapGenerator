package MapGenerator;

/**
 *
 * @author K1ta
 */
public class Edge {

    public Vector2 p1; //первая вершина ребра 
    public Vector2 p2; //вторая вершина ребра
    public double cost; //длина ребра

    public Edge() {
        p1 = new Vector2();
        p2 = new Vector2();
        cost = 0;
    }

    public Edge(Vector2 p1, Vector2 p2) {
        this.p1 = p1;
        this.p2 = p2;
        cost = p1.distance(p2);
    }

    /**
     * Проверяет, является ли одна из верщин точкой v
     *
     * @param v точка, с которой проверяется совпадение верщин
     * @return true если одна из верщин равна точке v, иначе false
     */
    public boolean contains(Vector2 v) {
        return p1.equals(v) || p2.equals(v);
    }

    /**
     * Проверяет совпадение вершин ребра this с объектом o
     *
     * @param o объект
     * @return true если верщины ребер совпадают, иначе false
     */
    @Override
    public boolean equals(Object o) {
        if (this.getClass() != o.getClass()) {
            return false;
        }
        Edge e = (Edge) o;
        return ((p1.equals(e.p1) && p2.equals(e.p2)) || (p2.equals(e.p1) && p1.equals(e.p2)));
    }

    /**
     * @return строка с координатами верщин ребра
     */
    @Override
    public String toString() {
        return "Edge: (" + p1 + " ; " + p2 + ")";
    }
}
