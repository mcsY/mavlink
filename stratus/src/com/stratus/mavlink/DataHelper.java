package com.stratus.mavlink;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DataHelper {
	
    //decode 1 byte into signed int
    public Integer int8(byte[] bytes){
        byte b = bytes[0];
        return (int)b;
    }
    
    
    //decode 2 bytes into signed int
    public Integer int16(byte[] bytes){
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        short int16 = bb.getShort();
        return (int)int16;
    }
    
    
    //decode 2 bytes into unsigned int
    public Integer uint16(byte[] bytes){
        byte[] intBytes = new byte[4];
        intBytes[0] = bytes[0];
        intBytes[1] = bytes[1];
        intBytes[2] = 0;
        intBytes[3] = 0;
        ByteBuffer bb = ByteBuffer.wrap(intBytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt();
    }
    
    
    //decode 4 bytes into a signed int
    public Integer int32(byte[] bytes){
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt();
    }
    
    
    //decode 4 bytes into a unsigned long
    public Long uint32(byte[] bytes){
        byte[] longBytes = new byte[8];
        System.arraycopy(bytes, 0, longBytes, 0, 4);
        for(int i = 4; i < 8; i++){
            longBytes[i] = 0;
        }
        ByteBuffer bb = ByteBuffer.wrap(longBytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        long test = bb.getLong();
        return test;
    }
    
    //decode 8 bytes into a long (signed or unsigned)
    public Long int64(byte[] bytes){
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getLong();
    }
    
    //decode 4 bytes into a float
    public Float float32(byte[] bytes){
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getFloat();
    }
    
    //decode 8 bytes into a double
    public Double double64(byte[] bytes){
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getDouble();
    }
    
    //decode 2 bytes into a char
    public Character char8(byte[] bytes){
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getChar();
    }
    
    //get byte value from int
    public byte IntToByte(int value)
    {
        byte[] bytes = new byte[4];
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.putInt(0, value);
        return bb.get(3);
    } 

    //get int value from byte
    public int ByteToInt(byte b)
    {
        byte[] bytes = new byte[4];
        bytes[3] = b;
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        return bb.getInt();
    }
    
    //get crc value from buffer
    public int CRC(byte[] bytes)
    {
    	byte[] intBytes = new byte[4];
    	intBytes[2] = 0;
    	intBytes[3] = 0;
    	System.arraycopy(bytes, 0, intBytes, 0, 2);
    	ByteBuffer bb = ByteBuffer.wrap(intBytes);
    	bb.order(ByteOrder.LITTLE_ENDIAN);
    	return bb.getInt();
    }

}