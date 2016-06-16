package cz.dat.oots.data.block;

import cz.dat.oots.block.BlockSpecial;
import cz.dat.oots.data.IDataObject;
import cz.dat.oots.settings.ObjectType;
import cz.dat.oots.util.Facing;
import cz.dat.oots.util.GameUtil;

import java.util.ArrayList;
import java.util.List;

public class SpecialDataObject implements IDataObject {

    private int blockID;
    private short facing;

    public SpecialDataObject(BlockSpecial blockSpecial) {
        this.blockID = blockSpecial.getID();
    }

    @Override
    public void load(List<String> values) {
        for (String line : values) {
            if (line.startsWith("d;"
                    + GameUtil.objectTypeAsString(ObjectType.INTEGER) + ";")) {
                this.facing = Short.parseShort(line.substring(8));
            }
        }
    }

    @Override
    public List<String> save() {
        List<String> r = new ArrayList<String>();
        r.add("d;" + GameUtil.objectTypeAsString(ObjectType.INTEGER) + ";"
                + this.facing);
        return r;
    }

    @Override
    public int getObjectID() {
        return this.blockID;
    }

    public Facing getFacing() {
        return Facing.values()[facing];
    }

    public void setFacing(Facing facing) {
        this.facing = (short) facing.ordinal();
    }
}
