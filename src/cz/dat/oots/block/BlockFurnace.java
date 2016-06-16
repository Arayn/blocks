package cz.dat.oots.block;

import cz.dat.oots.block.renderer.BlockRendererSpecial;
import cz.dat.oots.util.Facing;
import cz.dat.oots.world.IDRegister;
import cz.dat.oots.world.World;

public class BlockFurnace extends BlockSpecial {

    public BlockFurnace(String blockName, IDRegister register) {
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
        Facing blockFacing = getFacing(x, y, z, world);
        if (facing == blockFacing){
            return 21;
        }else if (facing == Facing.Up || facing == Facing.Down){
            return 24;
        }
        return 23;
    }
}
