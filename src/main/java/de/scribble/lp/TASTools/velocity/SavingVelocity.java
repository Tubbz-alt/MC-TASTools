package de.scribble.lp.TASTools.velocity;

import java.io.File;
import java.io.IOException;

import com.google.common.io.Files;

import de.scribble.lp.TASTools.CommonProxy;
import net.minecraft.entity.player.EntityPlayer;

public class SavingVelocity {
	private StringBuilder output = new StringBuilder();
	
	public void saveVelocity(EntityPlayer player, File file){

		output.append("#This file was generated by TASTools, the author is ScribbleLP. To prevent this file being generated, check the tastools.cfg\n\n");
		
		output.append("XYZ;"+player.motionX+";"+player.motionY+";"+player.motionZ+"\n");
		output.append("Falldistance;"+player.fallDistance+"\n");
		output.append("END");
		
		try {
			CommonProxy.logger.info("Saving velocity");
			Files.write(output.toString().getBytes(), file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void saveVelocityCustom(double motionX, double motionY, double motionZ, float fallDistance,File file) {


		output.append("#This file was generated by TASTools, the author is ScribbleLP. To prevent this file being generated, check the tastools.cfg\n\n");
		
		output.append("XYZ;"+motionX+";"+motionY+";"+motionZ+"\n");
		output.append("Falldistance;"+fallDistance+"\n");
		output.append("END");
		
		try {
			CommonProxy.logger.info("Saving velocity "+ motionX+" "+motionY+" "+motionZ);
			Files.write(output.toString().getBytes(), file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}