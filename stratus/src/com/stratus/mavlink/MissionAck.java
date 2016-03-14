package com.stratus.mavlink;

public class MissionAck extends Message{
	
	private int id = 47;
    private int payload = 3;
    private byte CRC = dh.IntToByte(153);
    
    @Override
    public void Unpack(byte[] stream, MAVlink mavlink){
    	
    	//int system = dh.ByteToInt(stream[0]);
    	//int component = dh.ByteToInt(stream[1]);
        int status = dh.ByteToInt(stream[2]);
        
        if(status == 0)
        {
        	mavlink.setCommandSuccess(true);
        }
    	
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