package cz.dat.oots.movable.entity;

import cz.dat.oots.block.Block;
import cz.dat.oots.block.BlockFluid;
import cz.dat.oots.block.BlockPlant;
import cz.dat.oots.block.BlockSlab;
import cz.dat.oots.collisions.AABB;
import cz.dat.oots.inventory.BasicBlockStack;
import cz.dat.oots.inventory.IObjectStack;
import cz.dat.oots.overlay.BasicLifesOverlay;
import cz.dat.oots.render.IOverlayRenderer;
import cz.dat.oots.settings.Keyconfig;
import cz.dat.oots.sound.SoundManager;
import cz.dat.oots.util.Facing;
import cz.dat.oots.util.Vector3;
import cz.dat.oots.world.Explosion;
import cz.dat.oots.world.RayTraceHit;
import cz.dat.oots.world.World;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.opengl.TextureImpl;

import java.util.Random;

public class PlayerEntity extends Entity implements IOverlayRenderer {

    public static final float PLAYER_HEIGHT = 1.7f;
    public static final float EYES_HEIGHT = 1.6f;
    public static final float PLAYER_SIZE = 0.5f;
    public static final float STEP_TIMER_FULL = 2.25f;
    public static final float JUMP_STRENGTH = 0.4f;
    public static final float MAX_WALK_SPEED = 0.25f;
    public static final int REGENERATION_TICKS = 20;


    private IObjectStack inHand;
    private BasicLifesOverlay lifesOverlay;

    private int lookingAtX;
    private int lookingAtY;
    private int lookingAtZ;

    private int placesAtX;
    private int placesAtY;
    private int placesAtZ;

    private boolean hasSelected = false;

    private float heading = 140.0F;
    private float tilt = -60.0F;

    // public float radYaw = 0f;
    //public float radPitch = 0f;
    private RayTraceHit rayTraceHit;
    private boolean onGround = false;
    private boolean wasOnGround = false;
    private float speed = 0;
    private float speedStrafe = 0;
    private Random rand = new Random();
    private float spf;
    private float stepTimer = PlayerEntity.STEP_TIMER_FULL;
    private float fallVelocity;
    private Block standingOn = null;
    private int regenerationTimer = 0;

    public PlayerEntity(World world, float x, float y, float z) {
        super(world, x, y, z);
        this.setHeldItemID(1);
        this.bb = new AABB(this.posX - PlayerEntity.PLAYER_SIZE / 2, this.posY,
                this.posZ - PlayerEntity.PLAYER_SIZE / 2, this.posX
                + PlayerEntity.PLAYER_SIZE / 2, this.posY
                + PlayerEntity.PLAYER_HEIGHT, this.posZ
                + PlayerEntity.PLAYER_SIZE / 2);
        this.updateOverlay();
    }

    public RayTraceHit getRayTraceHit() {
        return rayTraceHit;
    }

    @Override
    public void onTick() {
        super.onTick();

        if (Keyboard.isKeyDown(Keyboard.KEY_V)) {
            RayTraceHit hit2 = performRayCast(5f);
            if (hit2.getType() != RayTraceHit.HitType.None) {
                System.out.println(hit2.toString());
            }
        }

        this.updateStandingOn();

        this.regenerationTimer++;

        if (this.regenerationTimer >= PlayerEntity.REGENERATION_TICKS) {
            this.regenerationTimer = 0;
            this.regenerate(1);
        }

        if (Keyboard.isKeyDown(Keyconfig.explosion)) {
            if (this.hasSelected) {
                int x = (int) Math.floor(this.rayTraceHit.getX());
                int y = (int) Math.floor(this.rayTraceHit.getY());
                int z = (int) Math.floor(this.rayTraceHit.getZ());
                Explosion.explode(this.world, x, y, z);
//                Explosion.explode(this.world, this.lookingAtX, this.lookingAtY,
//                        this.lookingAtZ);
            }
        }

        int id = this.inHand.getItemID();
        boolean sblock = this.world.getRegister().getBlock(id) != null;

        if (Keyboard.isKeyDown(Keyboard.KEY_R) && sblock) {
            if (this.hasSelected) {
                Explosion.fill(this.world, this.lookingAtX, this.lookingAtY,
                        this.lookingAtZ, id);
            }
        }

        if (Keyboard.isKeyDown(Keyconfig.particleFirework)) {
            if (this.hasSelected) {
                for (int i = 0; i < 500; i++) {
                    this.world.spawnParticleWithRandomDirectionFast(
                            this.lookingAtX, this.lookingAtY + 1,
                            this.lookingAtZ, this.rand.nextInt(10), 0.3f);
                }
            }
        }

        while (Mouse.next()) {

            if (Mouse.isGrabbed()) {

                if (Mouse.getEventButtonState()) {

                    if (Keyconfig.isDown(Keyconfig.crouch)) {

                        if (hasSelected) {
                            this.world.getBlockObject(this.lookingAtX,
                                    this.lookingAtY, this.lookingAtZ).onClick(
                                    Mouse.getEventButton(), this.lookingAtX,
                                    this.lookingAtY, this.lookingAtZ, world);

                        } else {
                            this.inHand.useItem(Mouse.getEventButton(),
                                    this.lookingAtX, this.lookingAtY,
                                    this.lookingAtZ, 0, this.world);
                        }

                    } else if (Mouse.getEventButton() == 0) {
                        if (this.hasSelected) {
                            int x = (int) Math.floor(this.rayTraceHit.getX());
                            int y = (int) Math.floor(this.rayTraceHit.getY());
                            int z = (int) Math.floor(this.rayTraceHit.getZ());
//                            this.world.setBlock(this.lookingAtX,
//                                    this.lookingAtY, this.lookingAtZ, 0, true,
//                                    true);
                            this.world.setBlock(
                                    x,
                                    y,
                                    z,
                                    0,
                                    true,
                                    true);
                        }
                    } else if (Mouse.getEventButton() == 1) {
                        if (this.hasSelected
                                && (this.lookingAtX != this.placesAtX
                                || this.lookingAtY != this.placesAtY || this.lookingAtZ != this.placesAtZ)) {
                            this.inHand.useItem(1, this.placesAtX,
                                    this.placesAtY, this.placesAtZ, 0,
                                    this.world);
                        }
                    }

                }

                this.updateBlock();

            } else {
                if (Mouse.getEventButtonState() && Mouse.getEventButton() == 0) {
                    if (this.world.getGui().isOpened())
                        this.world.getGui().checkMouseClosing();
                }

            }

        }

        if (this.inHand.shouldRecycle()) {
            this.inHand = new BasicBlockStack(world.getBlockObject(this.inHand
                    .getItemID()), 32);
        }

        if (!this.wasOnGround && this.onGround) {

            Block block = this.standingOn;

            if (block != null) {
                SoundManager.getInstance().playSound(block.getFallSound(),
                        0.7f + this.rand.nextFloat() * 0.25f);

                if (this.fallVelocity > 0.7f) {
                    int h = block.getFallHurt() * (int) (this.fallVelocity * 3);
                    this.hurt(h);
                }
            }
        }

        if (this.onGround) {
            this.stepTimer -= this.spf;
        } else {
            this.stepTimer = 0.0f;
        }

        if (this.stepTimer <= 0 && this.onGround) {
            Block block = this.standingOn;
            if (block != null) {
                SoundManager.getInstance().playSound(block.getFootStepSound(),
                        1.0f - (this.rand.nextFloat() * 0.2f));
            }

            this.stepTimer += PlayerEntity.STEP_TIMER_FULL;
        }

        if (!this.alive && !this.world.getGame().s().peacefulMode.getValue()) {
            //this.world.getGame().getWorldsManager().exitWorld();
        }

        this.updateOverlay();
    }

    private void updateStandingOn() {

        int blockX = (int) Math.floor(this.posX);
        int blockY = (int) Math.floor(this.posY - 1);
        int blockZ = (int) Math.floor(this.posZ);

        int b = this.world.getBlock(blockX, blockY, blockZ);

        if (b == 0) {
            float[][] blocksAround = new float[3][3];
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    if (this.world.getBlock(blockX + x - 1, blockY, blockZ + z
                            - 1) == 0) {
                        blocksAround[x][z] = -1;
                    } else {
                        float xDist = x - 0.5f;
                        float zDist = z - 0.5f;

                        blocksAround[x][z] = (float) Math.sqrt(xDist * xDist
                                + zDist * zDist);
                    }
                }
            }

            boolean foundBlock = false;

            float minDist = 999999;

            int closestX = 0;
            int closestZ = 0;

            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    if (blocksAround[x][z] >= 0) {
                        if (blocksAround[x][z] < minDist) {
                            foundBlock = true;
                            minDist = blocksAround[x][z];
                            closestX = x;
                            closestZ = z;
                        }
                    }
                }
            }

            b = foundBlock ? this.world.getBlock(closestX + blockX - 1, blockY,
                    closestZ + blockZ - 1) : 0;

        }

        this.standingOn = world.getBlockObject(b);

    }

    public void onRenderTick(float ptt) {
        super.onRenderTick(ptt);

        if (Mouse.isGrabbed()) {
            float mouseDX = Mouse.getDX() * 0.8f * 0.16f;
            float mouseDY = Mouse.getDY() * 0.8f * 0.16f;
            this.heading += mouseDX;
            this.tilt += mouseDY;
        }

        while (this.heading <= -180) {
            this.heading += 360;
        }
        while (this.heading > 180) {
            this.heading -= 360;
        }

        if (this.tilt < -90) {
            this.tilt = -90;
        }

        if (this.tilt > 90) {
            this.tilt = 90;
        }

        this.updateLookingAt();
        this.rayTraceHit = performRayCast(this.world.getGame().s().reach.getValue());
    }

    public void updateOverlay() {
        int heartsX = 80;
        int heartsY = Display.getHeight() - 43;

        if (this.lifesOverlay == null) {
            this.lifesOverlay = new BasicLifesOverlay(this, heartsX, heartsY);
            //this.world.getGame().getOverlayManager().addOverlay(this.lifesOverlay);
        } else {
            this.lifesOverlay.setPosition(heartsX, heartsY);
        }
    }

    @Override
    public void renderOverlay(float ptt) {
        TextureImpl.bindNone();
        this.inHand.getRenderer().render(ptt, 25, Display.getHeight() - 75, 50,
                50, this.world);
    }

    @Override
    public void updatePosition() {
        if (this.world.getGame().s().noclip.getValue()) {

            this.wasOnGround = this.onGround;

            float frictionMultipler = 0.7f;

            float speedC = 0;
            float speedStrafeC = 0;

            float multi = 1;

            if (!this.world.getGui().isOpened()) {
                if (Keyconfig.isDown(Keyconfig.boost)) {
                    multi = 8;
                }

                if (Keyconfig.isDown(Keyconfig.ahead)) {
                    speedC -= 0.4f * multi;
                }

                if (Keyconfig.isDown(Keyconfig.back)) {
                    speedC += 0.4f * multi;
                }

                if (Keyconfig.isDown(Keyconfig.left)) {
                    speedStrafeC -= 0.4f * multi;
                }

                if (Keyconfig.isDown(Keyconfig.right)) {
                    speedStrafeC += 0.4f * multi;
                }

                if (Keyconfig.isDown(Keyconfig.jump)) {
                    this.velY += 0.4f * multi;
                }

                if (Keyconfig.isDown(Keyconfig.crouch)) {
                    this.velY -= 0.4f * multi;
                }
            }

            this.speed += speedC;
            this.speedStrafe += speedStrafeC;

            this.speed *= frictionMultipler;
            this.speedStrafe *= frictionMultipler;

            spf = (float) Math.sqrt(this.speed * this.speed + this.speedStrafe
                    * this.speedStrafe);

            this.velY *= frictionMultipler;

            double toMoveZ = (this.posZ + Math.cos(-this.heading / 180
                    * Math.PI)
                    * this.speed)
                    + (Math.cos((-this.heading + 90) / 180 * Math.PI) * this.speedStrafe);
            double toMoveX = (this.posX + Math.sin(-this.heading / 180
                    * Math.PI)
                    * this.speed)
                    + (Math.sin((-this.heading + 90) / 180 * Math.PI) * this.speedStrafe);
            double toMoveY = (this.posY + (this.velY));

            float xa = (float) -(this.posX - toMoveX);
            float ya = (float) -(this.posY - toMoveY);
            float za = (float) -(this.posZ - toMoveZ);

            this.velX = xa;
            this.velZ = za;

            this.bb.move(xa, ya, za);

            this.onGround = false;

            this.posX = (this.bb.x0 + this.bb.x1) / 2.0F;
            this.posY = this.bb.y0;
            this.posZ = (this.bb.z0 + this.bb.z1) / 2.0F;

            return;
        }
        this.wasOnGround = this.onGround;

        int blockPosX = (int) Math.floor(this.posX);
        int blockPosY = (int) Math.floor(this.posY);
        int blockPosZ = (int) Math.floor(this.posZ);

        boolean inWater = ((this.world.getBlockObject(blockPosX, blockPosY,
                blockPosZ) instanceof BlockFluid) || (this.world.getBlockObject(
                blockPosX, blockPosY + 1, blockPosZ) instanceof BlockFluid));

        float d0 = this.world.getBlockObject(blockPosX, blockPosY, blockPosZ) != null ? this.world
                .getBlockObject(blockPosX, blockPosY, blockPosZ).getDensity()
                : 1;

        float d1 = this.world.getBlockObject(blockPosX, blockPosY + 1,
                blockPosZ) != null ? this.world.getBlockObject(blockPosX,
                blockPosY + 1, blockPosZ).getDensity() : 1;

        float density = (d0 + d1) / 2f;
        float frictionMultipler = 1f / density;

        float speedC = 0;
        float speedStrafeC = 0;

        float multi = 1;

        if (!this.world.getGui().isOpened()) {
            if (Keyboard.isKeyDown(56)) { //alt key
                multi = 15;
            }

            if (Keyboard.isKeyDown(42)) { //shift key
                multi = 0.5f;
            }

//            if (Keyconfig.isDown(Keyconfig.boost)) {
//                multi = 15;
//            }

            if (Keyconfig.isDown(Keyconfig.ahead)) {
                speedC -= this.onGround ? 0.25 * multi : 0.03 * multi;
            }

            if (Keyconfig.isDown(Keyconfig.back)) {
                speedC += this.onGround ? 0.25 * multi : 0.03 * multi;
            }

            if (Keyconfig.isDown(Keyconfig.left)) {
                speedStrafeC -= this.onGround ? 0.25 * multi : 0.03 * multi;
            }

            if (Keyconfig.isDown(Keyconfig.right)) {
                speedStrafeC += this.onGround ? 0.25 * multi : 0.03 * multi;
            }

            if (Keyconfig.isDown(Keyconfig.jump)) {
                if (this.onGround) {
                    if (multi == 1) {
                        this.velY += PlayerEntity.JUMP_STRENGTH;
                    } else if (multi > 1) {
                        this.velY += PlayerEntity.JUMP_STRENGTH * 4;
                    } else {
                        this.velY += PlayerEntity.JUMP_STRENGTH * 0.95f;
                    }
                } else if (!this.onGround && inWater) {
                    this.velY += PlayerEntity.JUMP_STRENGTH / 4;
                }
            }
        }

        float xsq = Math.abs(speedC) * Math.abs(speedC);
        float ysq = Math.abs(speedStrafeC) * Math.abs(speedStrafeC);
        float sp = (float) Math.sqrt(xsq + ysq);
        if (sp > PlayerEntity.MAX_WALK_SPEED * multi) {
            float mult = PlayerEntity.MAX_WALK_SPEED * multi / sp;
            speedC *= mult;
            speedStrafeC *= mult;
        }

        this.speed += speedC;
        this.speedStrafe += speedStrafeC;

        this.speed *= this.onGround ? 0.5f : 0.9f;
        this.speed *= frictionMultipler;
        this.speedStrafe *= this.onGround ? 0.5f : 0.9f;
        this.speedStrafe *= frictionMultipler;

        spf = (float) Math.sqrt(this.speed * this.speed + this.speedStrafe
                * this.speedStrafe);

        this.velY -= World.GRAVITY;
        this.velY *= frictionMultipler;

        double toMoveZ = (this.posZ + Math.cos(-this.heading / 180 * Math.PI)
                * this.speed)
                + (Math.cos((-this.heading + 90) / 180 * Math.PI) * this.speedStrafe);
        double toMoveX = (this.posX + Math.sin(-this.heading / 180 * Math.PI)
                * this.speed)
                + (Math.sin((-this.heading + 90) / 180 * Math.PI) * this.speedStrafe);
        double toMoveY = (this.posY + (this.velY));

        float xa = (float) -(this.posX - toMoveX);
        float ya = (float) -(this.posY - toMoveY);
        float za = (float) -(this.posZ - toMoveZ);

        this.velX = xa;
        this.velZ = za;

        float yab = ya;

        float[] clipped = this.world.clipMovement(this.bb, xa, ya, za);
        ya = clipped[1];

        this.onGround = yab != ya && yab < 0.0F;

        if (this.onGround) {
            this.fallVelocity = -this.velY;
            this.velY = 0;
        }

        if (yab != ya) {
            this.velY = 0;
        }

        this.posX = (this.bb.x0 + this.bb.x1) / 2.0F;
        this.posY = this.bb.y0;
        this.posZ = (this.bb.z0 + this.bb.z1) / 2.0F;

    }

    private void updateBlock() {
        int wh = Mouse.getEventDWheel();

        if (wh > 0) {
            int newSelectedBlock = this.inHand.getItemID() + 1;

			/*if(newSelectedBlock == 19) {
                this.inHand = new BasicItemStack(
						IDRegister.itemImaginaryChocolate, 1);
				return;
			}*/

            if (newSelectedBlock > (world.getRegister().getBlockCount())) {
                newSelectedBlock = 1;
            }

            this.setHeldItemID(newSelectedBlock);
        }

        if (wh < 0) {
            int newSelectedBlock = this.inHand.getItemID() - 1;
            if (newSelectedBlock < 1) {
                newSelectedBlock = world.getRegister().getBlockCount();
            }

            this.setHeldItemID(newSelectedBlock);
        }
    }

    private void updateLookingAt() {
        float reach = this.world.getGame().s().reach.getValue();

        float xn = (float) this.getPosXPartial();
        float yn = (float) this.getPosYPartial() + PlayerEntity.PLAYER_HEIGHT;
        float zn = (float) this.getPosZPartial();

        float xl;
        float yl;
        float zl;

        float yChange = (float) Math.cos((-this.tilt + 90) / 180 * Math.PI);
        float ymult = (float) Math.sin((-this.tilt + 90) / 180 * Math.PI);

        float xChange = (float) (Math.cos((-this.heading + 90) / 180 * Math.PI) * ymult);
        float zChange = (float) (-Math
                .sin((-this.heading + 90) / 180 * Math.PI) * ymult);

        for (float f = 0; f <= reach; f += 0.01f) {
            xl = xn;
            yl = yn;
            zl = zn;

            xn = (float) (this.getPosXPartial() + f * xChange);
            yn = (float) (this.getPosYPartial() + PlayerEntity.EYES_HEIGHT + f
                    * yChange);
            zn = (float) (this.getPosZPartial() + f * zChange);

            if (this.getWorld().getBlock((int) Math.floor(xn),
                    (int) Math.floor(yn), (int) Math.floor(zn)) > 0) {
                this.lookingAtX = (int) Math.floor(xn);
                this.lookingAtY = (int) Math.floor(yn);
                this.lookingAtZ = (int) Math.floor(zn);

                this.placesAtX = (int) Math.floor(xl);
                this.placesAtY = (int) Math.floor(yl);
                this.placesAtZ = (int) Math.floor(zl);
                this.hasSelected = true;
                return;
            }

            this.hasSelected = false;

        }
    }

    public float[] getLookVector() {
        double rad = Math.toRadians(1.0D);

        float x = (float) Math.sin(this.heading * 0.017453292F - (float) Math.PI);
        float y = (float) Math.sin(this.tilt * 0.017453292F);
        float z = (float) -Math.cos(-this.heading * 0.017453292F - (float) Math.PI);
        float k = (float) -Math.cos(this.tilt * 0.017453292F);

        return new float[]{(x * k), y, (z * k)};
    }

    public RayTraceHit performRayCast(float maxDist) {
        RayTraceHit hit = new RayTraceHit();
        float size = 0.25f;
        Vector3 origin = new Vector3(posX, posY + PlayerEntity.PLAYER_HEIGHT - size / 2f, posZ);
        Vector3 pos = origin;

        float[] look = getLookVector();
        Vector3 ray = new Vector3(look[0], look[1], look[2]);

        int x;
        int y;
        int z;
        float step = 0.015f;
        AABB rayBB = new AABB(0, 0, 0, size, size, size);
        Vector3 blockPos = null;
        for (float dist = 0f; dist < maxDist - step; dist += step) {
            x = (int) Math.floor(pos.getX());
            y = (int) Math.floor(pos.getY());
            z = (int) Math.floor(pos.getZ());

            Block b = world.getBlockObject(x, y, z);
            //AABB translatedRay = rayBB.translate(pos.getX(), pos.getY(), pos.getZ());
            if (b != null) {
                AABB blockBB = b.getCollisionBox(world, x, y, z);
                if (blockBB != null) {
                    //if (blockBB.intersectAABB(translatedRay)) {
                    if (blockBB.contains(pos)) {
                        Facing side = Facing.Down;
                        blockPos = new Vector3(x + 0.5f, y + 0.5f, z + 0.5f);


                        Vector3 center = blockBB.getCenter();
                        if (b instanceof BlockSlab || b instanceof BlockPlant) {
                            // center = blockBB.getCenter();
                            // System.out.println(center);
                        }

                        Vector3 diff = center.sub(pos);
                        //Vector3 diff = blockPos.sub(pos);
                        float absx = Math.abs(diff.getX());
                        float absy = Math.abs(diff.getY());
                        float absz = Math.abs(diff.getZ());

                        if (absx > absy && absx > absz) {
                            if (diff.getX() > 0)
                                side = Facing.West;
                            else
                                side = Facing.East;
                        }
                        if (absy > absx && absy > absz) {
                            if (diff.getY() > 0)
                                side = Facing.Down;
                            else
                                side = Facing.Up;
                        }
                        if (absz > absy && absz > absx) {
                            if (diff.getZ() > 0)
                                side = Facing.North;
                            else
                                side = Facing.South;
                        }
                        hit = new RayTraceHit(x, y, z, 0f, side, RayTraceHit.HitType.Block, b.getName());
                    }
                }
            }

            Vector3 multipliedRay = ray.mul((maxDist + step) - dist);
            pos = origin.add(multipliedRay);

//            for(Entity e : entities)
//            {
//                if(e.getBoundingBox().intersectAABB(translatedRay) && e != sender)
//                {
//                    infos.type = CollisionType.ENTITY;
//                    infos.value = e;
//                    infos.x = e.posX;
//                    infos.y = e.posY;
//                    infos.z = e.posZ;
//                    infos.distance = maxReachedDist;
//                }
//            }
        }
        return hit;
    }

    private RayTraceHit rayTrace2(float distance) {
        RayTraceHit rayTraceHit = new RayTraceHit();

        float size = 0.45f;
        AABB rayBB = new AABB(0, 0, 0, size, size, size);

        float playerX = this.getPosXPartial();
        float playerY = this.getPosYPartial() + PlayerEntity.PLAYER_HEIGHT;
        float playerZ = this.getPosZPartial();

        float xl;
        float yl;
        float zl;

        float yChange = (float) Math.cos((-this.tilt + 90) / 180 * Math.PI);
        float ymult = (float) Math.sin((-this.tilt + 90) / 180 * Math.PI);

        float xChange = (float) (Math.cos((-this.heading + 90) / 180 * Math.PI) * ymult);
        float zChange = (float) (-Math.sin((-this.heading + 90) / 180 * Math.PI) * ymult);

        for (float f = 0; f <= distance; f += 0.01f) {
            xl = playerX;
            yl = playerY;
            zl = playerZ;


            playerX = this.getPosXPartial() + f * xChange;
            playerY = this.getPosYPartial() + PlayerEntity.EYES_HEIGHT + f
                    * yChange;
            playerZ = this.getPosZPartial() + f * zChange;

            int hitX = (int) Math.floor(playerX);
            int hitY = (int) Math.floor(playerY);
            int hitZ = (int) Math.floor(playerZ);

            if (this.getWorld().getBlock(hitX, hitY, hitZ) > 0) {
                rayTraceHit = new RayTraceHit(hitX, hitY, hitZ, f, Facing.Down, RayTraceHit.HitType.Block);
                return rayTraceHit;
            }
        }
        return rayTraceHit;
    }

    @Override
    public void setPosition(float x, float y, float z) {
        super.setPosition(x, y, z);
        this.bb = new AABB(this.posX - PlayerEntity.PLAYER_SIZE / 2, this.posY,
                this.posZ - PlayerEntity.PLAYER_SIZE / 2, this.posX
                + PlayerEntity.PLAYER_SIZE / 2, this.posY
                + PlayerEntity.PLAYER_HEIGHT, this.posZ
                + PlayerEntity.PLAYER_SIZE / 2);
    }

    public int getHeldItemID() {
        return this.inHand.getItemID();
    }

    public void setHeldItemID(int selectedBlockID) {
        this.inHand = new BasicBlockStack(
                world.getBlockObject(selectedBlockID), 32);
    }

    public Facing getFacing() {
        return Facing.fromIndex(Math.round(this.heading / 90f) & 3);
    }

    public float getHeading() {
        return this.heading;
    }

    public void setHeading(float heading) {
        this.heading = heading;
    }

    public IObjectStack getInHand() {
        return inHand;
    }

    public float getTilt() {
        return this.tilt;
    }

    public void setTilt(float tilt) {
        this.tilt = tilt;
    }

    public int getLookingAtX() {
        return this.lookingAtX;
    }

    public int getLookingAtY() {
        return this.lookingAtY;
    }

    public int getLookingAtZ() {
        return this.lookingAtZ;
    }

    public int getPlacesAtX() {
        return this.placesAtX;
    }

    public int getPlacesAtY() {
        return this.placesAtY;
    }

    public int getPlacesAtZ() {
        return this.placesAtZ;
    }

    public boolean hasSelectedBlock() {
        return this.hasSelected;
    }

    public float getSpeed() {
        return this.speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getSpeedStrafe() {
        return this.speedStrafe;
    }

    public void setSpeedStrafe(float speedStrafe) {
        this.speedStrafe = speedStrafe;
    }
}
