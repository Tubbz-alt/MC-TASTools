package de.scribble.lp.TASTools.duping;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.io.Files;

import de.scribble.lp.TASTools.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class RecordingDupe {
	private Minecraft mc= Minecraft.getMinecraft();
	private StringBuilder output = new StringBuilder();
	private int chestcounter=0;
	private int itemcounter=0;
	
	public static void sendMessage(String msg){
		try{
			Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(msg));
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	
	public void nearbyChest(EntityPlayer player){

		World world = player.getEntityWorld();
		BlockPos playerPos = new BlockPos(player);
		
		output.append("Chest:\n");
		/*Search for chests around the player*/
		for(int x=-5; x<=5; x++){				//x
			for(int y=-5; y<=5; y++){			//y
				for(int z=-5; z<=5; z++){		//z

					if (world.getBlockState(playerPos.add(x, y, z)).getBlock()== Blocks.CHEST||world.getBlockState(playerPos.add(x, y, z)).getBlock()== Blocks.TRAPPED_CHEST){
						TileEntityChest foundchest =(TileEntityChest) world.getTileEntity(playerPos.add(x,y,z));
						chestcounter++;
						//sendMessage(foundchest.getPos().toString().substring(9,foundchest.getPos().toString().length()-1));

						output.append("\t"+foundchest.getPos().toString().substring(9,foundchest.getPos().toString().length()-1)+"\n"); //add a chest to the list
						
						for(int i=0; i<foundchest.getSizeInventory();i++){
							ItemStack item = foundchest.getStackInSlot(i);
							if (Item.getIdFromItem(item.getItem())!=0){
								if(item.hasDisplayName()){
									//sendMessage("Slot;"+i+";"+Item.getIdFromItem(item.getItem())+";("+item.getUnlocalizedName()+");"+item.getCount()+";"+item.getItemDamage()+";"+item.getDisplayName()+";"+item.getEnchantmentTagList()+"\n");
									output.append("\t\tSlot;"+i+";"+Item.getIdFromItem(item.getItem())+";("+item.getUnlocalizedName()+");"+item.getCount()+";"+item.getItemDamage()+";"+item.getDisplayName()+";"+item.getEnchantmentTagList()+"\n");
								}else{
									//sendMessage("Slot;"+i+";"+Item.getIdFromItem(item.getItem())+";("+item.getUnlocalizedName()+");"+item.getCount()+";"+item.getItemDamage()+";null;"+item.getEnchantmentTagList()+"\n");
									output.append("\t\tSlot;"+i+";"+Item.getIdFromItem(item.getItem())+";("+item.getUnlocalizedName()+");"+item.getCount()+";"+item.getItemDamage()+";null;"+item.getEnchantmentTagList()+"\n");
								}
							}
						}
						output.append("\t\t-\n");
					}
				}
			}
		}
		output.append("\t-\n");
	}
	public void nearbyItems(EntityPlayer player){
		World world = player.getEntityWorld();
		BlockPos playerPos = new BlockPos(player);
		
		output.append("Items:"+playerPos.getX()+":"+playerPos.getY()+":"+playerPos.getZ()+"\n");
		
		List<EntityItem> entitylist= world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(playerPos).expandXyz(10.0));
		if(!entitylist.isEmpty()){
			itemcounter=entitylist.size();
			for(int i=0;i<entitylist.size();i++){
				if(entitylist.get(i).getEntityItem().hasDisplayName()){
					output.append("\tItem;"+i+";"+entitylist.get(i).posX+";"+entitylist.get(i).posY+";"+entitylist.get(i).posZ+";"+Item.getIdFromItem(entitylist.get(i).getEntityItem().getItem())+";("+entitylist.get(i).getEntityItem().getUnlocalizedName()+");"+entitylist.get(i).getEntityItem().getCount()+";"+entitylist.get(i).getEntityItem().getItemDamage()+";"+entitylist.get(i).getEntityItem().getDisplayName()+";"+entitylist.get(i).getEntityItem().getEnchantmentTagList()+";"+entitylist.get(i).getAge()+";"+entitylist.get(i).delayBeforeCanPickup+"\n");
				}else{
					output.append("\tItem;"+i+";"+entitylist.get(i).posX+";"+entitylist.get(i).posY+";"+entitylist.get(i).posZ+";"+Item.getIdFromItem(entitylist.get(i).getEntityItem().getItem())+";("+entitylist.get(i).getEntityItem().getUnlocalizedName()+");"+entitylist.get(i).getEntityItem().getCount()+";"+entitylist.get(i).getEntityItem().getItemDamage()+";null;"+entitylist.get(i).getEntityItem().getEnchantmentTagList()+";"+entitylist.get(i).getAge()+";"+entitylist.get(i).delayBeforeCanPickup+"\n");
				}
			}
		}
		output.append("\t-\n");
		
	}
	/**
	 * Kicks off the recording process
	 * @param player
	 */
	public void saveFile(EntityPlayer player){
		File file= new File(mc.mcDataDir, "saves" + File.separator +mc.getIntegratedServer().getFolderName()+File.separator+"latest_dupe.txt");
		output.append("#This file was generated by TASTools, the author is ScribbleLP. To prevent this file being generated, check the tastools.cfg\n");
	
		nearbyChest(player);
		nearbyItems(player);
		
		output.append("END");
		try {
			CommonProxy.logger.info("Saving "+chestcounter+" chest(s) and "+ itemcounter+ " item(s).");
			Files.write(output.toString().getBytes(), file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
