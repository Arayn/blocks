package cz.dat.oots.block;

import cz.dat.oots.block.renderer.BlockRendererSpecial;
import cz.dat.oots.data.IDataObject;
import cz.dat.oots.data.block.SpecialDataObject;
import cz.dat.oots.util.Facing;
import cz.dat.oots.world.IDRegister;
import cz.dat.oots.world.World;

public class BlockSpecial extends Block {


    //short facing = 0;

    public BlockSpecial(String blockName, IDRegister register) {
        super(blockName, register);
        setRenderer(new BlockRendererSpecial());
    }

    @Override
    public IDataObject createDataObject() {
        return new SpecialDataObject(this);
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
    public void onPlaced(int x, int y, int z, World world) {
        super.onPlaced(x, y, z, world);
        this.setFacing(x, y, z, world, world.getPlayer().getFacing().getOpposite());
    }

    public Facing getFacing(int x, int y, int z, World world) {
        if (world.hasData(x, y, z)) {
            return ((SpecialDataObject) world.getData(x, y, z)).getFacing();
        }
        return Facing.Down;
    }

    public void setFacing(int x, int y, int z, World world, Facing facing) {
        SpecialDataObject dataObject;

        if (!world.hasData(x, y, z)) {
            dataObject = (SpecialDataObject) world.createData(x, y, z, this);
        } else {
            dataObject = (SpecialDataObject) world.getData(x, y, z);
        }

        dataObject.setFacing(facing);
        world.setChunkDirty(x >> 4, y / 16, z >> 4);
    }
}
