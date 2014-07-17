package dax.blocks.gui;

import dax.blocks.Game;

public class GuiScreenMainMenu extends GuiScreen {

	int width = 400;
	int height = 114;

	int overflow = 8;

	public GuiScreenMainMenu(Game game) {
		super(game);
		objects.add(new GuiObjectRectangle((Game.settings.windowWidth.getValue() - width - overflow) / 2, (Game.settings.windowHeight.getValue() - height - overflow) / 2, (Game.settings.windowWidth.getValue() + width + overflow) / 2, (Game.settings.windowHeight.getValue() + height + overflow) / 2, 0xA0000000));

		objects.add(new GuiObjectTitleBar((Game.settings.windowWidth.getValue() - width) / 2, (Game.settings.windowHeight.getValue() - height) / 2, (Game.settings.windowWidth.getValue() + width) / 2, ((Game.settings.windowHeight.getValue() - height) / 2) + 30, this.f, "Main menu"));

		objects.add(new GuiObjectButton((Game.settings.windowWidth.getValue() - width) / 2, (Game.settings.windowHeight.getValue() - height) / 2 + 34, (Game.settings.windowWidth.getValue() + width) / 2, ((Game.settings.windowHeight.getValue() - height) / 2) + 58, this.f, "Start game", 0, this));
		objects.add(new GuiObjectButton((Game.settings.windowWidth.getValue() - width) / 2, (Game.settings.windowHeight.getValue() - height) / 2 + 62, (Game.settings.windowWidth.getValue() + width) / 2, ((Game.settings.windowHeight.getValue() - height) / 2) + 86, this.f, "Options", 1, this));
		objects.add(new GuiObjectButton((Game.settings.windowWidth.getValue() - width) / 2, (Game.settings.windowHeight.getValue() - height) / 2 + 90, (Game.settings.windowWidth.getValue() + width) / 2, ((Game.settings.windowHeight.getValue() - height) / 2) + 114, this.f, "Exit", 2, this));
	}

	@Override
	public void buttonPress(GuiObjectButton button) {
		if (button.id == 0) {
			Game.sound.getMusicProvider().updateMusic();
			game.displayLoadingScreen();
			game.makeNewWorld(true,"this_will_be_changable");
		} else if (button.id == 1) {
			game.openGuiScreen(new GuiScreenSettings(this));
		} else if (button.id == 2) {
			game.openGuiScreen(new GuiScreenExit(this));
		}
	}
	
	@Override
	public void sliderUpdate(GuiObjectSlider slider) {
	}

	@Override
	public void buttonChanged(GuiObjectChangingButton button, int line) {
	}

	@Override
	public void onClosing() {
	}

	@Override
	public void onOpening() {
		Game.sound.getMusicProvider().updateMusic();
	}



}
