package dax.blocks.movable;

public abstract class Movable {

	private float posX;
	private float posY;
	private float posZ;
	
	private float posXPartial;
	private float posYPartial;
	private float posZPartial;
	
	protected float velX;
	protected float velY;
	protected float velZ;
	
	private float lastPosX;
	private float lastPosY;
	private float lastPosZ;

	public void onTick() {
		lastPosX = posX;
		lastPosY = posY;
		lastPosZ = posZ;
		
		updatePosition();
	}

	public void onRenderTick(float ptt) {		
		updateRenderPosition(ptt);	
	}

	public Movable(float x, float y, float z) {
		this(x, y, z, 0, 0, 0);
	}

	public Movable(float x, float y, float z, float velX, float velY, float velZ) {
		setPosX(x);
		setPosY(y);
		setPosZ(z);
		setLastPosX(x);
		setLastPosY(y);
		setLastPosZ(z);
		setVelX(velX);
		setVelY(velY);
		setVelZ(velZ);
	}

	private void updateRenderPosition(float ptt) {
		float deltaX = posX - lastPosX;
		this.posXPartial = lastPosX + deltaX*ptt;
		
		float deltaY = posY - lastPosY;
		this.posYPartial = lastPosY + deltaY*ptt;
		
		float deltaZ = posZ - lastPosZ;
		this.posZPartial = lastPosZ + deltaZ*ptt;
	}

	public float getVelX() {
		return velX;
	}

	public void setVelX(float velX) {
		this.velX = velX;
	}

	public float getVelY() {
		return velY;
	}

	public void setVelY(float velY) {
		this.velY = velY;
	}

	public float getVelZ() {
		return velZ;
	}

	public void setVelZ(float velZ) {
		this.velZ = velZ;
	}

	public float getLastPosX() {
		return lastPosX;
	}

	public void setLastPosX(float lastPosX) {
		this.lastPosX = lastPosX;
	}

	public float getLastPosY() {
		return lastPosY;
	}

	public void setLastPosY(float lastPosY) {
		this.lastPosY = lastPosY;
	}

	public float getLastPosZ() {
		return lastPosZ;
	}

	public void setLastPosZ(float lastPosZ) {
		this.lastPosZ = lastPosZ;
	}

	public abstract void updatePosition();
	
	public float getPosX() {
		return posX;
	}
	
	public float getPosY() {
		return posY;
	}
	
	public float getPosZ() {
		return posZ;
	}
	
	public void setPosX(float posX) {
		this.posX = posX;
	}
	
	public void setPosY(float posY) {
		this.posY = posY;
	}
	
	public void setPosZ(float posZ) {
		this.posZ = posZ;
	}
	
	public void setPosXPartial(float posXPartial) {
		this.posXPartial = posXPartial;
	}
	
	public void setPosYPartial(float posYPartial) {
		this.posYPartial = posYPartial;
	}
	
	public void setPosZPartial(float posZPartial) {
		this.posZPartial = posZPartial;
	}
	
	public float getPosXPartial() {
		return posXPartial;
	}
	
	public float getPosYPartial() {
		return posYPartial;
	}
	
	public float getPosZPartial() {
		return posZPartial;
	}
	
}