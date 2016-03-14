package com.stratus.mavlink;

public class Heartbeat extends Message{
    
    private int ID = 0;
    private int payload = 9;
    private byte CRC = 50;
    
    @Override
    public void Unpack(byte[] stream, MAVlink mavlink){
        
        int mavType = stream[4];
        String MAVtype = mavlink.MavTypeEnum.get(mavType);
        
        int autoType = stream[5];
        String AutoType = mavlink.AutopilotEnum.get(autoType);

        mavlink.setMAVType(MAVtype);
        mavlink.setAPType(AutoType);
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