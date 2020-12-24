package de.scribble.lp.TASTools.freezeV2.networking;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class MovementPacket implements IMessage{
	private  double moX;
	private  double moY;
	private  double moZ;
	
	private  float relX;
	private  float relY;
	private  float relZ;
	public MovementPacket() {
	}
	public MovementPacket(double x, double y, double z, float rx, float ry, float rz) {
		moX=x;
		moY=y;
		moZ=z;
		
		relX=rx;
		relY=ry;
		relZ=rz;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		moX=buf.readDouble();
		moY=buf.readDouble();
		moZ=buf.readDouble();
		
		relX=buf.readFloat();
		relY=buf.readFloat();
		relZ=buf.readFloat();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeDouble(moX);
		buf.writeDouble(moY);
		buf.writeDouble(moZ);
		
		buf.writeFloat(relX);
		buf.writeFloat(relY);
		buf.writeFloat(relZ);
	}
	public double getMoX() {
		return moX;
	}
	public double getMoY() {
		return moY;
	}
	public double getMoZ() {
		return moZ;
	}
	public float getRelX() {
		return relX;
	}
	public float getRelY() {
		return relY;
	}
	public float getRelZ() {
		return relZ;
	}
}
