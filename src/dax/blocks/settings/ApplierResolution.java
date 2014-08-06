package dax.blocks.settings;

import dax.blocks.GLHelper;
import dax.blocks.Game;
import dax.blocks.gui.ingame.GuiManager;

public class ApplierResolution extends Applier {

	@Override
	public boolean apply(Object val) {
		int width = Settings.getInstance().windowWidth.getValue();
		int height = Settings.getInstance().windowHeight.getValue();

		if(this.applyingObject == Settings.getInstance().windowWidth) {
			width = (Integer) val;
		}

		if(this.applyingObject == Settings.getInstance().windowHeight) {
			height = (Integer) val;
		}

		if(width > 200 && height > 200) {

			GLHelper.setDisplayMode(width, height, Settings.getInstance().fullscreen.getValue());

			if (Game.getInstance().guiScreen != null) {
				Game.getInstance().closeGuiScreen();
			}
		}
		
		Settings.getInstance().resolution.setValue(width + "x" + height, false);

		if(GuiManager.getInstance().getCurrentScreen() != null)
			GuiManager.getInstance().getCurrentScreen().updateCenteredPosition(width, height);
		
		return true;

	}
}
