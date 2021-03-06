package cz.dat.oots.gui;

import cz.dat.oots.Game;
import cz.dat.oots.sound.SoundManager;

public class GuiScreenMainMenu extends GuiScreen {

    private int width = 400;
    private int height = 114;
    private int overflow = 8;

    public GuiScreenMainMenu(Game game) {
        super(game);
        /*this.objects.add(new GuiObjectRectangle((game.s().windowWidth
				.getValue() - width - overflow) / 2, (game.s().windowHeight
				.getValue() - height - overflow) / 2, (game.s().windowWidth
				.getValue() + width + overflow) / 2, (game.s().windowHeight
				.getValue() + height + overflow) / 2, 0xA0000000));*/

        this.objects.add(new GuiObjectTitleBar(
                (game.s().resolution.width() - width) / 2,
                (game.s().resolution.height() - height) / 2,
                (game.s().resolution.width() + width) / 2,
                ((game.s().resolution.height() - height) / 2) + 30, this.f,
                "Main menu"));

        this.objects.add(new GuiObjectButton(
                (game.s().resolution.width() - width) / 2,
                (game.s().resolution.height() - height) / 2 + 34,
                (game.s().resolution.width() + width) / 2,
                ((game.s().resolution.height() - height) / 2) + 58, this.f,
                "Start game", 0, this));

        this.objects.add(new GuiObjectButton(
                (game.s().resolution.width() - width) / 2,
                (game.s().resolution.height() - height) / 2 + 62,
                (game.s().resolution.width() + width) / 2,
                ((game.s().resolution.height() - height) / 2) + 86, this.f,
                "Options", 1, this));

        this.objects.add(new GuiObjectButton(
                (game.s().resolution.width() - width) / 2,
                (game.s().resolution.height() - height) / 2 + 90,
                (game.s().resolution.width() + width) / 2,
                ((game.s().resolution.height() - height) / 2) + 114,
                this.f, "Exit", 2, this));
    }

    @Override
    public void buttonPress(GuiObjectButton button) {
        if (button.id == 0) {
            SoundManager.getInstance().getMusicProvider()
                    .updateMusic(this.game);
            this.game.displayLoadingScreen();
            this.game.getWorldsManager().startWorld("this_will_be_changable");
        } else if (button.id == 1) {
            this.game.openGuiScreen(new GuiScreenSettings(this));
        } else if (button.id == 2) {
            //this.game.openGuiScreen(new GuiScreenExit(this));
            //Single click exit
            this.game.exit();
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
        SoundManager.getInstance().getMusicProvider().updateMusic(this.game);
    }
}
