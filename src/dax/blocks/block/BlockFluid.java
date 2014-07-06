package dax.blocks.block;

import org.lwjgl.opengl.GL11;

import dax.blocks.TextureManager;
import dax.blocks.render.ChunkDisplayList;

public class BlockFluid extends BlockBasic {

	public BlockFluid(int id) {
		super(id);
		setCullSame(true);
		setOpaque(false);
		setOccluder(false);
		setRenderPass(ChunkDisplayList.PASS_TRANSLUCENT);
	}
	
	@Override
	public void renderTop(int x, int y, int z, boolean xnzn, boolean zn, boolean xpzn, boolean xn, boolean xp, boolean xnzp, boolean zp, boolean xpzp) {
		GL11.glNormal3f(0, 1, 0);
		GL11.glTexCoord2f(TextureManager.getX2(topTexture), TextureManager.getY1(topTexture));
		//GL11.glVertex3f(x+1.0f, y+1f, z+0f); // Top Right Of The Quad (Top)
		addVertexWithAO(x+1, y+0.85f, z, xp, zn, xpzn);
		GL11.glTexCoord2f(TextureManager.getX2(topTexture), TextureManager.getY2(topTexture));
		//GL11.glVertex3f(x+0f, y+1f, z+0f); // Top Left Of The Quad (Top)
		addVertexWithAO(x, y+0.85f, z, xn, zn, xnzn);
		GL11.glTexCoord2f(TextureManager.getX1(topTexture), TextureManager.getY2(topTexture));
		//GL11.glVertex3f(x+0f, y+1f, z+1.0f); // Bottom Left Of The Quad (Top)
		addVertexWithAO(x, y+0.85f, z+1, xn, zp, xnzp);
		GL11.glTexCoord2f(TextureManager.getX1(topTexture), TextureManager.getY1(topTexture));
		//GL11.glVertex3f(x+1.0f, y+1f, z+1.0f); // Bottom Right Of The Quad (Top)
		addVertexWithAO(x+1, y+0.85f, z+1, xp, zp, xpzp);
	}

}
