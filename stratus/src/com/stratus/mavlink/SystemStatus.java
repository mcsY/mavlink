package com.stratus.mavlink;

public class SystemStatus extends Message{
	
	private int ID = 1;
    private int payload = 31;
    private byte CRC = 124;
    
    @Override
    public void Unpack(byte[] stream, MAVlink mavlink){
    	
    	byte[] buffer = new byte[2];
        System.arraycopy(stream, 14, buffer, 0, 2);
        int bat = dh.int16(buffer);
        Float batt = ((float)bat) / 1000;
        
        mavlink.setBattery(batt);

    	if(bat < 0)
    	{
    		mavlink.setUAVBattery("Unknown");
    	}
    	else
    	{
    		String battery = batt.toString() + " V";
    		mavlink.setUAVBattery(battery);
    	}
    }
    
    @Override
    public int getID(){
        return ID;
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