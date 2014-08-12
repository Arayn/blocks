package dax.blocks.gui.ingame;

import java.awt.Color;

import org.lwjgl.input.Mouse;
import dax.blocks.util.CoordUtil;
import dax.blocks.util.GLHelper;
import dax.blocks.util.Rectangle;

public class SliderControl extends Control {

	private float actualValue;
	private Color backgroundColor;
	private Color sliderColor;
	private boolean horizontal = true;
	private int sliderSize = 5;
	private Rectangle sliderRectangle;

	private boolean isMouseDown = false;
	
	public SliderControl(int relativeX, int relativeY, int width, int height,
			int sliderSize, GuiScreen screen) {
		this(relativeX, relativeY, width, height, 0x80A9A9A9, 0xF1000000,
				sliderSize, screen);
	}

	public SliderControl(int relativeX, int relativeY, int width, int height,
			int backColor, int sliderColor, int sliderSize, GuiScreen screen) {
		super(relativeX, relativeY, width, height, screen);
		/*this.backgroundColor = GameUtil.getColorFromInt(backColor);
		this.sliderColor = GameUtil.getColorFromInt(sliderColor);*/
		this.backgroundColor = Color.RED;
		this.sliderColor = Color.BLUE;
		this.sliderRectangle = new Rectangle(0, 0, 0, 0);
		this.actualValue = 0.5f;
		this.sliderSize = sliderSize;
	}

	public void setSliderSize(int size) {
		if(size >= 1
				&& size <= ((this.horizontal ? super.rectangle.getWidth()
						: super.rectangle.getHeight()) / 2)) {
			this.sliderSize = size;
		}
	}

	public void setHorizontal(boolean isHorizontal) {
		this.horizontal = isHorizontal;
	}

	public void setValue(float value) {
		if(value >= 0 && value <= 1) {
			this.actualValue = value;
		}
	}

	public float getValue() {
		return this.actualValue;
	}

	@Override
	public void onRenderTick(float ptt) {
		super.onRenderTick(ptt);
		int mouseX = Mouse.getX();
		int mouseY = CoordUtil.getProperMouseY(Mouse.getY());
		
		this.updateMouse(mouseX, mouseY);
		this.updateSliderRectangle();
	}

	@Override
	public void render() {
		GLHelper.drawRectangle(this.backgroundColor.getRed(),
				this.backgroundColor.getGreen(),
				this.backgroundColor.getBlue(),
				this.backgroundColor.getAlpha(), super.rectangle.getX(),
				super.rectangle.getTopRight(), super.rectangle.getY(),
				super.rectangle.getBottomLeft());

		GLHelper.drawRectangle(this.sliderColor.getRed(),
				this.sliderColor.getGreen(), this.sliderColor.getBlue(),
				this.sliderColor.getAlpha(), this.sliderRectangle.getX(),
				this.sliderRectangle.getTopRight(),
				this.sliderRectangle.getY(),
				this.sliderRectangle.getBottomLeft());
	}

	private void updateMouse(int mX, int mY) {
		boolean mouseDown = Mouse.isButtonDown(0);
		
		if(mouseDown) {
			if(this.horizontal) {
				
				int width = super.rectangle.getTopRight() - this.rectangle.getX() - this.sliderSize;
				int clickedX = mX - super.rectangle.getX() - this.sliderSize / 2 + 1;
				this.actualValue = ((float)(clickedX) / (float)width);
				this.actualValue = (float) (0.01 * Math.floor(this.actualValue * 100.0));
				
				if(this.actualValue > 1) {
					this.actualValue = 1;
				}
				
				if(this.actualValue < 0) {
					this.actualValue = 0;
				}
			}
		}
		
	}
	
	private void updateSliderRectangle() {

		float val = this.actualValue;

		int minPos = this.horizontal ? super.rectangle.getX() : super.rectangle
				.getY();
		int range = (this.horizontal ? super.rectangle.getWidth()
				: super.rectangle.getHeight()) - this.sliderSize;
		int offset = (int) Math.round(range * val);

		this.sliderRectangle
				.set(this.horizontal ? (minPos + offset) : super.rectangle
						.getX(), this.horizontal ? super.rectangle.getY()
						: (minPos + offset), this.horizontal ? this.sliderSize
						: super.rectangle.getWidth(),
						this.horizontal ? super.rectangle.getHeight()
								: this.sliderSize);
	}
}
