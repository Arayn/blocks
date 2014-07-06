package dax.blocks.world.chunk;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

import org.xerial.snappy.Snappy;

import dax.blocks.Coord2D;
import dax.blocks.Game;
import dax.blocks.WorldsManager;
import dax.blocks.world.World;
import dax.blocks.world.WorldInfo;

public class ChunkSaveManager {
	World world;
	ChunkProvider provider;
	String name;
	
	public static final int WORLD_VERSION = 1;
	
	private void deleteFolder(File folder) {
	    File[] files = folder.listFiles();
	    if(files!=null) { 
	        for(File f: files) {
	            if(f.isDirectory()) {
	                deleteFolder(f);
	            } else {
	                f.delete();
	            }
	        }
	    }
	    
	}
	
	public void tryToLoadWorld() {
			File dir = new File(WorldsManager.SAVES_DIR, name);
			
			if (!dir.exists()) {
				dir.mkdir();
			}	
			
			if(dir.exists() && !provider.loadingWorld) {
				deleteFolder(dir);
				return;
			}

			File file = new File(dir, "world" + ".txt");

			if (!file.exists()) {
				Game.console.out("World save not found!");
				return;
			}

			WorldInfo i = Game.worlds.getWorld(name);
			this.world.player.setPos(i.getPlayerX(), i.getPlayerY(), i.getPlayerZ());
			this.world.player.tilt = i.getPlayerTilt();
			this.world.player.heading = i.getPlayerHeading();

			Game.console.out("World info sucessfully loaded!");
	}
	
	public ChunkSaveManager(ChunkProvider provider, String saveName) {
		this.provider = provider;
		this.world = provider.world;
		this.name = saveName;
	}
	
	public boolean isChunkSaved(int cx, int cz) {
		File dir = new File(WorldsManager.SAVES_DIR, name);

		if (!dir.exists()) {
			dir.mkdir();
		}

		File file = new File(dir, "x" + cx + "z" + cz + ".ccf");

		return file.exists();
	}

	public void saveAll() {
		Game.getInstance().displayLoadingScreen("Saving...");
		Iterator<Entry<Coord2D, Chunk>> it = provider.loadedChunks.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Coord2D, Chunk> pairs = it.next();
			Chunk c = (Chunk) pairs.getValue();

			c.deleteAllRenderChunks();

			saveChunk(c);
		}

		WorldInfo i = Game.worlds.getWorld(name);
		i.setPlayerX(this.world.player.posX);
		i.setPlayerY(this.world.player.posY);
		i.setPlayerZ(this.world.player.posZ);
		i.setPlayerTilt(this.world.player.tilt);
		i.setPlayerHeading(this.world.player.heading);
		i.setWorldSeed(this.provider.seed);
		
		i.saveWorldInfo();
		Game.getInstance().closeGuiScreen();
	}

	

	public Chunk loadChunk(int cx, int cz) {
		File dir = new File(WorldsManager.SAVES_DIR, name);
		File file = new File(dir, "x" + cx + "z" + cz + ".ccf");
		byte[] fileData = new byte[(int) file.length()];
		try {
			DataInputStream dis = new DataInputStream(new FileInputStream(file));
			dis.readFully(fileData);
			dis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Chunk c = new Chunk(cx, cz, world);
		try {
			c.blocksBuffer.put(Snappy.uncompressShortArray(fileData));
		} catch (IOException e) {
			e.printStackTrace();
		}

		c.changed = true;

		return c;
	}

	public void saveChunk(Chunk c) {
		if (!c.changed) {
			return;
		}

		try {
			File dir = new File(WorldsManager.SAVES_DIR, name);

			if (!dir.exists()) {
				dir.mkdir();
			}

			File file = new File(dir, "x" + c.x + "z" + c.z + ".ccf");
			FileOutputStream stream = new FileOutputStream(file);
			try {
				stream.write(Snappy.compress(c.blocksBuffer.array()));
			} finally {
				stream.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}