/**
 * 
 */
/**
 * @author santoscm
 *
 */
package com.stratus.mavlink;

public class Attitude extends Message{
	
	private int id = 30;
    private int payload = 28;
    private byte CRC = 39;
    
    @Override
    public void Unpack(byte[] stream, MAVlink mavlink){
    	
        byte[] buffer = new byte[4];
        System.arraycopy(stream, 4, buffer, 0, 4);
        Float roll = dh.float32(buffer);
        mavlink.setMAVRoll(roll);
        
        buffer = new byte[4];
        System.arraycopy(stream, 8, buffer, 0, 4);
        Float pitch = dh.float32(buffer);
        mavlink.setMAVPitch(pitch);
    }
    
    @Override
    public int getID(){
        return id;
    }
    
    @Override
    public int getPayload(){
        return payload;
    }
    
    @Override
    public byte getCRC()
    {
        return CRC;
    }

}