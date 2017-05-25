package MapGenerator;

/**
 *
 * @author K1ta
 */
public class Room {

    public Vector2 position; // позиция комнаты
    public Vector2 size;     // размер комнаты
    public Vector2 offset;   // смещение

    public Room() {
        position = new Vector2();
        size = new Vector2();
        offset = new Vector2();
    }

    public Room(Vector2 position, Vector2 size) {
        this.position = position;
        this.size = size;
        offset = new Vector2();
    }

    @Override
    public String toString() {
        return "pos:" + position.toString() + " size:" + size.toString();
    }

    /**
     * Применяет смещение к позиции комнаты. Приводит вектор смещения к
     * единичному, чтобы расстояние между комнатами после разделения не было
     * большим. В конце обнуляет вектор смещения.
     */
    public void applyOffset() {
        if (offset.x > 0) {
            offset.x = 2;
        } else if (offset.x < 0) {
            offset.x = -2;
        }
        if (offset.y > 0) {
            offset.y = 2;
        } else if (offset.y < 0) {
            offset.y = -2;
        }
        //позиция комнаты смещается на вектор offset
        position.plus(offset);
        offset.reset();
    }

    /**
     * Прибавляет к текущему смещению новое.
     *
     * @param offset вектор смещения
     */
    public void addOffset(Vector2 offset) {
        this.offset.plus(offset);
    }

    /**
     * Проверка, пересекает ли комната this комнату room. Проверка идет от
     * обратного, то есть проверяется отсутствие пересечения
     *
     * @param room комната, пересечение с которой проверяется
     * @return false если не пересекает; true если пересекает
     */
    public boolean isOverlapping(Room room) {
        return !((position.y + (size.y / 2) + 2 < room.position.y - (room.size.y / 2))
                || (position.y - (size.y / 2) - 2 > room.position.y + (room.size.y / 2))
                || (position.x + (size.x / 2) + 2 < room.position.x - (room.size.x / 2))
                || (position.x - (size.x / 2) - 2 > room.position.x + (room.size.x / 2)));
    }
}
