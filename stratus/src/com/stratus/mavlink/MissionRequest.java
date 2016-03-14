package com.stratus.mavlink;

public class MissionRequest extends Message{
    
    private int ID = 40;
    private int payload = 4;
    private byte CRC = dh.IntToByte(230);
    
    @Override
    public void Unpack(byte[] stream, MAVlink mavlink){
        
        byte[] buffer = new byte[2];
        System.arraycopy(stream, 0, buffer, 0, 2);
        int sequence = dh.int16(buffer);
        mavlink.setMissionRequest(true);
        mavlink.setMissionRequestSequence(sequence + 1);
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