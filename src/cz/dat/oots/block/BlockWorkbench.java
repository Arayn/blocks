package cz.dat.oots.block;

import cz.dat.oots.block.renderer.BlockRendererSpecial;
import cz.dat.oots.util.Facing;
import cz.dat.oots.world.IDRegister;
import cz.dat.oots.world.World;

public class BlockWorkbench extends Block {


    public BlockWorkbench(String blockName, IDRegister register) {
        super(blockName, register);
        setRenderer(new BlockRendererSpecial());
    }

    @Override
    public void onUpdate(int x, int y, int z, int type, World world) {

    }

    @Override
    public void onNeighbourUpdate(int x, int y, int z, World world) {

    }

    @Override
    public void onTick(int x, int y, int z, World world) {

    }

    @Override
    public void onRenderTick(float partialTickTime, int x, int y, int z, World world) {

    }

    @Override
    public void onClick(int mouseButton, int x, int y, int z, World world) {

    }

    @Override
    public int getTexture(int x, int y, int z, World world, Facing facing) {
        if (facing == Facing.North || facing == Facing.West){
            return 25;
        } else if (facing == Facing.South || facing == Facing.East){
            return 26;
        } else if (facing == Facing.Up){
            return 27;
        }
        return 2;
    }

    @Override
    public int getTexture(Facing facing) {
        if (facing == Facing.North || facing == Facing.West){
            return 25;
        } else if (facing == Facing.South || facing == Facing.East){
            return 26;
        } else if (facing == Facing.Up){
            return 27;
        }
        return 2;
    }
}
