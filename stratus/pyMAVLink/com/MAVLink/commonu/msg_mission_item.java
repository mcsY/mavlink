/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the
 * java mavlink generator tool. It should not be modified by hand.
 */

// MESSAGE MISSION_ITEM PACKING
package com.MAVLink.commonu;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        
/**
* Message encoding a mission item. This message is emitted to announce
                the presence of a mission item and to set a mission item on the system. The mission item can be either in x, y, z meters (type: LOCAL) or x:lat, y:lon, z:altitude. Local frame is Z-down, right handed (NED), global frame is Z-up, right handed (ENU). See also http://qgroundcontrol.org/mavlink/waypoint_protocol.
*/
public class msg_mission_item extends MAVLinkMessage{

    public static final int MAVLINK_MSG_ID_MISSION_ITEM = 39;
    public static final int MAVLINK_MSG_LENGTH = 37;
    private static final long serialVersionUID = MAVLINK_MSG_ID_MISSION_ITEM;


      
    /**
    * PARAM1, see MAV_CMD enum
    */
    public float param1;
      
    /**
    * PARAM2, see MAV_CMD enum
    */
    public float param2;
      
    /**
    * PARAM3, see MAV_CMD enum
    */
    public float param3;
      
    /**
    * PARAM4, see MAV_CMD enum
    */
    public float param4;
      
    /**
    * PARAM5 / local: x position, global: latitude
    */
    public float x;
      
    /**
    * PARAM6 / y position: global: longitude
    */
    public float y;
      
    /**
    * PARAM7 / z position: global: altitude (relative or absolute, depending on frame.
    */
    public float z;
      
    /**
    * Sequence
    */
    public int seq;
      
    /**
    * The scheduled action for the MISSION. see MAV_CMD in common.xml MAVLink specs
    */
    public int command;
      
    /**
    * System ID
    */
    public short target_system;
      
    /**
    * Component ID
    */
    public short target_component;
      
    /**
    * The coordinate system of the MISSION. see MAV_FRAME in mavlink_types.h
    */
    public short frame;
      
    /**
    * false:0, true:1
    */
    public short current;
      
    /**
    * autocontinue to next wp
    */
    public short autocontinue;
    

    /**
    * Generates the payload for a mavlink message for a message of this type
    * @return
    */
    public MAVLinkPacket pack(){
        MAVLinkPacket packet = new MAVLinkPacket();
        packet.len = MAVLINK_MSG_LENGTH;
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_MISSION_ITEM;
              
        packet.payload.putFloat(param1);
              
        packet.payload.putFloat(param2);
              
        packet.payload.putFloat(param3);
              
        packet.payload.putFloat(param4);
              
        packet.payload.putFloat(x);
              
        packet.payload.putFloat(y);
              
        packet.payload.putFloat(z);
              
        packet.payload.putUnsignedShort(seq);
              
        packet.payload.putUnsignedShort(command);
              
        packet.payload.putUnsignedByte(target_system);
              
        packet.payload.putUnsignedByte(target_component);
              
        packet.payload.putUnsignedByte(frame);
              
        packet.payload.putUnsignedByte(current);
              
        packet.payload.putUnsignedByte(autocontinue);
        
        return packet;
    }

    /**
    * Decode a mission_item message into this class fields
    *
    * @param payload The message to decode
    */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
              
        this.param1 = payload.getFloat();
              
        this.param2 = payload.getFloat();
              
        this.param3 = payload.getFloat();
              
        this.param4 = payload.getFloat();
              
        this.x = payload.getFloat();
              
        this.y = payload.getFloat();
              
        this.z = payload.getFloat();
              
        this.seq = payload.getUnsignedShort();
              
        this.command = payload.getUnsignedShort();
              
        this.target_system = payload.getUnsignedByte();
              
        this.target_component = payload.getUnsignedByte();
              
        this.frame = payload.getUnsignedByte();
              
        this.current = payload.getUnsignedByte();
              
        this.autocontinue = payload.getUnsignedByte();
        
    }

    /**
    * Constructor for a new message, just initializes the msgid
    */
    public msg_mission_item(){
        msgid = MAVLINK_MSG_ID_MISSION_ITEM;
    }

    /**
    * Constructor for a new message, initializes the message with the payload
    * from a mavlink packet
    *
    */
    public msg_mission_item(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_MISSION_ITEM;
        unpack(mavLinkPacket.payload);        
    }

                                
    /**
    * Returns a string with the MSG name and data
    */
    public String toString(){
        return "MAVLINK_MSG_ID_MISSION_ITEM -"+" param1:"+param1+" param2:"+param2+" param3:"+param3+" param4:"+param4+" x:"+x+" y:"+y+" z:"+z+" seq:"+seq+" command:"+command+" target_system:"+target_system+" target_component:"+target_component+" frame:"+frame+" current:"+current+" autocontinue:"+autocontinue+"";
    }
}
        