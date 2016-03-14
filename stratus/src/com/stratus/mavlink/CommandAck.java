package com.stratus.mavlink;

public class CommandAck extends Message{
	
	private int id = 77;
    private int payload = 3;
    private byte CRC = dh.IntToByte(143);
    
    @Override
    public void Unpack(byte[] stream, MAVlink mavlink){
    	
    	byte[] buffer = new byte[2];
        System.arraycopy(stream, 0, buffer, 0, 2);
        
        int status = stream[2];
        
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