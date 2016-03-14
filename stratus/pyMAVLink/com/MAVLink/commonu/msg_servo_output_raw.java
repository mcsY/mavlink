/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the
 * java mavlink generator tool. It should not be modified by hand.
 */

// MESSAGE SERVO_OUTPUT_RAW PACKING
package com.MAVLink.commonu;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        
/**
* The RAW values of the servo outputs (for RC input from the remote, use the RC_CHANNELS messages). The standard PPM modulation is as follows: 1000 microseconds: 0%, 2000 microseconds: 100%.
*/
public class msg_servo_output_raw extends MAVLinkMessage{

    public static final int MAVLINK_MSG_ID_SERVO_OUTPUT_RAW = 36;
    public static final int MAVLINK_MSG_LENGTH = 21;
    private static final long serialVersionUID = MAVLINK_MSG_ID_SERVO_OUTPUT_RAW;


      
    /**
    * Timestamp (microseconds since system boot)
    */
    public long time_usec;
      
    /**
    * Servo output 1 value, in microseconds
    */
    public int servo1_raw;
      
    /**
    * Servo output 2 value, in microseconds
    */
    public int servo2_raw;
      
    /**
    * Servo output 3 value, in microseconds
    */
    public int servo3_raw;
      
    /**
    * Servo output 4 value, in microseconds
    */
    public int servo4_raw;
      
    /**
    * Servo output 5 value, in microseconds
    */
    public int servo5_raw;
      
    /**
    * Servo output 6 value, in microseconds
    */
    public int servo6_raw;
      
    /**
    * Servo output 7 value, in microseconds
    */
    public int servo7_raw;
      
    /**
    * Servo output 8 value, in microseconds
    */
    public int servo8_raw;
      
    /**
    * Servo output port (set of 8 outputs = 1 port). Most MAVs will just use one, but this allows to encode more than 8 servos.
    */
    public short port;
    

    /**
    * Generates the payload for a mavlink message for a message of this type
    * @return
    */
    public MAVLinkPacket pack(){
        MAVLinkPacket packet = new MAVLinkPacket();
        packet.len = MAVLINK_MSG_LENGTH;
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_SERVO_OUTPUT_RAW;
              
        packet.payload.putUnsignedInt(time_usec);
              
        packet.payload.putUnsignedShort(servo1_raw);
              
        packet.payload.putUnsignedShort(servo2_raw);
              
        packet.payload.putUnsignedShort(servo3_raw);
              
        packet.payload.putUnsignedShort(servo4_raw);
              
        packet.payload.putUnsignedShort(servo5_raw);
              
        packet.payload.putUnsignedShort(servo6_raw);
              
        packet.payload.putUnsignedShort(servo7_raw);
              
        packet.payload.putUnsignedShort(servo8_raw);
              
        packet.payload.putUnsignedByte(port);
        
        return packet;
    }

    /**
    * Decode a servo_output_raw message into this class fields
    *
    * @param payload The message to decode
    */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
              
        this.time_usec = payload.getUnsignedInt();
              
        this.servo1_raw = payload.getUnsignedShort();
              
        this.servo2_raw = payload.getUnsignedShort();
              
        this.servo3_raw = payload.getUnsignedShort();
              
        this.servo4_raw = payload.getUnsignedShort();
              
        this.servo5_raw = payload.getUnsignedShort();
              
        this.servo6_raw = payload.getUnsignedShort();
              
        this.servo7_raw = payload.getUnsignedShort();
              
        this.servo8_raw = payload.getUnsignedShort();
              
        this.port = payload.getUnsignedByte();
        
    }

    /**
    * Constructor for a new message, just initializes the msgid
    */
    public msg_servo_output_raw(){
        msgid = MAVLINK_MSG_ID_SERVO_OUTPUT_RAW;
    }

    /**
    * Constructor for a new message, initializes the message with the payload
    * from a mavlink packet
    *
    */
    public msg_servo_output_raw(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_SERVO_OUTPUT_RAW;
        unpack(mavLinkPacket.payload);        
    }

                        
    /**
    * Returns a string with the MSG name and data
    */
    public String toString(){
        return "MAVLINK_MSG_ID_SERVO_OUTPUT_RAW -"+" time_usec:"+time_usec+" servo1_raw:"+servo1_raw+" servo2_raw:"+servo2_raw+" servo3_raw:"+servo3_raw+" servo4_raw:"+servo4_raw+" servo5_raw:"+servo5_raw+" servo6_raw:"+servo6_raw+" servo7_raw:"+servo7_raw+" servo8_raw:"+servo8_raw+" port:"+port+"";
    }
}
        