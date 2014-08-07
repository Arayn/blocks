package dax.blocks.block;

import dax.blocks.world.IDRegister;
import dax.blocks.world.World;

public class BlockBasic extends Block {

	protected float lightColorR = 1;
	protected float lightColorG = 1;
	protected float lightColorB = 1;
	
	public BlockBasic(String name, IDRegister r) {
		super(name, r);
	}

	@Override
	public void onRenderTick(float partialTickTime, int x, int y, int z, World world) {
	}


	@Override
	public void onTick(int x, int y, int z, World world) {
	}

	@Override
	public void onClick(int button, int x, int y, int z, World world) {
	}


}
