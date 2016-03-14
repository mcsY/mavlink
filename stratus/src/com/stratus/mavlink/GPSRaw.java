package com.stratus.mavlink;

public class GPSRaw extends Message{
	
	private int id = 24;
    private int payload = 30;
    private byte CRC = 24;
    
    @Override
    public void Unpack(byte[] stream, MAVlink mavlink){
    	

        byte[] buffer = new byte[4];
        System.arraycopy(stream, 8, buffer, 0, 4);
        int latitude = dh.int32(buffer);
        Float mavLatitude = (float) (latitude / 10000000.0);
        mavlink.setMAVlatitude(mavLatitude);
        
        buffer = new byte[4];
        System.arraycopy(stream, 12, buffer, 0, 4);
        int longitude = dh.int32(buffer); 
        Float mavLongitude = (float) (longitude / 10000000.0);
        mavlink.setMAVlongitude(mavLongitude);
        
        int fix = stream[28];
        mavlink.setUAVGPS(fix);
        
        int sat = stream[29];
        mavlink.setMAVsatellites(sat);
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