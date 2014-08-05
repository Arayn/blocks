package dax.blocks;

import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ListIterator;
import java.util.Locale;

import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.openal.AL;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;

import dax.blocks.auth.AuthManager;
import dax.blocks.block.Block;
import dax.blocks.console.Console;
import dax.blocks.gui.GuiObjectBlank;
import dax.blocks.gui.GuiScreen;
import dax.blocks.gui.GuiScreenLoading;
import dax.blocks.gui.GuiScreenMainMenu;
import dax.blocks.gui.GuiScreenMenu;
import dax.blocks.gui.ingame.GuiManager;
import dax.blocks.model.ModelManager;
import dax.blocks.profiler.Profiler;
import dax.blocks.profiler.Section;
import dax.blocks.render.ChunkRendererDisplayList;
import dax.blocks.render.IChunkRenderer;
import dax.blocks.render.IRenderable;
import dax.blocks.render.RenderEngine;
import dax.blocks.settings.Keyconfig;
import dax.blocks.settings.Settings;
import dax.blocks.sound.SoundManager;
import dax.blocks.world.World;

public class Game implements Runnable {

	public static Settings settings = new Settings();
	public static Console console = new Console();
	public static WorldsManager worlds = new WorldsManager();
	public static SoundManager sound = new SoundManager();
	public static ModelManager models = new ModelManager();
	public static GuiManager ingameGuiManager = new GuiManager();
	
	public static final String TITLE = Start.GAME_NAME + " v" + Start.GAME_VERSION;
	
	private File configFile = new File("settings.txt");
	
	public boolean showbg = false;
	public boolean consoleOpen = false;
	public boolean ingame = false;

	public RenderEngine renderEngine;
	public GuiScreen guiScreen;
	public TrueTypeFont font;
	public World world;
	public IChunkRenderer chunkRenderer = new ChunkRendererDisplayList();
	public AuthManager authManager;
	
	
	private static final int TPS = 20;
	private static final double TICK_TIME = 1.0D / TPS;
	private int ticks = 0;
	private String ticksString = "N/A";

	private float animationProgress = 0;
	private float lastProgress = 0;
	private long lastFrame;
	private int fpsCounter;
	private int fps = 0;
	private long lastFPS;
	private int vertices = 0;
	private float lastFov = 0;

	private String loginString = "Unlogged";
	private String versionString = "version " + Start.GAME_VERSION;
	
	private Profiler profiler = new Profiler();
	
	private static Game instance = null;

	private Game() {
		Game.instance = this;
	}

	public static Game getInstance() {
		if(Game.instance == null) {
			new Game();
		}

		return Game.instance;
	}
	
	public Profiler getProfiler() {
		return this.profiler;
	}

	//.... RUN METHODS ....
	
	@Override
	public void run() {
		try {
			if(!this.configFile.exists()) {
				this.configFile.createNewFile();
				Game.settings.saveToFile(configFile);
			}

			Game.settings.loadFromFile(this.configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

		GLHelper.setDisplayMode(Game.settings.windowWidth.getValue(), Game.settings.windowHeight.getValue(), Game.settings.fullscreen.getValue());
		this.init();
		this.load(true);

		this.renderEngine = new RenderEngine(Game.settings.enable_shaders.getValue());

		long time = System.nanoTime();
		long lastTime = time;
		long lastInfo = time;

		while(!Display.isCloseRequested()) {
			time = System.nanoTime();
			while(time - lastTime >= Game.TICK_TIME * 1000000000) {
				this.ticks++;
				this.profiler.tick.start();
				this.onTick();
				this.profiler.tick.end();
				lastTime += Game.TICK_TIME * 1000000000;
			}

			if(time - lastInfo >= 1000000000) {
				lastInfo += 1000000000;
				this.ticksString = "Ticks: " + ticks;
				this.ticks = 0;
				Game.sound.updatePlaying();
				Game.sound.getMusicProvider().updateMusic();
			}
			
			this.profiler.render.start();
			float partialTickTime = (time - lastTime) / ((float) TICK_TIME * 1000000000);

			this.onRender(partialTickTime);
			this.render(partialTickTime);

			Display.update();
			this.profiler.render.end();
			
			if(Game.settings.fps_limit.getValue() > 0)
				Display.sync(Game.settings.fps_limit.getValue());
		}

		this.exit();
	}

	public void init() {
		GLHelper.initGL(Game.settings.windowWidth.getValue(), Game.settings.windowHeight.getValue());
		Display.setTitle(TITLE);
	}

	public void exit() {
		if(this.ingame) {
			this.exitGame();
		}
		Game.sound.shutdown();

		Display.destroy();
		AL.destroy();

		try {
			Game.settings.saveToFile(this.configFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		System.exit(0);
	}

	public void load(boolean toMenu) {
		FontManager.load();
		this.font = FontManager.text;
		this.displayLoadingScreen("Loading textures...");
		TextureManager.load();
		this.displayLoadingScreen("Loading models...");
		Game.models.load();
		this.displayLoadingScreen("Loading keyconfig...");
		Keyconfig.load();
		this.displayLoadingScreen("Loading sounds...");

		this.lastFPS = getTime();

		this.showbg = true;

		if(toMenu) {
			this.openGuiScreen(new GuiScreenMainMenu(this));
		} else {
			this.closeGuiScreen();
			this.ingame = true;
		}
	}

	public void createFont() {
		Font awtFont = new Font("Arial", Font.BOLD, 10);
		this.font = new TrueTypeFont(awtFont, false);
	}

	
	//.... GAME METHODS ....

	public void makeNewWorld(boolean load, String name) {
		this.ingame = true;
		GLHelper.updateFiltering(Game.settings.linear_filtering.getValue());
		this.world = new World(Game.settings.tree_generation.getValue(), this, load, name);
		this.closeGuiScreen();
		//ingame = true;
	}

	public void exitGame() {
		this.world.saveAllChunks();
		this.world = null;
		this.renderEngine = new RenderEngine(Game.settings.enable_shaders.getValue());
		this.ingame = false;
		this.openGuiScreen(new GuiScreenMainMenu(this));
	}
	
	//.... RENDER/UPDATE METHODS ....

	public void onTick() {

		while(Keyboard.next()) {
			if(Keyboard.getEventKeyState()) {
				if(Keyboard.getEventKey() == Keyconfig.screenshot) {
					GameUtil.screenshot();
				}
				
				if(this.consoleOpen) {
					Game.console.charTyped(Keyboard.getEventCharacter(),
							Keyboard.getEventKey());
				}


				if(Keyconfig.isDownEvent(Keyconfig.console)) {
					if(!this.consoleOpen) {
						Game.console.clearInput();
						this.consoleOpen = true;
					}		
				}
				
				if(Keyboard.getEventKey() == Keyconfig.fullscreen
						&& this.guiScreen == null && !this.consoleOpen) {
					this.toggleFullscreen();
				}

				if(Keyboard.getEventKey() == Keyconfig.exit) {
					if(!this.consoleOpen) {
						if(Game.ingameGuiManager.isOpened()) {
							Game.ingameGuiManager.closeScreen();
						} else {
							if(this.guiScreen != null) {
								this.closeGuiScreen();
							} else {
								this.openGuiScreen(new GuiScreenMenu(this));
							}
						}
					} else {
						this.consoleOpen = false;
					}
				}

			}
		}
		
		if(Keyboard.isKeyDown(Keyconfig.zoom)) {
			if(this.lastFov == 0) {
				this.lastFov = Game.settings.fov.getValue();
				Game.settings.fov.setValue(15f);
			}
		} else {
			if(this.lastFov > 0) {
				Game.settings.fov.setValue(lastFov);
				this.lastFov = 0;
			}
		}

		if(!this.ingame && this.guiScreen == null) {
			this.openGuiScreen(new GuiScreenMainMenu(this));
		}

		if(this.guiScreen == null && this.ingame && !this.consoleOpen) {
			this.world.onTick();
		} else if(this.ingame) {
			this.world.menuUpdate();
		}

		this.lastProgress = this.animationProgress;

		this.animationProgress += this.consoleOpen ? 0.150f : -0.150f;

		if(this.animationProgress < 0) {
			this.animationProgress = 0;
		}

		if(this.animationProgress > 1) {
			this.animationProgress = 1;
		}
	}

	public void onRender(float ptt) {
		if(this.guiScreen == null && this.ingame && !this.consoleOpen) {
			this.world.onRenderTick(ptt);
		}
	}
	
	public void render(float ptt) {

		float pttbackup = ptt;

		if(this.guiScreen != null || this.consoleOpen) {
			ptt = 1;
		}

		// Clear old frame
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		if(this.ingame) {

			TextureManager.atlas.bind();

			this.renderEngine.renderWorld(this.world, ptt);
			
			GLHelper.setOrtho(Game.settings.windowWidth.getValue(), Game.settings.windowHeight.getValue());
			
			this.world.renderGui(ptt);
		
			this.renderOverlay();

			this.updateFPS();
		}

		GLHelper.setOrtho(Game.settings.windowWidth.getValue(), Game.settings.windowHeight.getValue());

		this.renderGuiScreen(ptt);

		this.renderConsole(pttbackup);

		GLHelper.setPerspective(Game.settings.windowWidth.getValue(), Game.settings.windowHeight.getValue());

	}
	
	public void renderGuiScreen(float ptt) {
		if(this.guiScreen != null) {
			this.guiScreen.update();
			if(this.guiScreen != null) {

				if(!this.ingame) {
					if(TextureManager.menuBg != null && this.showbg) {
						GLHelper.drawTexture(TextureManager.menuBg, 0, Game.settings.windowWidth.getValue(),
								0, Game.settings.windowWidth.getValue());
					} else {
						GLHelper.drawRectangle(0.2f, 0.2f, 0.2f, 0, Game.settings.windowWidth.getValue(), 0,
								Game.settings.windowWidth.getValue());
					}

					if(TextureManager.logo != null && this.showbg) {
						GLHelper.drawTexture(
								TextureManager.logo, 
								(Game.settings.windowWidth.getValue() / 2) - (TextureManager.logo.getImageWidth() / 2), 
								32
						);

						this.font.drawString(5, Game.settings.windowHeight.getValue() - this.font.getHeight(),
								this.versionString);
						this.font.drawString(
								15 + this.font.getWidth(this.versionString), 
								Game.settings.windowHeight.getValue() - font.getHeight(), 
								this.loginString, 
								this.authManager.isAuthenticated() ? Color.white : Color.red
						);
					}
					this.guiScreen.render();
				}

				this.guiScreen.render();
			}
		}
	}
	
	public void renderConsole(float ptt) {
		GL11.glPushMatrix();

		int cHeight = this.settings.consoleHeight.getValue();

		float lerp = this.lastProgress + (this.animationProgress - this.lastProgress) * ptt;

		GL11.glTranslatef(0, -((1 - lerp) * cHeight), 0);

		if(this.lastProgress > 0) {
			GuiObjectBlank gui = new GuiObjectBlank();
			gui.drawRect(0, 0, Game.settings.windowWidth.getValue(), cHeight, 0xD0000000);
			gui.drawRect(
					0, 
					cHeight - this.font.getLineHeight(), 
					Game.settings.windowWidth.getValue(),
					cHeight, 
					0x500030A0
			);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			String cursor = (this.ticks % Game.TPS >= Game.TPS / 2) ? "_" : "";
			this.font.drawString(0, cHeight - this.font.getLineHeight(), 
					"> " + this.console.currentCommand + cursor);
			String info = Game.TITLE;
			this.font.drawString(Game.settings.windowWidth.getValue() - this.font.getWidth(info) - 2,
					cHeight - this.font.getLineHeight() * 2, info,
					new org.newdawn.slick.Color(120, 120, 120));

			ListIterator<String> li = this.console.lines.listIterator(
					console.lines.size());

			int offset = 0;

			while(li.hasPrevious()) {
				offset += this.font.getLineHeight();

				this.font.drawString(
						0, 
						cHeight - this.font.getLineHeight() - offset - this.console.getTranslation(), 
						li.previous(),
						new org.newdawn.slick.Color(200, 200, 200)
				);
			}

		}

		GL11.glPopMatrix();
	}
	
	public void renderOverlay() {
		if(Game.settings.debug.getValue()) {
			GL11.glLineWidth(1);
			
			GL11.glBegin(GL11.GL_LINES);
			
			int offset = Display.getWidth() - Section.MAX_RECORDS;
			
			float[] tick = this.profiler.tick.getTimes();
			float[] render = this.profiler.render.getTimes();
			float[] build = this.profiler.build.getTimes();
			
			for(int i = 0; i < Section.MAX_RECORDS; i++) {
				GL11.glColor4f(0, 1, 0, 1.0f);
				GL11.glVertex2f(offset+i, Display.getHeight());
				GL11.glVertex2f(offset+i, Display.getHeight()-tick[i]*10);
				
				GL11.glColor4f(0, 0, 1, 0.5f);
				GL11.glVertex2f(offset+i, Display.getHeight());
				GL11.glVertex2f(offset+i, Display.getHeight()-render[i]*10);
				
				GL11.glColor4f(1, 0, 0, 0.4f);
				GL11.glVertex2f(offset+i, Display.getHeight());
				GL11.glVertex2f(offset+i, Display.getHeight()-build[i]*10);
			}
			
			float avgTick = this.profiler.tick.avg();
			float avgRender = this.profiler.render.avg();
			float avgBuild = this.profiler.build.avg();
			
			GL11.glColor4f(0.3f, 1.0f, 0, 1.0f);
			GL11.glVertex2f(Display.getWidth()-Section.MAX_RECORDS, Display.getHeight()-avgTick*10);
			GL11.glVertex2f(Display.getWidth(), Display.getHeight()-avgTick*10);
			
			GL11.glColor4f(0.3f, 0, 1.0f, 1.0f);
			GL11.glVertex2f(Display.getWidth()-Section.MAX_RECORDS, Display.getHeight()-avgRender*10);
			GL11.glVertex2f(Display.getWidth(), Display.getHeight()-avgRender*10);
			
			GL11.glColor4f(1.0f, 0, 0.3f, 1.0f);
			GL11.glVertex2f(Display.getWidth()-Section.MAX_RECORDS, Display.getHeight()-avgBuild*10);
			GL11.glVertex2f(Display.getWidth(), Display.getHeight()-avgBuild*10);
			
			GL11.glEnd(); 
			
			String tickText = "avg tick " + String.format(Locale.ENGLISH, "%.2f", avgTick) + "ms";
			FontManager.text.drawString(
					offset - FontManager.text.getWidth(tickText) - 2, 
					(int)(Display.getHeight() - avgTick * 10 -FontManager.text.getLineHeight() * 0.75f), 
					tickText
			);
			
			String renderText = "avg render " + String.format(Locale.ENGLISH, "%.2f", avgRender) + "ms";
			FontManager.text.drawString(
					offset - FontManager.text.getWidth(renderText) - 2, 
					(int)(Display.getHeight() - avgRender * 10 - FontManager.text.getLineHeight() * 0.75f), 
					renderText
			);
			
			String buildText = "avg build" + String.format(Locale.ENGLISH, "%.2f", avgBuild) + "ms";
			FontManager.text.drawString(
					offset - FontManager.text.getWidth(buildText) - 2, 
					(int)(Display.getHeight() - avgBuild * 10 - FontManager.text.getLineHeight() * 0.75f), 
					buildText
			);
		}
		
		Block b = Block.getBlock(world.getPlayer().getSelectedBlockID());
		int textureid = b.sideTexture;

		GLHelper.drawFromAtlas(textureid, 25, 75, Game.settings.windowHeight.getValue() - 75, Game.settings.windowHeight.getValue() - 25);

		Runtime runtime = Runtime.getRuntime();

		long allocatedMemory = runtime.totalMemory();
		long freeMemory = runtime.freeMemory();

		String fpsString = "FPS: " + this.fps + ", " + this.ticksString;
		int stringWidth = this.font.getWidth(fpsString);
		this.font.drawString(Game.settings.windowWidth.getValue() - stringWidth - 2, this.font.getHeight() * 2,
				fpsString);

		this.font.drawString(2, 0, "X Position: " + this.world.getPlayer().getPosX() + " (laX: " + this.world.getPlayer().getLookingAtX() + ")");
		this.font.drawString(2, this.font.getHeight(),
				"Y Position: " + this.world.getPlayer().getPosY() + " (laY: " + this.world.getPlayer().getLookingAtY() + ")");
		this.font.drawString(2, this.font.getHeight() * 2,
				"Z Position: " + this.world.getPlayer().getPosZ() + " (laZ: " + this.world.getPlayer().getLookingAtZ() + ")");
		this.font.drawString(
				2,
				this.font.getHeight() * 3,
				"Biome: "
						+ this.world.getChunkProvider().getBiomeAtLocation(
								(int) this.world.getPlayer().getPosX(),
								(int) this.world.getPlayer().getPosZ()).getName());
		this.font.drawString(2, this.font.getHeight() * 4, "Lives: "
				+ ((int) (this.world.getPlayer().getLifes() * 100)));

		String memory = "Used memory: "
				+ (allocatedMemory / (1024 * 1024) - freeMemory / (1024 * 1024))
				+ "MB" + "/" + allocatedMemory / (1024 * 1024) + "MB";
		int memoryWidth = this.font.getWidth(memory);
		this.font.drawString(Game.settings.windowWidth.getValue() - memoryWidth - 2, 0, memory);

		String chunks = "Chunks drawn: " + this.renderEngine.chunksDrawn + "/"
				+ this.renderEngine.chunksLoaded;
		this.font.drawString(Game.settings.windowWidth.getValue() - this.font.getWidth(chunks) - 2, this.font.getHeight(),
				chunks);

		if(this.world.getChunkProvider().loading) {
			this.font.drawString(Game.settings.windowWidth.getValue() - this.font.getWidth("Loading chunks...") - 2,
					Game.settings.windowHeight.getValue() - this.font.getHeight(), "Loading chunks...",
					new org.newdawn.slick.Color(255, 255, 255, 255));
		}

		if(this.renderEngine.building) {
			this.font.drawString(Game.settings.windowWidth.getValue() - this.font.getWidth("Building chunks...") - 2,
					Game.settings.windowHeight.getValue() - this.font.getHeight()
							* (this.world.getChunkProvider().loading ? 2 : 1),
					"Building chunks...", new org.newdawn.slick.Color(255, 255,
							255, 255));
		}

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		
		GLHelper.drawLine(
				Game.settings.windowWidth.getValue() / 2, 
				Game.settings.windowWidth.getValue() / 2, 
				(Game.settings.windowHeight.getValue() / 2) - 10, 
				(Game.settings.windowHeight.getValue() / 2) + 10, 
				2, 0, 0, 0, 0.5f);
		GLHelper.drawLine(
				(Game.settings.windowWidth.getValue() / 2) - 10, 
				(Game.settings.windowWidth.getValue() / 2) + 10, 
				Game.settings.windowHeight.getValue() / 2, 
				Game.settings.windowHeight.getValue() / 2, 
				2, 0, 0, 0, 0.5f);

	}


	//.... GUI METHODS ....

	public void openGuiScreen(GuiScreen scr) {
		if(this.guiScreen != null)
			this.guiScreen.onClosing();
		this.guiScreen = scr;
		scr.onOpening();
		Mouse.setGrabbed(false);
	}

	public void closeGuiScreen() {
		this.guiScreen.onClosing();
		this.guiScreen = null;

		if(this.consoleOpen) {
			this.consoleOpen = false;
		}

		Mouse.setGrabbed(true);
	}

	public void displayLoadingScreen(String text) {
		// isIngame = false;
		this.openGuiScreen(new GuiScreenLoading(this, text));
		this.render(0);
		Display.update();
	}

	public void displayLoadingScreen() {
		// isIngame = false;
		this.openGuiScreen(new GuiScreenLoading(this));
		this.render(0);
		Display.update();
	}


	//.... NEMAMZDANIKCEMU METHODS ....

	public long getTime() {
		return (Sys.getTime() * 1000) / Sys.getTimerResolution();
	}

	public void updateFPS() {
		if(this.getTime() - this.lastFPS > 1000) {
			this.fps = fpsCounter;
			this.fpsCounter = 0;
			this.lastFPS += 1000;
		}
		this.fpsCounter++;
	}

	public void toggleFullscreen() {
		Game.settings.fullscreen.setValue(!Game.settings.fullscreen.getValue());
	}


	//.... LOGIN METHODS ....
	public void doLogin(String userName, String password, String token) {
		this.authManager = new AuthManager(userName, password, token);
	
		if(this.authManager.isAuthenticated()) {
			this.loginString = userName + " is logged in with token " + token;
		} else {
			this.loginString = "Bad login for " + userName;
		}
	}
	
	public void dummyLogin() {
		this.authManager = new AuthManager();
		this.authManager.setDummyName("Player");
		this.loginString = "User is not logged in, using name Player";
	}

}
