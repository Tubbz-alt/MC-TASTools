package de.scribble.lp.TASTools.savestates;

import java.util.List;

import de.scribble.lp.TASTools.CommonProxy;
import de.scribble.lp.TASTools.ModLoader;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class SavestateCommandc extends CommandBase{

	@Override
	public String getName() {
		return "savestate";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "command.savestate.usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (sender instanceof EntityPlayer) {
			if (!server.isDedicatedServer()) {
				if (args.length == 1 && args[0].equalsIgnoreCase("save")) {
					ModLoader.NETWORK.sendTo(new SavestatePacket(true,1), (EntityPlayerMP) sender);
				} else if (args.length == 1 && args[0].equalsIgnoreCase("load")) {
					ModLoader.NETWORK.sendTo(new SavestatePacket(false,1), (EntityPlayerMP) sender);
				}
			}else {
				if (args.length == 1 && args[0].equalsIgnoreCase("save")) {
					CommonProxy.getSaveHandler().saveState();
					
				}else if(args.length == 1 && args[0].equalsIgnoreCase("load")) {
					CommonProxy.getSaveHandler().setFlagandShutdown();
				}
			}
		}else {
			if (args.length == 1 && args[0].equalsIgnoreCase("save")&&server.isDedicatedServer()) {
				CommonProxy.logger.info("Making a Savestate! Hold on...");
				CommonProxy.getSaveHandler().saveState();
				CommonProxy.logger.info("Done!");
			}else if(args.length == 1 && args[0].equalsIgnoreCase("load")) {
				CommonProxy.getSaveHandler().setFlagandShutdown();
				CommonProxy.logger.info("Loading a savestate");
			}
		}
		
	}
	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			BlockPos targetPos) {
		if(args.length==1) {
			return getListOfStringsMatchingLastWord(args, new String[] {"save","load"});
		}else {
			return super.getTabCompletions(server, sender, args, targetPos);
		}
	}
}
