package de.scribble.lp.TASTools.savestates;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.io.Files;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.scribble.lp.TASTools.CommonProxy;
import de.scribble.lp.TASTools.ModLoader;
import de.scribble.lp.TASTools.freeze.FreezeHandler;
import de.scribble.lp.TASTools.freeze.FreezePacket;
import de.scribble.lp.TASTools.velocity.SavingVelocity;
import de.scribble.lp.TASTools.velocity.VelocityEvents;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldServer;

@SideOnly(Side.SERVER)
public class SavestateHandlerServer {
	private boolean isSaving;
	protected static File currentworldfolder;
	protected static File targetsavefolder;
	
	public void saveState() {
		if(FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer()) {
			if(!isSaving) {
				isSaving=true;
				MinecraftServer server=FMLCommonHandler.instance().getMinecraftServerInstance();
				SavestateHandlerServer.currentworldfolder = new File(FMLCommonHandler.instance().getSavesDirectory().getPath()
						+ File.separator + ModLoader.getLevelname());
				targetsavefolder = null;
				if (!FreezeHandler.isServerFrozen()) {
					FreezeHandler.startFreezeServer();
					ModLoader.NETWORK.sendToAll(new FreezePacket(true));
				}
				ModLoader.NETWORK.sendToAll(new SavestatePacket());
				int i = 1;
				while (i <= 300) {
					if (i == 300) {
						CommonProxy.logger.error(
								"Couldn't make a savestate, there are too many savestates in the target directory");
						return;
					}
					if (i > 300) {
						CommonProxy.logger.error(
								"Aborting saving due to savestate count being greater than 300 for safety reasons");
						return;
					}
					targetsavefolder = new File(FMLCommonHandler.instance().getSavesDirectory().getAbsolutePath()
							+ File.separator + "savestates" + File.separator + ModLoader.getLevelname() + "-Savestate"
							+ Integer.toString(i));
					if (!targetsavefolder.exists()) {
						break;
					}
					i++;
				}
				if (VelocityEvents.velocityenabledServer) {
					List<EntityPlayerMP> players = server.getConfigurationManager().playerEntityList;
					for (int o = 0; o < players.size(); o++) {
						for (int e = 0; e < FreezeHandler.entity.size(); e++) {
							if (FreezeHandler.entity.get(e).getPlayername().equals(players.get(o).getDisplayName())) {
								new SavingVelocity().saveVelocityCustom(FreezeHandler.entity.get(o).getMotionX(),
										FreezeHandler.entity.get(o).getMotionY(),
										FreezeHandler.entity.get(o).getMotionZ(), FreezeHandler.entity.get(o).getFalldistance(), new File(currentworldfolder.getPath()
												+ File.separator + players.get(o).getDisplayName() + "_velocity.txt"));
							}
						}
					}
				}
				try {
					int[] incr = getInfoValues(getInfoFile(ModLoader.getLevelname()));
					if (incr[0] == 0) {
						saveInfo(getInfoFile(ModLoader.getLevelname()), null);
					} else {
						incr[0]++;
						saveInfo(getInfoFile(ModLoader.getLevelname()), incr);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().saveAllPlayerData();
				saveAll(server);
				try {
					copyDirectory(currentworldfolder, targetsavefolder, new String[] {" "});
					
				} catch (IOException e) {
					CommonProxy.logger.error("Could not copy the directory "+currentworldfolder.getPath()+" to "+targetsavefolder.getPath()+" for some reason (Savestate save)");
					e.printStackTrace();
					return;
				}
				isSaving=false;
				ModLoader.NETWORK.sendToAll(new SavestatePacket());
			}
		}
	}

	public void setFlagandShutdown() {
		if(FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer()) {
			if (!isSaving) {
				
				File targetsavefolder=null;
				//getting latest savestate
				int i=1;
				while(i<=300) {
					targetsavefolder = new File(FMLCommonHandler.instance().getSavesDirectory().getPath() + File.separator + "savestates"+ File.separator + ModLoader.getLevelname() + "-Savestate" + Integer.toString(i));
					if (!targetsavefolder.exists()) {
						if(i-1==0) {
							CommonProxy.logger.info("Couldn't find a valid savestate, abort loading the savestate!");
							return;
						}
						if(i>300) {
							CommonProxy.logger.error("Too many savestates found. Aborting loading for safety reasons");
							return;
						}
						targetsavefolder = new File(FMLCommonHandler.instance().getSavesDirectory().getPath() + File.separator + "savestates"+ File.separator + ModLoader.getLevelname() + "-Savestate" + Integer.toString(i-1));
						break;
					}
					i++;
				}
				
				try {
					int[] incr=getInfoValues(getInfoFile(ModLoader.getLevelname()));
					incr[1]++;
					saveInfo(getInfoFile(ModLoader.getLevelname()), incr);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				CommonProxy.serverconfig.get("Savestate","LoadSavestate", false, "This is used for loading a Savestate. When entering /savestate load, this will be set to true, and the server will delete the current world and copy the latest savestate when starting.").set(true);
				CommonProxy.serverconfig.save();
				FMLCommonHandler.instance().getMinecraftServerInstance().initiateShutdown();
			}
		}
	}
	private void copyDirectory(File sourceLocation, File targetLocation, String[] ignore) throws IOException
	    {
	        if (sourceLocation.isDirectory())
	        {
	            if (!targetLocation.exists())
	                targetLocation.mkdirs();

	            String[] children = sourceLocation.list();
	            for (int i = 0; i < children.length; i++)
	            {
	                boolean ignored = false;	
	                for (String str : ignore)
	                    if (str.equals(children[i]))
	                    {
	                        ignored = true;
	                        break;
	                    }

	                if (!ignored)
	                    copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]), ignore);
	            }
	        }
	        else
	        {
	            boolean ignored = false;
	            for (String str : ignore)
	                if (str.equals(sourceLocation.getName()))
	                {
	                    ignored = true;
	                    break;
	                }

	            if (!ignored)
	            {
	                InputStream in = new FileInputStream(sourceLocation);
	                OutputStream out = new FileOutputStream(targetLocation);

	                // Copy the bits from instream to outstream
	                byte[] buf = new byte[1024];
	                int len;
	                while ((len = in.read(buf)) > 0)
	                    out.write(buf, 0, len);

	                in.close();
	                out.close();
	            }
	        }
	    }

	/**
	 * Delete directory contents recursively. Leaves the specified starting
	 * directory empty. Ignores files / dirs listed in "ignore" array.
	 * 
	 * @param dir    directory to delete
	 * @param ignore ignored files
	 * @return true on success
	 */
	private boolean deleteDirContents(File dir, String[] ignore) {

		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean ignored = false;
				for (String str : ignore)
					if (str.equals(children[i])) {
						ignored = true;
						break;
					}

				if (!ignored) {
					boolean success = deleteDirContents(new File(dir, children[i]), ignore);
					if (!success)
						return false;
				}
			}
		} else {
			dir.delete();
		}
		return true;
	}

	public File getInfoFile(String worldname) {
		File file = new File(FMLCommonHandler.instance().getSavesDirectory().getPath() + File.separator + "savestates"
				+ File.separator + ModLoader.getLevelname() + "-info.txt");
		return file;
	}
	
    public int[] getInfoValues(File file) throws IOException {
    	int[] out = {0,0};
    	if (file.exists()){
			try {
				BufferedReader buff = new BufferedReader(new FileReader(file));
				String s;
				int i = 0;
				while (i < 100) {
					s = buff.readLine();
					if (s.equalsIgnoreCase("END")) {
						break;
					} else if (s.startsWith("#")) {
						continue;
					} else if (s.startsWith("Total Savestates")) {
						String[] valls = s.split("=");
						out[0] = Integer.parseInt(valls[1]);
					} else if (s.startsWith("Total Rerecords")) {
						String[] valls = s.split("=");
						out[1] = Integer.parseInt(valls[1]);
					}
					i++;
				}
				buff.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
    	}
    	return out;
    }
	 
	public void saveInfo(File file, @Nullable int[] values) {
		StringBuilder output = new StringBuilder();
		output.append("#This file was generated by TASTools and diplays info about the usage of savestates!\n\n");
		if (values == null) {
			output.append("Total Savestates=1\nTotal Rerecords=0\nEND");
		} else {
			output.append("Total Savestates=" + Integer.toString(values[0]) + "\nTotal Rerecords="
					+ Integer.toString(values[1]) + "\nEND");
		}
		try {
			Files.write(output.toString().getBytes(), file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void loadSavestateOnServerStart() {
		CommonProxy.logger.info("Start loading the savestate");
		File currentworldfolder = new File(FMLCommonHandler.instance().getSavesDirectory().getPath() + File.separator + ModLoader.getLevelname());
		File targetsavefolder=null;
		//getting latest savestate
		int i=1;
		while(i<=300) {
			targetsavefolder = new File(FMLCommonHandler.instance().getSavesDirectory().getPath() + File.separator + "savestates"+ File.separator + ModLoader.getLevelname() + "-Savestate" + Integer.toString(i));
			if (!targetsavefolder.exists()) {
				if(i-1==0) {
					CommonProxy.logger.info("Couldn't find a valid savestate, abort loading the savestate!");
					return;
				}
				if(i>300) {
					CommonProxy.logger.error("Too many savestates found. Aborting loading for safety reasons");
					return;
				}
				targetsavefolder = new File(FMLCommonHandler.instance().getSavesDirectory().getPath() + File.separator + "savestates"+ File.separator + ModLoader.getLevelname() + "-Savestate" + Integer.toString(i-1));
				break;
			}
			i++;
		}
		deleteDirContents(currentworldfolder, new String[] { " " });
		try {
			copyDirectory(targetsavefolder, currentworldfolder, new String[] { " " });
		} catch (IOException e) {
			CommonProxy.logger.error("Could not copy the directory " + currentworldfolder.getPath() + " to "
					+ targetsavefolder.getPath() + " for some reason (Savestate load)");
			e.printStackTrace();
		}
		CommonProxy.logger.info("Done");
	}
	
	private void saveAll(MinecraftServer minecraftserver) {
		try {
			int i;
			WorldServer worldserver;
			boolean flag;

			for (i = 0; i < minecraftserver.worldServers.length; ++i) {
				if (minecraftserver.worldServers[i] != null) {
					worldserver = minecraftserver.worldServers[i];
					flag = worldserver.levelSaving;
					worldserver.levelSaving = false;
					worldserver.saveAllChunks(true, (IProgressUpdate) null);
					worldserver.levelSaving = flag;
				}
			}
		} catch (MinecraftException e) {
			CommonProxy.logger.error("Something went wrong while saving chunks on the server");
			CommonProxy.logger.catching(e);
		}
	}
	
}
