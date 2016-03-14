package com.stratus.mavlink;

public class VFRHud extends Message{
	
	private int id = 74;
    private int payload = 20;
    private byte CRC = 20;
    
    @Override
    public void Unpack(byte[] stream, MAVlink mavlink){
    	
    	byte[] buffer = new byte[4];
        System.arraycopy(stream, 4, buffer, 0, 4);
        String Ground = (dh.float32(buffer)).toString() + " m/s";
        mavlink.setSpeed(Ground);
        
        buffer = new byte[4];
        System.arraycopy(stream, 8, buffer, 0, 4);
        String Altitude = (dh.float32(buffer)).toString() + " m";
    	mavlink.setAltitude(Altitude);
    	
    	buffer = new byte[2];
    	System.arraycopy(stream, 16, buffer, 0, 2);
    	int heading = (dh.int16(buffer));
    	mavlink.setMAVYaw(heading);
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