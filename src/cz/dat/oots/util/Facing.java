package cz.dat.oots.util;

public enum Facing {

    Up("up", new Coord3D(0, 1, 0), 1, 5),
    Down("down", new Coord3D(0, -1, 0), 0, 4),
    South("south", new Coord3D(0, 0, 1), 3, 2),
    North("north", new Coord3D(0, 0, -1), 2, 0),
    East("east", new Coord3D(1, 0, 0), 5, 1),
    West("west", new Coord3D(-1, 0, 0), 4, 3);

    private final String name;
    private final Coord3D facingVector;
    private final int opposite;
    private final int index;

    Facing(String name, Coord3D facingVector, int opposite, int index) {
        this.name = name;
        this.facingVector = facingVector;
        this.opposite = opposite;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public Coord3D getFacingVector() {
        return facingVector;
    }

    public Facing getOpposite() {
        return this.values()[opposite];
    }

    public int getIndex() {
        return index;
    }

    public static Facing fromIndex(int index) {
        for (Facing facing : values()) {
            if (facing.getIndex() == index)
                return facing;
        }
        return Facing.Down;
    }
}
