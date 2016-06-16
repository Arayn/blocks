package cz.dat.oots.world;

public class RayTraceHit {
    float x;
    float y;
    float z;
    float hitDistance;
    HitType type;

    public RayTraceHit() {
        this.type = HitType.None;
    }

    public RayTraceHit(float x, float y, float z, float hitDistance, HitType type) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.hitDistance = hitDistance;
        this.type = type;
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
        return String.format("Hit=%s X: %s Y: %s Z:%s Dist: %s", type.toString(), x, y, z, hitDistance);
    }

    public enum HitType {
        None,
        Block,
        Entity
    }
}
