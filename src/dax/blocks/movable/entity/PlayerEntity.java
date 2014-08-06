package dax.blocks.movable.entity;

import java.util.Random;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import dax.blocks.Game;
import dax.blocks.TextureManager;
import dax.blocks.block.Block;
import dax.blocks.collisions.AABB;
import dax.blocks.gui.ingame.GuiManager;
import dax.blocks.item.stack.BasicBlockStack;
import dax.blocks.item.stack.BasicItemStack;
import dax.blocks.item.stack.IObjectStack;
import dax.blocks.render.IOverlayRenderer;
import dax.blocks.settings.Keyconfig;
import dax.blocks.settings.Settings;
import dax.blocks.sound.SoundManager;
import dax.blocks.util.GLHelper;
import dax.blocks.world.Explosion;
import dax.blocks.world.IDRegister;
import dax.blocks.world.World;

public class PlayerEntity extends Entity implements IOverlayRenderer {

	public static final float PLAYER_HEIGHT = 1.7f;
	public static final float EYES_HEIGHT = 1.6f;
	public static final float PLAYER_SIZE = 0.5f;
	public static final float STEP_TIMER_FULL = 2.25f;
	public static final float JUMP_STRENGTH = 0.4f;
	public static final float MAX_WALK_SPEED = 0.25f;
	public static final int REGENERATION_TICKS = 20;

	private IObjectStack inHand;

	private int lookingAtX;
	private int lookingAtY;
	private int lookingAtZ;

	private int placesAtX;
	private int placesAtY;
	private int placesAtZ;

	private boolean hasSelected = false;

	private float heading = 140.0F;
	private float tilt = -60.0F;

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
		this.setSelectedBlockID(1);
		this.bb = new AABB(posX - PLAYER_SIZE / 2, posY,
				posZ - PLAYER_SIZE / 2, posX + PLAYER_SIZE / 2, posY
						+ PLAYER_HEIGHT, posZ + PLAYER_SIZE / 2);
	}

	@Override
	public void onTick() {
		super.onTick();

		updateStandingOn();

		regenerationTimer++;

		if (this.regenerationTimer >= PlayerEntity.REGENERATION_TICKS) {
			regenerationTimer = 0;
			this.regenerate(1);
		}

		if (Keyboard.isKeyDown(Keyconfig.explosion)) {
			if (hasSelected) {
				Explosion.explode(world, lookingAtX, lookingAtY, lookingAtZ);
			}
		}

		if (Keyboard.isKeyDown(Keyconfig.particleFirework)) {
			if (hasSelected) {
				for (int i = 0; i < 50; i++) {
					world.spawnParticleWithRandomDirectionFast(this.lookingAtX,
							this.lookingAtY + 1, this.lookingAtZ,
							rand.nextInt(10), 0.3f);
				}
			}
		}

		while (Mouse.next()) {
			if (Mouse.isGrabbed()) {
				if (Mouse.getEventButtonState()) {
					if (Mouse.getEventButton() == 0
							&& Keyconfig.isDown(Keyconfig.crouch)) {
						if (hasSelected) {
							this.inHand.useItem(0, lookingAtX, lookingAtY, lookingAtZ, 0, world);
						}
					} else if (Mouse.getEventButton() == 0) {
						if (hasSelected) {
							world.setBlock(lookingAtX, lookingAtY, lookingAtZ,
									0, true, true);
						}
					}
					if (Mouse.getEventButton() == 1) {
						if (hasSelected
								&& (lookingAtX != placesAtX
										|| lookingAtY != placesAtY || lookingAtZ != placesAtZ)) {
							this.inHand.useItem(1, placesAtX, placesAtY, placesAtZ, 0, world);
						}
					}
				}

				updateBlock();
			} else {
				if(Mouse.getEventButtonState() && Mouse.getEventButton() == 0) {
					if(GuiManager.getInstance().isOpened())
						GuiManager.getInstance().checkMouseClosing();
				}
			}
		}
		
		if(this.inHand.shouldRecycle()) {
			this.inHand = new BasicBlockStack(world.getBlockObject(this.inHand.getItemID()), 32);
		}
		

		if (!wasOnGround && onGround) {

			Block block = this.standingOn;

			if (block != null) {

				SoundManager.getInstance().playSound(block.getFallSound(),
						0.7f + rand.nextFloat() * 0.25f);

				if (fallVelocity > 0.7f) {
					int h = block.getFallHurt() * (int) (fallVelocity * 3);
					this.hurt(h);
				}
			}
		}

		if (onGround) {
			stepTimer -= spf;
		} else {
			stepTimer = 0.0f;
		}

		if (stepTimer <= 0 && onGround) {
			Block block = this.standingOn;
			if (block != null) {
				SoundManager.getInstance().playSound(block.getFootStepSound(),
						1.0f - (rand.nextFloat() * 0.2f));
			}

			stepTimer += STEP_TIMER_FULL;
		}

		if (!this.alive && !Settings.getInstance().peacefulMode.getValue()) {
			Game.getInstance().exitGame();
		}
	}

	private void updateStandingOn() {

		int blockX = (int) Math.floor(this.posX);
		int blockY = (int) Math.floor(this.posY - 1);
		int blockZ = (int) Math.floor(this.posZ);

		int b = world.getBlock(blockX, blockY, blockZ);

		if (b == 0) {
			float[][] blocksAround = new float[3][3];
			for (int x = 0; x < 3; x++) {
				for (int z = 0; z < 3; z++) {
					if (world.getBlock(blockX + x - 1, blockY, blockZ + z - 1) == 0) {
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

			b = foundBlock ? world.getBlock(closestX + blockX - 1, blockY,
					closestZ + blockZ - 1) : 0;

		}

		this.standingOn = world.getBlockObject(b);

	}

	public void onRenderTick(float ptt) {
		super.onRenderTick(ptt);

		if (Mouse.isGrabbed()) {
			float mouseDX = Mouse.getDX() * 0.8f * 0.16f;
			float mouseDY = Mouse.getDY() * 0.8f * 0.16f;
			heading += mouseDX;
			tilt += mouseDY;
		}

		while (heading <= -180) {
			heading += 360;
		}
		while (heading > 180) {
			heading -= 360;
		}

		if (tilt < -90) {
			tilt = -90;
		}

		if (tilt > 90) {
			tilt = 90;
		}

		updateLookingAt();
	}

	@Override
	public void renderOverlay(float ptt) {
		this.inHand.renderGUITexture(25, Settings.getInstance().windowHeight.getValue() - 75, 50, 50);
		
		int heartsX = 80;
		int heartsY = Settings.getInstance().windowHeight.getValue() - 43;

		GLHelper.drawTexture(TextureManager.life_zero, heartsX, heartsY);
		GLHelper.drawTextureCropped(TextureManager.life_full, heartsX, heartsY,
				lifes, 1);
	}

	@Override
	public void updatePosition() {
		wasOnGround = onGround;

		int blockPosX = (int) Math.floor(this.posX);
		int blockPosY = (int) Math.floor(this.posY);
		int blockPosZ = (int) Math.floor(this.posZ);

		boolean inWater = ((world.getBlockObject(blockPosX, blockPosY, blockPosZ) == IDRegister.water) || 
				(world.getBlockObject(blockPosX, blockPosY + 1, blockPosZ) == IDRegister.water));
		
		float d0 = world.getBlockObject(blockPosX, blockPosY, blockPosZ) != null ? 
				world.getBlockObject(blockPosX, blockPosY, blockPosZ).getDensity() : 1;
				
		float d1 = world.getBlockObject(blockPosX, blockPosY + 1, blockPosZ) != null ? 
				world.getBlockObject(blockPosX, blockPosY + 1, blockPosZ).getDensity() : 1;
				
		float density = (d0 + d1) / 2f;
		float frictionMultipler = 1f / density;

		float speedC = 0;
		float speedStrafeC = 0;

		float multi = 1;

		if (Keyconfig.isDown(Keyconfig.boost)) {
			multi = 15;
		}

		if (Keyconfig.isDown(Keyconfig.ahead)) {
			speedC -= onGround ? 0.25 * multi : 0.03 * multi;
		}

		if (Keyconfig.isDown(Keyconfig.back)) {
			speedC += onGround ? 0.25 * multi : 0.03 * multi;
		}

		if (Keyconfig.isDown(Keyconfig.left)) {
			speedStrafeC -= onGround ? 0.25 * multi : 0.03 * multi;
		}

		if (Keyconfig.isDown(Keyconfig.right)) {
			speedStrafeC += onGround ? 0.25 * multi : 0.03 * multi;
		}

		if (Keyconfig.isDown(Keyconfig.jump)) {
			if (onGround) {
				if (multi == 1) {
					velY += JUMP_STRENGTH;
				} else if (multi > 1) {
					velY += JUMP_STRENGTH * 4;
				} else {
					velY += JUMP_STRENGTH * 0.95f;
				}
			} else if (!onGround && inWater) {
				velY += JUMP_STRENGTH / 4;
			}
		}

		float xsq = Math.abs(speedC) * Math.abs(speedC);
		float ysq = Math.abs(speedStrafeC) * Math.abs(speedStrafeC);
		float sp = (float) Math.sqrt(xsq + ysq);
		if (sp > MAX_WALK_SPEED * multi) {
			float mult = MAX_WALK_SPEED * multi / sp;
			speedC *= mult;
			speedStrafeC *= mult;
		}

		speed += speedC;
		speedStrafe += speedStrafeC;

		speed *= onGround ? 0.5f : 0.9f;
		speed *= frictionMultipler;
		speedStrafe *= onGround ? 0.5f : 0.9f;
		speedStrafe *= frictionMultipler;

		spf = (float) Math.sqrt(speed * speed + speedStrafe * speedStrafe);

		velY -= World.GRAVITY;
		velY *= frictionMultipler;

		double toMoveZ = (posZ + Math.cos(-heading / 180 * Math.PI) * speed)
				+ (Math.cos((-heading + 90) / 180 * Math.PI) * speedStrafe);
		double toMoveX = (posX + Math.sin(-heading / 180 * Math.PI) * speed)
				+ (Math.sin((-heading + 90) / 180 * Math.PI) * speedStrafe);
		double toMoveY = (posY + (velY));

		float xa = (float) -(posX - toMoveX);
		float ya = (float) -(posY - toMoveY);
		float za = (float) -(posZ - toMoveZ);

		velX = xa;
		velZ = za;

		float yab = ya;

		float[] clipped = world.clipMovement(this.bb, xa, ya, za);	
		ya = clipped[1];
		
		this.onGround = yab != ya && yab < 0.0F;

		if (this.onGround) {
			this.fallVelocity = -velY;
			velY = 0;
		}

		if (yab != ya) {
			velY = 0;
		}

		this.posX = (this.bb.x0 + this.bb.x1) / 2.0F;
		this.posY = this.bb.y0;
		this.posZ = (this.bb.z0 + this.bb.z1) / 2.0F;

	}

	private void updateBlock() {
		int wh = Mouse.getEventDWheel();

		if (wh > 0) {
			int newSelectedBlock = this.inHand.getItemID() + 1;
			
			if(newSelectedBlock == 18) {
				this.inHand = new BasicItemStack(IDRegister.itemImaginaryChocolate, 1);
				return;
			}
			
			if (newSelectedBlock > (world.getRegister().getBlockCount())) {
				newSelectedBlock = 1;
			}

			this.setSelectedBlockID(newSelectedBlock);
		}

		if (wh < 0) {
			int newSelectedBlock = this.inHand.getItemID() - 1;
			if (newSelectedBlock < 1) {
				newSelectedBlock = world.getRegister().getBlockCount();
			}

			this.setSelectedBlockID(newSelectedBlock);
		}
	}

	private void updateLookingAt() {
		float reach = Settings.getInstance().reach.getValue();

		float xn = (float) getPosXPartial();
		float yn = (float) getPosYPartial() + PLAYER_HEIGHT;
		float zn = (float) getPosZPartial();

		float xl;
		float yl;
		float zl;

		float yChange = (float) Math.cos((-tilt + 90) / 180 * Math.PI);
		float ymult = (float) Math.sin((-tilt + 90) / 180 * Math.PI);

		float xChange = (float) (Math.cos((-heading + 90) / 180 * Math.PI) * ymult);
		float zChange = (float) (-Math.sin((-heading + 90) / 180 * Math.PI) * ymult);

		for (float f = 0; f <= reach; f += 0.01f) {
			xl = xn;
			yl = yn;
			zl = zn;

			xn = (float) (getPosX() + f * xChange);
			yn = (float) (getPosY() + EYES_HEIGHT + f * yChange);
			zn = (float) (getPosZ() + f * zChange);

			if (getWorld().getBlock((int) Math.floor(xn), (int) Math.floor(yn),
					(int) Math.floor(zn)) > 0) {
				lookingAtX = (int) Math.floor(xn);
				lookingAtY = (int) Math.floor(yn);
				lookingAtZ = (int) Math.floor(zn);

				placesAtX = (int) Math.floor(xl);
				placesAtY = (int) Math.floor(yl);
				placesAtZ = (int) Math.floor(zl);
				hasSelected = true;
				return;
			}

			hasSelected = false;

		}
	}

	@Override
	public void setPosition(float x, float y, float z) {
		super.setPosition(x, y, z);
		this.bb = new AABB(posX - PLAYER_SIZE / 2, posY,
				posZ - PLAYER_SIZE / 2, posX + PLAYER_SIZE / 2, posY
						+ PLAYER_HEIGHT, posZ + PLAYER_SIZE / 2);
	}

	public int getSelectedBlockID() {
		return this.inHand.getItemID();
	}

	public void setSelectedBlockID(int selectedBlockID) {
		this.inHand = new BasicBlockStack(world.getBlockObject(selectedBlockID), 32);
	}

	public float getHeading() {
		return heading;
	}

	public void setHeading(float heading) {
		this.heading = heading;
	}

	public float getTilt() {
		return tilt;
	}

	public void setTilt(float tilt) {
		this.tilt = tilt;
	}

	public int getLookingAtX() {
		return lookingAtX;
	}

	public int getLookingAtY() {
		return lookingAtY;
	}

	public int getLookingAtZ() {
		return lookingAtZ;
	}

	public int getPlacesAtX() {
		return placesAtX;
	}

	public int getPlacesAtY() {
		return placesAtY;
	}

	public int getPlacesAtZ() {
		return placesAtZ;
	}

	public boolean hasSelectedBlock() {
		return hasSelected;
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public float getSpeedStrafe() {
		return speedStrafe;
	}

	public void setSpeedStrafe(float speedStrafe) {
		this.speedStrafe = speedStrafe;
	}

}