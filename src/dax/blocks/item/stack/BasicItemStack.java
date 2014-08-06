package dax.blocks.item.stack;

import java.util.Random;

import dax.blocks.item.Item;
import dax.blocks.util.GLHelper;
import dax.blocks.world.World;

public class BasicItemStack implements IObjectStack {

	int items = 1;
	Item surroudingItem;
	int[] itemInstanceIdentifiactors;
	Random rand = new Random();
	
	public BasicItemStack(Item itemFor, int count) {
		if(count > this.getMaximumItemsPerStack() || count < 1) {
			count = 1;
		}
		
		this.setCurrentItemsCount(count);
		
		this.itemInstanceIdentifiactors = new int[this.getMaximumItemsPerStack()];
		this.surroudingItem = itemFor;
	}
	
	@Override
	public int getMaximumItemsPerStack() {
		return 16;
	}

	@Override
	public int getItemID() {
		return surroudingItem.getID();
	}

	@Override
	public String getShowedName() {
		return surroudingItem.getShowedName();
	}

	@Override
	public String getShowedDescription() {
		return null;
	}

	@Override
	public int getCurrentItemsCount() {
		return this.items;
	}

	@Override
	public void tickItems(World w) {
		for(int i = 0; i < this.getCurrentItemsCount(); i++) {
			surroudingItem.onTick(this.itemInstanceIdentifiactors[i], w);
		}
	}

	@Override
	public void renderTickItems(float partialTickTime, World w) {
		for(int i = 0; i < this.getCurrentItemsCount(); i++) {
			surroudingItem.onRenderTick(partialTickTime, this.itemInstanceIdentifiactors[i], w);
		}
	}

	@Override
	public void renderTexture(int x, int y, int width, int height) {
		GLHelper.drawTexture(surroudingItem.getTexture(), x, x + width, y, y + height);
	}

	@Override
	public void setCurrentItemsCount(int count) throws IllegalArgumentException {
		if(count > this.getMaximumItemsPerStack() || count < 1) {
			throw new IllegalArgumentException("Count must be greater than 0 and smaller the maximum items per stack");
		}
		
		this.items = count;
		
		for(int i = 0; i < this.getCurrentItemsCount(); i++) {
			if(this.itemInstanceIdentifiactors[i] == 0) {
				this.itemInstanceIdentifiactors[i] = surroudingItem.getName().hashCode() + rand.nextInt(7) * rand.nextInt(13);
			}
		}
		
		for(int i = this.getCurrentItemsCount(); i < this.getMaximumItemsPerStack(); i++) {
			if(this.itemInstanceIdentifiactors[i] != 0) {
				this.itemInstanceIdentifiactors[i] = 0;
			}
		}
	}

}