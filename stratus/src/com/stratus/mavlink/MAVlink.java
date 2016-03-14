package com.stratus.mavlink;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.common.msg_command_int;
import com.MAVLink.enums.MAV_CMD;
import com.stratus.UDP.UDPStream;
import com.stratus.activity.GPS;


public class MAVlink {
	
	DataHelper dh = new DataHelper();
	public final int TIMEOUT = 0;
	public final int INVALID = 1;
	public final int STATUS = 2;
	public final int HEARTBEAT = 3;
	public final int ACK = 4;
	
	private final byte Start = dh.IntToByte(254);
	private final byte SendingSystem = dh.IntToByte(255);
	private final byte SendingComponent = dh.IntToByte(190);
	
	private String UAVBattery;
	private String Altitude;
	private String Speed;
	private String MAVType;
	private String APType;
	private String ToastText;
	
	private boolean ToastAvailable = false;
	private boolean CommandSuccess = false;
	private boolean MissionRequest = false;
	
	private Float MAVlatitude;
	private Float MAVlongitude;
	private Float PhoneLatitude;
	private Float PhoneLongitude;
	private Float MAVRoll;
	private Float MAVPitch;
	private Float LaunchLatitude;
	private Float LaunchLongitude;
	private Float Battery;
	
	private int MAVYaw;
	private int UAVGPS;
	private int MAVsatellites;
	private int SystemId;
	private int ComponentId;
	private int SendSequence = 0;
	private int MissionRequestSequence;
	private int TargetAltitude = 10;
	
	public int GetMessage(UDPStream in, boolean getSenderInfo)
	{
		try
		{
			while(true)
			{
				//message start
				if(in.read() == 254)
				{
					//get message header
					byte[] messageInfo = new byte[5];
					in.read(messageInfo, 0, 5);
	                int payload = dh.ByteToInt(messageInfo[0]);
	                int system = dh.ByteToInt(messageInfo[2]);
	                int component = dh.ByteToInt(messageInfo[3]);
	                int messageId = dh.ByteToInt(messageInfo[4]);
	                
	                //store sending system info
	                if(getSenderInfo)
	                {
	                    setSystemId(system);
	                    setComponentId(component);
	                }
	                
	                //create message class
	                Message message = getNewMessage(messageId);
	                if(message == null)
	                {
	                	return INVALID;
	                }
	                
	                //check if payload size is correct
	                boolean sizeMatch = (payload == message.getPayload());
	                if(!sizeMatch)
	                {
	                	return INVALID;
	                }
	                else
	                {
	                	//use CRC to determine is message is corrupt
	                	byte[] messagePayload = new byte[message.getPayload() + 1];
	                	in.read(messagePayload, 0, message.getPayload());
	                	messagePayload[message.getPayload()] = message.getCRC();
	                	byte[] crcBytes = new byte[2];
	                	in.read(crcBytes, 0, 2);
	                	int receivedCRC = dh.CRC(crcBytes);
	                	
	                	byte[] crcFull = new byte[message.getPayload() + 6];
	                	System.arraycopy(messageInfo, 0, crcFull, 0, 5);
	                	System.arraycopy(messagePayload, 0, crcFull, 5, messagePayload.length);
	                	int expectedCRC = crc_calculate(crcFull);
	                	
	                	if(receivedCRC != expectedCRC)
	                	{
	                		return INVALID;
	                	}
	                	message.Unpack(messagePayload, this);
	                	if(message.getID() == 0)
	                	{
	                		return HEARTBEAT;
	                	}
	                	return STATUS;
	                }
				}
			}
		}catch(Exception e){
			e.getMessage();
			return TIMEOUT;
		}
	}
	
    public Message getNewMessage(int messageId){
        switch(messageId){
            case 0:
                return new Heartbeat();
            case 1:
                return new SystemStatus();
            case 24:
                return new GPSRaw();
            case 30:
            	return new Attitude();
            case 40:
            	return new MissionRequest();
            case 47: 
            	return new MissionAck();
            case 74:
                return new VFRHud();
            case 77:
            	return new CommandAck();
        }
        return null;
    }
    
    private void sendMessage(byte[] payload, UDPStream out) throws IOException
    {
    	int crc = crc_calculate(payload);
    	byte[] message = new byte[payload.length + 2];
    	message[0] = Start;
    	System.arraycopy(payload, 0, message, 1, payload.length - 1);
    	message[payload.length] = translateCRCLow(crc);
    	message[payload.length + 1] = translateCRCHigh(crc);
    	out.write(message);
    }

    public void sendHeartbeat(UDPStream out) throws IOException
    {
    	byte[] heartbeat = new byte[15];
        heartbeat[0] = dh.IntToByte(9);
        heartbeat[1] = getSequence();
        heartbeat[2] = SendingSystem;
        heartbeat[3] = SendingComponent;
        heartbeat[4] = dh.IntToByte(0);
        heartbeat[5] = dh.IntToByte(0);
        heartbeat[6] = dh.IntToByte(0);
        heartbeat[7] = dh.IntToByte(0);
        heartbeat[8] = dh.IntToByte(0);
        heartbeat[9] = dh.IntToByte(6);
        heartbeat[10] = dh.IntToByte(8);
        heartbeat[11] = dh.IntToByte(2);
        heartbeat[12] = dh.IntToByte(4);
        heartbeat[13] = dh.IntToByte(3);
        heartbeat[14] = dh.IntToByte(50);
        sendMessage(heartbeat, out);
    }
    
    public void requestDataStream(UDPStream out) throws IOException
    {
    	byte[] request = new byte[12];
    	request[0] = dh.IntToByte(6);
    	request[1] = getSequence();
    	request[2] = SendingSystem;
    	request[3] = SendingComponent;
    	request[4] = dh.IntToByte(66);
    	request[5] = dh.IntToByte(3); //message rate
    	request[6] = dh.IntToByte(0);
    	request[7] = dh.IntToByte(SystemId);
    	request[8] = dh.IntToByte(ComponentId);
    	request[9] = dh.IntToByte(2); //gps info
    	request[10] = dh.IntToByte(1);
    	request[11] = dh.IntToByte(148);
    	sendMessage(request, out);
    	
    	request[1] = getSequence();
    	request[9] = dh.IntToByte(11); //VFR HUD
    	sendMessage(request, out);
    	
    	request[1] = getSequence();
    	request[5] = dh.IntToByte(5);
    	request[9] = dh.IntToByte(10); //Attitude
    	sendMessage(request, out);
    }
    
    public void sendArmCommand(UDPStream out, boolean arm) throws IOException
    {
    	byte[] command = new byte[39];
    	command[0] = dh.IntToByte(33); //payload size
    	command[1] = getSequence();
    	command[2] = SendingSystem;
    	command[3] = SendingComponent;  
    	command[4] = dh.IntToByte(76); //message id
    	
    	byte[] buffer = new byte[4];
    	ByteBuffer bb = ByteBuffer.wrap(buffer);
    	bb.order(ByteOrder.LITTLE_ENDIAN);
    	if(arm)
    	{
    		bb.putFloat(0, 1);
    		System.arraycopy(buffer, 0, command, 5, 4);	//param1
    	}
    	else
    	{
    		bb.putFloat(0, 0);
    		System.arraycopy(buffer, 0, command, 5, 4);	//param1
    	}
    	bb.putFloat(0, 0);
    	System.arraycopy(buffer, 0, command, 9, 4);	//param2
    	System.arraycopy(buffer, 0, command, 13, 4);//param3
    	System.arraycopy(buffer, 0, command, 17, 4);//param4
    	System.arraycopy(buffer, 0, command, 21, 4);//param5
    	System.arraycopy(buffer, 0, command, 25, 4);//param6
    	System.arraycopy(buffer, 0, command, 29, 4);//param7
    	buffer = new byte[2];
    	bb = ByteBuffer.wrap(buffer);
    	bb.order(ByteOrder.LITTLE_ENDIAN);
    	bb.putShort(0, (short)400);
    	System.arraycopy(buffer, 0, command, 33, 2);//arm command id
    	command[35] = dh.IntToByte(SystemId);		//target system
    	command[36] = dh.IntToByte(250);				//target component
    	command[37] = dh.IntToByte(0);				//confirmation
    	command[38] = dh.IntToByte(152);			//crc extra
    	sendMessage(command, out);
    }
    
    public void sendLandCommand(UDPStream out, boolean returnToBase) throws IOException
    {
    	byte[] command = new byte[39];
    	command[0] = dh.IntToByte(33); //payload size
    	command[1] = getSequence();
    	command[2] = SendingSystem;
    	command[3] = SendingComponent;  
    	command[4] = dh.IntToByte(76); //message id
    	
    	byte[] buffer = new byte[4];
    	ByteBuffer bb = ByteBuffer.wrap(buffer);
    	bb.order(ByteOrder.LITTLE_ENDIAN);
    	bb.putFloat(0, 0);
    	System.arraycopy(buffer, 0, command, 5, 4);	//param1
    	System.arraycopy(buffer, 0, command, 9, 4);	//param2
    	System.arraycopy(buffer, 0, command, 13, 4);//param3
    	System.arraycopy(buffer, 0, command, 17, 4);//param4
    	System.arraycopy(buffer, 0, command, 21, 4);//param5
    	System.arraycopy(buffer, 0, command, 25, 4);//param6
    	System.arraycopy(buffer, 0, command, 29, 4);//param7
    	buffer = new byte[2];
    	bb = ByteBuffer.wrap(buffer);
    	bb.order(ByteOrder.LITTLE_ENDIAN);
    	if(returnToBase)
    	{
    		bb.putShort(0, (short)20);				//RTL command id
    	}
    	else
    	{
    		bb.putShort(0, (short)21);				//Land command id
    	}
    	System.arraycopy(buffer, 0, command, 33, 2);//command id
    	command[35] = dh.IntToByte(SystemId);		//target system
    	command[36] = dh.IntToByte(0);				//target component
    	command[37] = dh.IntToByte(0);				//confirmation
    	command[38] = dh.IntToByte(152);			//crc extra
    	sendMessage(command, out);
    }
    
    public void sendWaypoint(UDPStream out, GPS gps) throws IOException
    {
    	byte[] command = new byte[43];
    	command[0] = dh.IntToByte(37); 	//payload size
    	command[1] = getSequence();		//sequence number
    	command[2] = SendingSystem;		//sending system id
    	command[3] = SendingComponent;	//sending component id
    	command[4] = dh.IntToByte(39);	//message id
    	
    	byte[] buffer = new byte[4];
    	ByteBuffer bb = ByteBuffer.wrap(buffer);
    	bb.order(ByteOrder.LITTLE_ENDIAN);
    	bb.putFloat(0, 1);
    	System.arraycopy(buffer, 0, command, 5, 4);		//param1 waypoint radius accected
    	bb.putFloat(0, 0);
    	System.arraycopy(buffer, 0, command, 9, 4);		//param2 not used
    	System.arraycopy(buffer, 0, command, 13, 4);	//param3 not used
    	System.arraycopy(buffer, 0, command, 17, 4);	//param4 not used
    	bb.putFloat(0, gps.getLatitude());
    	System.arraycopy(buffer, 0, command, 21, 4);	//x - latitude
    	bb.putFloat(0, gps.getLongitude());
    	System.arraycopy(buffer, 0, command, 25, 4);	//y - longitude
    	bb.putFloat(0, (float)TargetAltitude);
    	System.arraycopy(buffer, 0, command, 29, 4);	//z - rel altitude
    	command[33] = dh.IntToByte(0);					//mission sequence
    	command[34] = dh.IntToByte(0);
    	command[35] = dh.IntToByte(16);					//waypoint command id
    	command[36] = dh.IntToByte(0);
    	command[37] = dh.IntToByte(SystemId);			//target system
    	command[38] = dh.IntToByte(0);					//target component
    	command[39] = dh.IntToByte(0);					//global frame id
    	command[40] = dh.IntToByte(2);					//current (2 for guided mode command)
    	command[41] = dh.IntToByte(0);					//autocontinue
    	command[42] = dh.IntToByte(254);				//crc extra
    	sendMessage(command, out);
    }
    
    public void sendTakeoffCommandItem(UDPStream out) throws IOException
    {
    	byte[] command = new byte[43];
    	command[0] = dh.IntToByte(37); 	//payload size
    	command[1] = getSequence();		//sequence number
    	command[2] = SendingSystem;		//sending system id
    	command[3] = SendingComponent;	//sending component id
    	command[4] = dh.IntToByte(39);	//message id
    	
    	byte[] buffer = new byte[4];
    	ByteBuffer bb = ByteBuffer.wrap(buffer);
    	bb.order(ByteOrder.LITTLE_ENDIAN);
    	bb.putFloat(0, 0);
    	System.arraycopy(buffer, 0, command, 5, 4);		//param1
    	System.arraycopy(buffer, 0, command, 9, 4);		//param2
    	System.arraycopy(buffer, 0, command, 13, 4);	//param3
    	System.arraycopy(buffer, 0, command, 17, 4);	//param4
    	bb.putFloat(0, MAVlatitude);
    	System.arraycopy(buffer, 0, command, 21, 4);	//x - latitude
    	bb.putFloat(0, MAVlongitude);
    	System.arraycopy(buffer, 0, command, 25, 4);	//y - longitude
    	bb.putFloat(0, 10);
    	System.arraycopy(buffer, 0, command, 29, 4);	//z - rel altitude
    	command[33] = dh.IntToByte(1);					//mission sequence
    	command[34] = dh.IntToByte(0);
    	command[35] = dh.IntToByte(22);					//takeoff command id
    	command[36] = dh.IntToByte(0);
    	command[37] = dh.IntToByte(SystemId);			//target system
    	command[38] = dh.IntToByte(0);					//target component
    	command[39] = dh.IntToByte(0);					//global frame id
    	command[40] = dh.IntToByte(0);					//current (0 for mission script)
    	command[41] = dh.IntToByte(0);					//autocontinue
    	command[42] = dh.IntToByte(254);				//crc extra
    	sendMessage(command, out);
    }

    public void sendHomeWaypointItem(UDPStream out) throws IOException
    {
    	byte[] command = new byte[43];
    	command[0] = dh.IntToByte(37); 	//payload size
    	command[1] = getSequence();		//sequence number
    	command[2] = SendingSystem;		//sending system id
    	command[3] = SendingComponent;	//sending component id
    	command[4] = dh.IntToByte(39);	//message id
    	
    	byte[] buffer = new byte[4];
    	ByteBuffer bb = ByteBuffer.wrap(buffer);
    	bb.order(ByteOrder.LITTLE_ENDIAN);
    	bb.putFloat(0, 0);
    	System.arraycopy(buffer, 0, command, 5, 4);		//param1
    	System.arraycopy(buffer, 0, command, 9, 4);		//param2
    	System.arraycopy(buffer, 0, command, 13, 4);	//param3
    	System.arraycopy(buffer, 0, command, 17, 4);	//param4
    	bb.putFloat(0, MAVlatitude);
    	System.arraycopy(buffer, 0, command, 21, 4);	//x - latitude
    	bb.putFloat(0, MAVlongitude);
    	System.arraycopy(buffer, 0, command, 25, 4);	//y - longitude
    	bb.putFloat(0, 0);
    	System.arraycopy(buffer, 0, command, 29, 4);	//z - rel altitude
    	command[33] = dh.IntToByte(0);					//mission sequence
    	command[34] = dh.IntToByte(0);
    	command[35] = dh.IntToByte(16);					//waypoint command id
    	command[36] = dh.IntToByte(0);
    	command[37] = dh.IntToByte(SystemId);			//target system
    	command[38] = dh.IntToByte(0);					//target component
    	command[39] = dh.IntToByte(0);					//global frame id
    	command[40] = dh.IntToByte(0);					//current (0 for mission script)
    	command[41] = dh.IntToByte(0);					//autocontinue
    	command[42] = dh.IntToByte(254);				//crc extra
    	sendMessage(command, out);
    }

    public void sendLoiterItem(UDPStream out) throws IOException
    {
    	byte[] command = new byte[43];
    	command[0] = dh.IntToByte(37); 	//payload size
    	command[1] = getSequence();		//sequence number
    	command[2] = SendingSystem;		//sending system id
    	command[3] = SendingComponent;	//sending component id
    	command[4] = dh.IntToByte(39);	//message id
    	
    	byte[] buffer = new byte[4];
    	ByteBuffer bb = ByteBuffer.wrap(buffer);
    	bb.order(ByteOrder.LITTLE_ENDIAN);
    	bb.putFloat(0, 0);
    	System.arraycopy(buffer, 0, command, 5, 4);		//param1
    	System.arraycopy(buffer, 0, command, 9, 4);		//param2
    	System.arraycopy(buffer, 0, command, 13, 4);	//param3
    	System.arraycopy(buffer, 0, command, 17, 4);	//param4
    	bb.putFloat(0, MAVlatitude);
    	System.arraycopy(buffer, 0, command, 21, 4);	//x - latitude
    	bb.putFloat(0, MAVlongitude);
    	System.arraycopy(buffer, 0, command, 25, 4);	//y - longitude
    	bb.putFloat(0, (float)TargetAltitude);
    	System.arraycopy(buffer, 0, command, 29, 4);	//z - rel altitude
    	command[33] = dh.IntToByte(1);					//mission sequence
    	command[34] = dh.IntToByte(0);
    	command[35] = dh.IntToByte(17);					//loiter command id
    	command[36] = dh.IntToByte(0);
    	command[37] = dh.IntToByte(SystemId);			//target system
    	command[38] = dh.IntToByte(0);					//target component
    	command[39] = dh.IntToByte(0);					//global frame id
    	command[40] = dh.IntToByte(0);					//current (0 for mission script)
    	command[41] = dh.IntToByte(0);					//autocontinue
    	command[42] = dh.IntToByte(254);				//crc extra
    	sendMessage(command, out);    	
    }
    
    public void clearMission(UDPStream out) throws IOException
    {
    	byte[] command = new byte[8];
    	command[0] = dh.IntToByte(2); 	//payload size
    	command[1] = getSequence();		//sequence number
    	command[2] = SendingSystem;		//sending system id
    	command[3] = SendingComponent;	//sending component id
    	command[4] = dh.IntToByte(45);	//message id
    	command[5] = dh.IntToByte(SystemId); //target system
    	command[6] = dh.IntToByte(0);		 //target component
    	command[7] = dh.IntToByte(232);		 //crc extra
    	sendMessage(command, out);
    }

    public void sendMissionCount(UDPStream out) throws IOException
    {
    	byte[] command = new byte[10];
    	command[0] = dh.IntToByte(4); 	//payload size
    	command[1] = getSequence();		//sequence number
    	command[2] = SendingSystem;		//sending system id
    	command[3] = SendingComponent;	//sending component id
    	command[4] = dh.IntToByte(44);	//message id
    	command[5] = dh.IntToByte(2);	//number of mission items
    	command[6] = dh.IntToByte(0);	
    	command[7] = dh.IntToByte(SystemId);//target system
    	command[8] = dh.IntToByte(0);	    //target component
    	command[9] = dh.IntToByte(221);		//crc extra
    	sendMessage(command, out);
    }
    
    public void missionStart(UDPStream out) throws IOException
    {
    	byte[] command = new byte[39];
    	command[0] = dh.IntToByte(33); //payload size
    	command[1] = getSequence();
    	command[2] = SendingSystem;
    	command[3] = SendingComponent;  
    	command[4] = dh.IntToByte(76); //message id
    	
    	byte[] buffer = new byte[4];
    	ByteBuffer bb = ByteBuffer.wrap(buffer);
    	bb.order(ByteOrder.LITTLE_ENDIAN);
    	bb.putFloat(0, 0);
    	System.arraycopy(buffer, 0, command, 5, 4);	//param1
    	System.arraycopy(buffer, 0, command, 9, 4);	//param2
    	System.arraycopy(buffer, 0, command, 13, 4);//param3
    	System.arraycopy(buffer, 0, command, 17, 4);//param4
    	System.arraycopy(buffer, 0, command, 21, 4);//param5
    	System.arraycopy(buffer, 0, command, 25, 4);//param6
    	System.arraycopy(buffer, 0, command, 29, 4);//param7
    	buffer = new byte[2];
    	bb = ByteBuffer.wrap(buffer);
    	bb.order(ByteOrder.LITTLE_ENDIAN);
    	bb.putShort(0, (short)300);					//mission start command id
    	System.arraycopy(buffer, 0, command, 33, 2);//command id
    	command[35] = dh.IntToByte(SystemId);		//target system
    	command[36] = dh.IntToByte(0);				//target component
    	command[37] = dh.IntToByte(0);				//confirmation
    	command[38] = dh.IntToByte(152);			//crc extra
    	sendMessage(command, out);
    }
    
    public void overrideThrottle(UDPStream out) throws IOException
    {
    	byte[] command = new byte[24];
    	command[0] = dh.IntToByte(18); //payload size
    	command[1] = getSequence();
    	command[2] = SendingSystem;
    	command[3] = SendingComponent;  
    	command[4] = dh.IntToByte(70); //message id
    	
    	byte[] buffer = new byte[2];
    	ByteBuffer bb = ByteBuffer.wrap(buffer);
    	bb.order(ByteOrder.LITTLE_ENDIAN);
    	bb.putShort(0, (short)0);
    	System.arraycopy(buffer, 0, command, 5, 2);  //channel 1
    	System.arraycopy(buffer, 0, command, 7, 2);  //channel 2
    	bb.putShort(0, (short)1500);
    	System.arraycopy(buffer, 0, command, 9, 2);  //channel 3
    	bb.putShort(0, (short)0);
    	System.arraycopy(buffer, 0, command, 11, 2); //channel 4
    	System.arraycopy(buffer, 0, command, 13, 2); //channel 5
    	System.arraycopy(buffer, 0, command, 15, 2); //channel 6
    	System.arraycopy(buffer, 0, command, 17, 2); //channel 7
    	System.arraycopy(buffer, 0, command, 19, 2); //channel 8
    	command[21] = dh.IntToByte(SystemId);	//target system
    	command[22] = dh.IntToByte(0);			//target component
    	command[23] = dh.IntToByte(124);
    	sendMessage(command, out);
    }
    
    public void releaseThrottle(UDPStream out) throws IOException
    {
    	byte[] command = new byte[24];
    	command[0] = dh.IntToByte(18); //payload size
    	command[1] = getSequence();
    	command[2] = SendingSystem;
    	command[3] = SendingComponent;  
    	command[4] = dh.IntToByte(70); //message id
    	
    	byte[] buffer = new byte[2];
    	ByteBuffer bb = ByteBuffer.wrap(buffer);
    	bb.order(ByteOrder.LITTLE_ENDIAN);
    	bb.putShort(0, (short)0);
    	System.arraycopy(buffer, 0, command, 5, 2);  //channel 1
    	System.arraycopy(buffer, 0, command, 7, 2);  //channel 2
    	System.arraycopy(buffer, 0, command, 9, 2);  //channel 3
    	System.arraycopy(buffer, 0, command, 11, 2); //channel 4
    	System.arraycopy(buffer, 0, command, 13, 2); //channel 5
    	System.arraycopy(buffer, 0, command, 15, 2); //channel 6
    	System.arraycopy(buffer, 0, command, 17, 2); //channel 7
    	System.arraycopy(buffer, 0, command, 19, 2); //channel 8
    	command[21] = dh.IntToByte(SystemId);	//target system
    	command[22] = dh.IntToByte(0);			//target component
    	command[23] = dh.IntToByte(124);
    	sendMessage(command, out);
    }
    
    public byte getSequence()
    {
    	byte sequence = dh.IntToByte(SendSequence);
    	if(sequence == 255)
    	{
    		sequence = 0;
    	}
    	else
    	{
    		SendSequence++;
    	}
    	return sequence;
    }
    
    public final HashMap<Integer, String> MavTypeEnum = new HashMap<Integer, String>(){
		private static final long serialVersionUID = 1L;
		{
            put(0,"Generic");
            put(1,"Fixed Wing");
            put(2,"Quadcopter");
            put(3,"Coaxial");
            put(4,"Helicopter");
            put(5,"Antenna Tracker");
            put(6,"GCS");
            put(7,"Airship");
            put(8,"Balloon");
            put(9,"Rocket");
            put(10,"Ground Rover");
            put(11,"Surface Boat");
            put(12,"Submarine");
            put(13,"Hexarotor");
            put(14,"Octorotor");
            put(15,"Tricopter");
            put(16,"Flapping Wing");
            put(17,"Kite");
        }
    }; 
    
    public final HashMap<Integer, String> AutopilotEnum = new HashMap<Integer, String>(){
		private static final long serialVersionUID = 1L;

		{
            put(0,"Generic");
            put(1,"Pixhawk");
            put(2,"Slugs");
            put(3,"ArduPilotMega");
            put(4,"Open Pilot");
            put(5,"Generic WP Only");
            put(6,"Generic Simple Navigation Only");
            put(7,"Generic Mission Full");
            put(8,"Invalid");
            put(9,"PPZ");
            put(10,"UD8");
            put(11,"FlexPilot");
            put(12,"PX4");
        }
    } ;  
    
    public int crc_calculate(byte[] buffer) {
        int crc = 0x0000ffff;
        for (int i = 0; i < buffer.length; i++) {
            crc = crc_accumulate(buffer[i], crc);
        }
        return crc;
    }

    public static int crc_accumulate(byte data, int crc) {

        int tmp, tmpdata;
        int crcaccum = crc & 0x000000ff;
        tmpdata = data & 0x000000ff;
        tmp = tmpdata ^ crcaccum;
        tmp &= 0x000000ff;
        int tmp4 = tmp << 4;
        tmp4 &= 0x000000ff;
        tmp ^= tmp4;
        tmp &= 0x000000ff;
        int crch = crc >> 8;
        crch &= 0x0000ffff;
        int tmp8 = tmp << 8;
        tmp8 &= 0x0000ffff;
        int tmp3 = tmp << 3;
        tmp3 &= 0x0000ffff;
        tmp4 = tmp >> 4;
        tmp4 &= 0x0000ffff;
        int tmpa = crch ^ tmp8;
        tmpa &= 0x0000ffff;
        int tmpb = tmp3 ^ tmp4;
        tmpb &= 0x0000ffff;
        crc = tmpa ^ tmpb;
        crc &= 0x0000ffff;
        return crc;
    }
    
    public byte translateCRCLow(int value)
    {
        byte[] bytes = new byte[4];
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.putInt(0, value);
        return bb.get(3);
    }
    
    public byte translateCRCHigh(int value)
    {
        byte[] bytes = new byte[4];
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.putInt(value);
        return bb.get(2);
    }

	public String getUAVBattery() {
		return UAVBattery;
	}

	public void setUAVBattery(String uAVBattery) {
		UAVBattery = uAVBattery;
	}

	public int getUAVGPS() {
		return UAVGPS;
	}

	public void setUAVGPS(int uAVGPS) {
		UAVGPS = uAVGPS;
	}

	public String getAltitude() {
		return Altitude;
	}

	public void setAltitude(String altitude) {
		Altitude = altitude;
	}

	public String getSpeed() {
		return Speed;
	}

	public void setSpeed(String speed) {
		Speed = speed;
	}

	public String getMAVType() {
		return MAVType;
	}

	public void setMAVType(String mAVType) {
		MAVType = mAVType;
	}

	public String getAPType() {
		return APType;
	}

	public void setAPType(String aPType) {
		APType = aPType;
	}

	public int getSystemId() {
		return SystemId;
	}

	public void setSystemId(int systemId) {
		SystemId = systemId;
	}

	public int getComponentId() {
		return ComponentId;
	}

	public void setComponentId(int componentId) {
		ComponentId = componentId;
	}

	public Float getMAVlatitude() {
		return MAVlatitude;
	}

	public void setMAVlatitude(Float mAVlatitude) {
		MAVlatitude = mAVlatitude;
	}

	public Float getMAVlongitude() {
		return MAVlongitude;
	}

	public void setMAVlongitude(Float mAVlongitude) {
		MAVlongitude = mAVlongitude;
	}

	public Float getPhoneLatitude() {
		return PhoneLatitude;
	}

	public void setPhoneLatitude(Float phoneLatitude) {
		PhoneLatitude = phoneLatitude;
	}

	public Float getPhoneLongitude() {
		return PhoneLongitude;
	}

	public void setPhoneLongitude(Float phoneLongitude) {
		PhoneLongitude = phoneLongitude;
	}

	public int getMAVsatellites() {
		return MAVsatellites;
	}

	public void setMAVsatellites(int mAVsatellites) {
		MAVsatellites = mAVsatellites;
	}

	public String getUAVGPSStatus() {
		if(UAVGPS < 0)
		{
			return "";
		}
		if(UAVGPS < 2)
		{
			return "No Fix (" + MAVsatellites + ")";
		}
		else
		{
			if(MAVsatellites < 5)
			{
				return "Poor (" + MAVsatellites + ")";
			}
			else if(MAVsatellites < 7)
			{
				return "Fair (" + MAVsatellites + ")";
			}
			else
			{
				return "Good (" + MAVsatellites + ")";
			}
		}
	}

	public String getToastText() {
		return ToastText;
	}

	public void setToastText(String toastText) {
		ToastText = toastText;
	}

	public boolean isToastAvailable() {
		return ToastAvailable;
	}

	public void setToastAvailable(boolean toastAvailable) {
		ToastAvailable = toastAvailable;
	}

	public boolean isCommandSuccess() {
		return CommandSuccess;
	}

	public void setCommandSuccess(boolean commandSuccess) {
		CommandSuccess = commandSuccess;
	}

	public void setMAVRoll(Float mAVRoll) {
		MAVRoll = mAVRoll;
	}

	public void setMAVPitch(Float mAVPitch) {
		MAVPitch = mAVPitch;
	}

	public void setMAVYaw(int mAVYaw) {
		MAVYaw = mAVYaw;
	}
	
	public String getRoll()
	{
		float calc = (float) (MAVRoll * 57.295827);
		int rounded = Math.round(calc);
		String value = ((Integer)rounded).toString();
		return value + "\u00B0";
	}
	
	public String getPitch()
	{
		float calc = (float) (MAVPitch * 57.295827);
		int rounded = Math.round(calc);
		String value = ((Integer)rounded).toString();
		return value + "\u00B0";
	}
	
	public String getYaw()
	{
		String value = ((Integer)MAVYaw).toString();
		return value;
	}

	public Float getLaunchLatitude() {
		return LaunchLatitude;
	}

	public void setLaunchLatitude(Float launchLatitude) {
		LaunchLatitude = launchLatitude;
	}

	public Float getLaunchLongitude() {
		return LaunchLongitude;
	}

	public void setLaunchLongitude(Float launchLongitude) {
		LaunchLongitude = launchLongitude;
	}

	public boolean isMissionRequest() {
		return MissionRequest;
	}

	public void setMissionRequest(boolean missionRequest) {
		MissionRequest = missionRequest;
	}

	public int getMissionRequestSequence() {
		return MissionRequestSequence;
	}

	public void setMissionRequestSequence(int missionRequestSequence) {
		MissionRequestSequence = missionRequestSequence;
	}

	public int getTargetAltitude() {
		return TargetAltitude;
	}

	public void setTargetAltitude(int targetAltitude) {
		TargetAltitude = targetAltitude;
	}

	public Float getBattery() {
		return Battery;
	}

	public void setBattery(Float battery) {
		Battery = battery;
	}
	
	  ///////////////////////////////////////////////
    /////////////////////////////////////
    //my stuff
    public void  changeSpeed(UDPStream out, float speed)  throws IOException{
    	msg_command_int cmd = new msg_command_int();
    	cmd.command = MAV_CMD.MAV_CMD_DO_CHANGE_SPEED;
    	cmd.target_system = 250;
    	cmd.target_component=190;
    	cmd.param1 = 0;
    	cmd.param2 = speed;
    	cmd.param3 = 20;
    	MAVLinkPacket txtPacket = cmd.pack();
    	byte[] buf = txtPacket.encodePacket();
    	out.write(buf);
    
    }
    ///Trying to make a customized mavlink cmd for Uvade
  
    ////////////////////////////////
}