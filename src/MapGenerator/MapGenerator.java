package MapGenerator;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 *
 * @author K1ta
 */
public class MapGenerator {

    private ArrayList<Room> rooms; //список комнат
    private ArrayList<Triangle> triangles; //список треугольников
    private ArrayList<Edge> edges; //список граней
    private ArrayList<Vector2> vertices; //список вершин
    private final int NumberOfRooms; //количество комнат для генерации
    private final int minSize; //минимальный размер комнаты
    private final int maxSize; //максимальный размер комнаты
    private boolean[][] map; //массив карты подземелья

    /**
     *
     * @param NumberOfRooms количество генерируемых комнат
     * @param minSize минимальный размер одной комнаты
     * @param maxSize максимальеый размер одной комнаты
     */
    public MapGenerator(int NumberOfRooms, int minSize, int maxSize) {
        rooms = new ArrayList<>();
        triangles = new ArrayList<>();
        edges = new ArrayList<>();
        vertices = new ArrayList<>();
        this.NumberOfRooms = NumberOfRooms;
        this.minSize = minSize;
        this.maxSize = maxSize;
    }

    /**
     * Генерирует необходимое количество комнат и добавляет их в список rooms.
     * Размер каждой комнаты по X и Y генерируется случайным образом от minSize
     * до maxSize. Позиции комнат генерируются в квадрате с центром в точке
     * (0,0) и стороной minSize * 2
     */
    private void generateRooms() {
        for (int i = 0; i < NumberOfRooms; i++) {
            //генерация размера
            int xSize = minSize + (int) (Math.random() * (maxSize - minSize + 1));
            int ySize = minSize + (int) (Math.random() * (maxSize - minSize + 1));
            //генерация координат
            double xPos = (int) (Math.random() * minSize + 1) * 2 - minSize;
            double yPos = (int) (Math.random() * minSize + 1) * 2 - minSize;
            if (xSize % 2 == 1) {
                xPos += 0.5;
            }
            if (ySize % 2 == 1) {
                yPos += 0.5;
            }
            //добавление сгенерированной комнаты в список комнат
            rooms.add(new Room(new Vector2(xPos, yPos), new Vector2(xSize, ySize)));
        }
    }

    /**
     * Функция разделения комнат. Использует два цикла foreach для определения
     * пересекающихся комнат. Если комнаты пересекаются, то для них
     * высчитывается смещение. После окончания циклов foreach каждая комната
     * сдвигается на свое смещение. После чего цикл запускается заново.
     */
    private void separateRooms() {
        boolean separated = false;
        while (!separated) {
            separated = true;
            for (Room room1 : rooms) {
                for (Room room2 : rooms) {
                    //если room1 пересекает room2, то вызывается функция computeSeparation()
                    if (room1.isOverlapping(room2) && rooms.indexOf(room1) != rooms.indexOf(room2)) {
                        computeSeparation(room1, room2);
                        separated = false;
                    }
                }
            }
            //в конце цикла каждая комната сдвигается на вектор offset
            for (Room room : rooms) {
                room.applyOffset();
            }
        }
    }

    /**
     * Рассчитывает смещение для комнат. Смещение каждой комнаты за одну
     * итерацию не должно превышать 1, иначе расстояние между комнатами будет
     * очень большим.
     *
     * @param room комната, для которой высчитывается смещение
     * @param cause комната, относительно которой высчитывается смещение
     */
    private void computeSeparation(Room room, Room cause) {
        //вектор от комнаты room до комнаты cause
        Vector2 dir = room.position.returnDifference(cause.position);
        //определяется, в какую сторону идет смещение комнаты room
        if (Math.abs(dir.x) >= Math.abs(dir.y)) {
            if (dir.x > 0) {
                room.addOffset(new Vector2(1, 0));
            } else if (dir.x < 0) {
                room.addOffset(new Vector2(-1, 0));
            }
        }
        if (Math.abs(dir.x) <= Math.abs(dir.y)) {
            if (dir.y > 0) {
                room.addOffset(new Vector2(0, 1));
            } else if (dir.y < 0) {
                room.addOffset(new Vector2(0, -1));
            }
        }
        //если комнаты в одной точке, то они расходятся в рандомные стороны
        if (dir.equals(new Vector2())) {
            int rand = (int) (Math.random() * 2) + 1;
            if (rand == 2) {
                rand = -1;
            } else {
                rand = 1;
            }
            //если индекс комнаты room меньше, то она смещается по X, иначе по Y
            if (rooms.indexOf(room) > rooms.indexOf(cause)) {
                room.addOffset(new Vector2(1 * rand, 0));
            } else {
                room.addOffset(new Vector2(0, 1 * rand));
            }
        }
    }

    /**
     * Создает триангуляцию Делоне, используя позиции комнат в качестве вершин.
     * Трангуляция Делоне - это планарный граф, все внутренние области которого
     * являются треугольниками и который удовлетворяет условию Делоне: внутрь
     * окружности, описанной вокруг любого построенного треугольника, не
     * попадает ни одна из заданных точек триангуляции. Триангуляция нужна для
     * создания проходов между комнатами. Условие Делоне нужно для того, чтобы
     * треугольники равномерно распределились по мешу и не было слишком узких
     * или вытяннутых треугольников. Алгоритм следующий: сначала строятся два
     * треугольника, образующие квадрат, так, чтобы все точки триангуляции
     * гарантированно попадали внутрь уже существующих треугольников. Затем для
     * каждой новой точки определяются треугольники, в окружность которых
     * попадает эта точка. Эти треугольники удаляются и на основе полученного
     * контура строятся новые треугольники.
     */
    private void createTriangulation() {
        //Сначала создаем суперпозицию, то есть создаем два треугольника, образующие
        //квадрат так, чтобы они захватывали все точки
        double top = 0; //максимальная позиция по Y
        double bot = 0; //минимальная позиция по Y
        double right = 0; //максимальная позиция по X
        double left = 0; //минимальная позиция по X
        for (Room room : rooms) {
            if (room.position.y > top) {
                top = room.position.y;
            }
            if (room.position.y < bot) {
                bot = room.position.y;
            }
            if (room.position.x > right) {
                right = room.position.x;
            }
            if (room.position.x < left) {
                left = room.position.x;
            }
        }
        top++;
        bot--;
        right++;
        left--;
        //Те самые два треугольника, образующие суперпозицию
        triangles.add(new Triangle(new Vector2(right, bot), new Vector2(left, bot), new Vector2(left, top)));
        triangles.add(new Triangle(new Vector2(left, top), new Vector2(right, top), new Vector2(right, bot)));
        for (int i = 0; i < NumberOfRooms; i++) {
            Vector2 point = rooms.get(i).position;
            //список треуголььников, для которых после добавления новой точки перестало
            //выполняться условие Делоне. Они подлежат удалению
            ArrayList<Triangle> selected = new ArrayList<>();
            //список треуголников, которые остаются нетронутыми
            ArrayList<Triangle> usual = new ArrayList<>();
            for (Triangle t : triangles) {
                //если точка попадает в описанную окружность треугольника, то она
                //попадает в selected, иначе в usual
                if (t.checkPoint(point)) {
                    selected.add(t);
                } else {
                    usual.add(t);
                }
            }
            //Создаем контур из точек, которые входили в удаленные треугольники.
            //То есть у нас образовалась дырка в триангуляции, и этот список - точки,
            //образующие эту дыру.
            ArrayList<Vector2> points = new ArrayList<>();
            if (selected.size() > 0) {
                points.add(selected.get(0).p1);
                points.add(selected.get(0).p2);
                points.add(selected.get(0).p3);
                selected.remove(0);
                //список для очереди еще не просмотренных треугоьников
                ArrayList<Triangle> queue = new ArrayList<>();
                do {
                    queue.clear();
                    //Так как точки идут вразнобой, нам нужно отсортировать их по часовой стрелке
                    for (int j = 0; j < selected.size(); j++) {
                        //для каждого добавляющегося треуголника мы смотрим, содержится ли любая пара
                        //его точек в списке. Если содержится, то не входящую в список точку мы вставляем
                        //между этими двумя.
                        if (points.contains(selected.get(j).p1) && points.contains(selected.get(j).p2)) {
                            int ind1 = points.indexOf(selected.get(j).p1);
                            int ind2 = points.indexOf(selected.get(j).p2);
                            if (ind1 - ind2 == 1) {
                                points.add(ind2 + 1, selected.get(j).p3);
                            } else if (ind2 - ind1 == 1) {
                                points.add(ind1 + 1, selected.get(j).p3);
                            } else {
                                points.add(points.size(), selected.get(j).p3);
                            }
                        } else if (points.contains(selected.get(j).p2) && points.contains(selected.get(j).p3)) {
                            int ind1 = points.indexOf(selected.get(j).p2);
                            int ind2 = points.indexOf(selected.get(j).p3);
                            if (ind1 - ind2 == 1) {
                                points.add(ind2 + 1, selected.get(j).p1);
                            } else if (ind2 - ind1 == 1) {
                                points.add(ind1 + 1, selected.get(j).p1);
                            } else {
                                points.add(points.size(), selected.get(j).p1);
                            }
                        } else if (points.contains(selected.get(j).p1) && points.contains(selected.get(j).p3)) {
                            int ind1 = points.indexOf(selected.get(j).p1);
                            int ind2 = points.indexOf(selected.get(j).p3);
                            if (ind1 - ind2 == 1) {
                                points.add(ind2 + 1, selected.get(j).p2);
                            } else if (ind2 - ind1 == 1) {
                                points.add(ind1 + 1, selected.get(j).p2);
                            } else {
                                points.add(points.size(), selected.get(j).p2);
                            }
                            //если точки треуголника не входят в этот список, он отправляется в очередь
                        } else {
                            queue.add(selected.get(j));
                        }
                    }
                    if (!queue.isEmpty()) {
                        selected.clear();
                        selected.addAll(queue);
                    }
                } while (queue.size() > 0);
                //создание новых треугольников, содержащих 2 точки контура и новую точку
                for (int j = 0; j < points.size() - 1; j++) {
                    usual.add(new Triangle(points.get(j), points.get(j + 1), point));
                }
                usual.add(new Triangle(points.get(points.size() - 1), points.get(0), point));
                triangles = usual;
            }
        }
        //Добавляем все ребра треуголников в список ребер, но только не те, которые
        //входят в суперпозцию
        for (Triangle t : triangles) {
            Edge e = new Edge(t.p1, t.p2);
            if (!e.contains(new Vector2(left, bot)) && !e.contains(new Vector2(left, top)) && !e.contains(new Vector2(right, top)) && !e.contains(new Vector2(right, bot))) {
                if (!edges.contains(e)) {
                    edges.add(e);
                }
            }
            e = new Edge(t.p2, t.p3);
            if (!e.contains(new Vector2(left, bot)) && !e.contains(new Vector2(left, top)) && !e.contains(new Vector2(right, top)) && !e.contains(new Vector2(right, bot))) {
                if (!edges.contains(e)) {
                    edges.add(e);
                }
            }
            e = new Edge(t.p1, t.p3);
            if (!e.contains(new Vector2(left, bot)) && !e.contains(new Vector2(left, top)) && !e.contains(new Vector2(right, top)) && !e.contains(new Vector2(right, bot))) {
                if (!edges.contains(e)) {
                    edges.add(e);
                }
            }
        }
    }

    /**
     * Создает минимальное остовное дерево на основе триангуляции Делоне. У нас
     * есть список треугольников triangles, но нам нужны только их ребра, так
     * что вносим все ребра в спсок edges. Список вершин: vertices. Также нам
     * нужно еще три списка вершин: уже добавленные в минимальный остовный граф
     * (path), еще не добавленные (active) и промежуточные (active_buffer), так
     * как в цикле foreach мы не можем удалть элементы списка. Затем выбираем
     * ребро с наименьшей стоимостью. После чего пока не закончатся
     * несоединенные точки находи такое ребро с минимальной стоимостью, чтобы
     * один его конец уже был частью пути, а второй еще был частью недобавленных
     * вершин.
     */
    private void generateCorridors() {
        if (edges.isEmpty()) {
            return;
        }
        //определяем грань с наименьшим весом
        Edge first = edges.get(0);
        for (Edge e : edges) {
            if (e.cost < first.cost) {
                first = e;
            }
        }
        for (Room r : rooms) {
            vertices.add(r.position);
        }
        //список с ребрами, составляющими путь (минимальное остовное дерево)
        ArrayList<Edge> path = new ArrayList<>();
        //список ребер, которые еще не рассматривались
        ArrayList<Edge> active = edges;
        //список, служащий буфером для еще не рассмотренных ребер
        ArrayList<Edge> active_buffer = new ArrayList<>();
        path.add(first);
        vertices.remove(first.p1);
        vertices.remove(first.p2);
        active.remove(first);
        //пока не закончатся активные точки, мы просматриваем все активные ребра и
        //выбираем одно самое минимальное, которое одной точкой уже входит в путь, 
        //а другой входит в активные точки
        while (vertices.size() > 0) {
            //ребро с минимальным весом из всех активных ребер
            Edge edge = new Edge();
            edge.cost = Double.MAX_VALUE;
            for (Edge e : active) {
                int k = 0;
                for (Vector2 v : vertices) {
                    //смотреим, сколько не добавленных в граф точек включает ребро
                    if (e.contains(v)) {
                        k++;
                    }
                }
                //если одну, то такое ребро нам подходит
                if (k == 1) {
                    //смотрим, минимальна ли его длина (вес)
                    if (e.cost < edge.cost) {
                        //если предыдущее минимальное ребро входило в список активных ребер,
                        //то возвращаем его туда через буфер активных реьер
                        if (edge.p1 != edge.p2) {
                            active_buffer.add(edge);
                        }
                        //и кладем на его место новое минимальное ребро
                        edge = e;
                    } else {
                        active_buffer.add(e);
                    }
                    //если ребро все еще не соприкасается с путем, то кладем его в буфер
                } else if (k == 2) {
                    active_buffer.add(e);
                }
            }
            //очищаем список и заменяем его списком буфера. Теперь в списке остались все ребра,
            //кроме одного, выбранного нами в цикле (с минимальным весом)
            active.clear();
            active.addAll(active_buffer);
            active_buffer.clear();
            path.add(edge);
            if (vertices.contains(edge.p1)) {
                vertices.remove(edge.p1);
            }
            if (vertices.contains(edge.p2)) {
                vertices.remove(edge.p2);
            }
        }
        //на выходе получаем список из ребер, которые образуют проходы между комнатами
        edges = path;
    }

    /**
     * Рассчитывает размер массива, создает его и инициализирует пробелами.
     * Также смещает комнаты в 4 координатную четверть и делает их координаты
     * положительными. Это нужно для того, чтобы избежать проблем с координатами
     * комнат в массиве.
     */
    private void createMap() {
        //максимальная позиция комнаты по оси Y
        int top = (int) (rooms.get(0).position.y + rooms.get(0).size.y / 2);
        //минимальная позиция комнаты по оси Y
        int bot = (int) (rooms.get(0).position.y - rooms.get(0).size.y / 2);
        //максимальная позиция комнаты по оси X
        int right = (int) (rooms.get(0).position.x + rooms.get(0).size.x / 2);
        //минимальная позиция комнаты по оси X
        int left = (int) (rooms.get(0).position.x - rooms.get(0).size.x / 2);
        //ищем границы будущего массива
        for (Room room : rooms) {
            if (room.position.x + room.size.x / 2 >= right) {
                right = (int) (room.position.x + room.size.x / 2) + 1;
            }
            if (room.position.x - room.size.x / 2 <= left) {
                left = (int) (room.position.x - room.size.x / 2) - 1;
            }
            if (room.position.y + room.size.y / 2 >= top) {
                top = (int) (room.position.y + room.size.y / 2) + 1;
            }
            if (room.position.y - room.size.y / 2 <= bot) {
                bot = (int) (room.position.y - room.size.y / 2) - 1;
            }
        }
        //считаем размер массива и инициализируем его пробелами
        int n = top - bot;
        int m = right - left;
        map = new boolean[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                map[i][j] = false;
            }
        }
        //смещаем все комнаты в 4 координатную четверть и берем их координаты по
        //модулю. Это позволяет избежать проблем с занесением в массив
        Vector2 oldCoords = new Vector2((right + left) / 2.0, (top + bot) / 2.0);
        Vector2 newCoords = new Vector2((Math.abs(right) + Math.abs(left)) / 2.0, -(Math.abs(top) + Math.abs(bot)) / 2.0);
        double xOffset = newCoords.x - oldCoords.x;
        double yOffset = newCoords.y - oldCoords.y;
        for (int i = 0; i < rooms.size(); i++) {
            rooms.get(i).position.plus(new Vector2(xOffset, yOffset));
            rooms.get(i).position.y = Math.abs(rooms.get(i).position.y);
        }
    }

    /**
     * Заполняет ячейки массива, соответствующие коридорам, нулями.
     */
    private void createCorridors() {
        //комнаты скорее всего не будут лежать на какой-либо оси координат. Для их соединения
        //находим вектор, показывающий направление от первой комнаты до второй и в зависимости
        //от его направления строим пересекающиеся прямые, параллельные одной из оси координат.
        //Цикл выполняется для каждого ребра
        for (Edge e : edges) {
            //Вектор, показывающий отношение между первой и второй точкой
            Vector2 dir = e.p1.returnDifference(e.p2);
            //Если первая точка лежит на оси X дальше второй
            if (dir.x > 0) {
                //Если первая точка лежит ниже второй
                if (dir.y > 0) {
                    for (int i = (int) e.p2.x; i < (int) e.p1.x; i++) {
                        map[(int) e.p1.y][i] = true;
                    }
                    for (int i = (int) e.p2.y; i < (int) e.p1.y; i++) {
                        map[i][(int) e.p2.x] = true;
                    }
                    //иначе
                } else {
                    for (int i = (int) e.p2.x; i < (int) e.p1.x; i++) {
                        map[(int) e.p1.y][i] = true;
                    }
                    for (int i = (int) e.p1.y; i < (int) e.p2.y; i++) {
                        map[i][(int) e.p2.x] = true;
                    }
                }
            } else //или если первая точка лежит ближе второй
            {
                if (dir.y > 0) { //и лежит ниже второй
                    for (int i = (int) e.p1.x; i < (int) e.p2.x; i++) {
                        map[(int) e.p1.y - 1][i] = true;
                    }
                    for (int i = (int) e.p2.y; i < (int) e.p1.y; i++) {
                        map[i][(int) e.p2.x] = true;
                    }
                } else { //иначе
                    for (int i = (int) e.p1.x; i < (int) e.p2.x; i++) {
                        map[(int) e.p1.y][i] = true;
                    }
                    for (int i = (int) e.p1.y; i < (int) e.p2.y; i++) {
                        map[i][(int) e.p2.x] = true;
                    }
                }
            }
        }
    }

    /**
     * Заполняет ячейки массива, соответствующие комнатам, нулями
     */
    private void createRooms() {
        for (Room room : rooms) {
            //находит координаты левого верхнего угла комнаты и заполняет массив нулями,
            //начиная с него.
            //координаты левого верхнего угла по X
            int xCorner = (int) (room.position.x - room.size.x / 2);
            //координаты левого верхнего угла по Y
            int yCorner = (int) (room.position.y - room.size.y / 2);
            for (int i = yCorner; i < yCorner + room.size.y; i++) {
                for (int j = xCorner; j < xCorner + room.size.x; j++) {
                    map[i][j] = true;
                }
            }
        }
    }

    public void generateMap() {
        generateRooms();
        separateRooms();
        createTriangulation();
        generateCorridors();
        createMap();
        createCorridors();
        createRooms();
    }

    public boolean[][] getMap() {
        return map;
    }

    /**
     * Печатает карту в консоль
     */
    public void PrintMap() {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                System.out.print((map[i][j] ? "0" : " ") + " ");
            }
            System.out.println("");
        }
    }

    /**
     * Печатает карту в файл с именем name
     *
     * @param name имя для файла
     */
    public void PrintMap(String name) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(name);
        } catch (FileNotFoundException ex) {
            System.out.println("Ошибка 4! Ошибка при создании файла");
            System.exit(4);
        }
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                pw.print((map[i][j] ? "0" : " ") + " ");
            }
            pw.println("");
        }
        pw.close();
    }
}
