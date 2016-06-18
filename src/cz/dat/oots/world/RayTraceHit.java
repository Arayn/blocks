package cz.dat.oots.world;

import cz.dat.oots.util.Facing;

public class RayTraceHit {
    float x;
    float y;
    float z;
    float hitDistance;
    HitType type;
    Facing side;
    String name = "";

    public RayTraceHit() {
        this.type = HitType.None;
    }

    public RayTraceHit(float x, float y, float z, float hitDistance, Facing side, HitType type, String name) {
        this(x, y, z, hitDistance, side, type);
        this.name = name;
    }


    public RayTraceHit(float x, float y, float z, float hitDistance, Facing side, HitType type) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.hitDistance = hitDistance;
        this.side = side;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Facing getSide() {
        return side;
    }

    public float getHitDistance() {
        return hitDistance;
    }

    public HitType getType() {
        return type;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    @Override
    public String toString() {
        return String.format("Hit=%s X: %s Y: %s Z:%s Side: %s Name: %s", type.toString(), x, y, z, side.getName(), name);
    }

    public enum HitType {
        None,
        Block,
        Entity
    }
}
