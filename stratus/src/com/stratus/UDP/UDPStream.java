package com.stratus.UDP;
/**
 * 
 */
/**
 * @author santoscm
 *
 */
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UDPStream {
	
	private static final int PACKET_BUFFER_SIZE = 5000;
	private static final int HOST_PORT = 54063; //sender port
	private static final int LOCAL_PORT = 14552; // receiver port
	private static final String SEND_IP = "192.168.1.221";
	
	InetAddress sendIP;
	
	DatagramSocket socket = null;
	DatagramPacket packet = null;
	DatagramPacket sendPacket = null;
	
	byte[] inputData = new byte[PACKET_BUFFER_SIZE];
	int dataSize = 0;
	int dataIndex = 0;
	
	public UDPStream()
	{
		try
		{
			open();
			sendIP = InetAddress.getByName(SEND_IP);
		}catch(Exception e){}
	}
	
	public void open() throws UnknownHostException, SocketException
	{
		socket = new DatagramSocket(LOCAL_PORT);
		//throw exception if no data received for 2 seconds
		socket.setSoTimeout(2000);
	}
	
	public void close() throws IOException
	{
		socket.close();
		socket = null;
		inputData = null;
		dataSize = 0;
		dataIndex = 0;
	}
	
	public int available() throws IOException
	{
		return dataSize - dataIndex;
	}
	
	public int read() throws IOException
	{
		if(dataSize == dataIndex)
		{
			receive();
		}
		int value = inputData[dataIndex] & 0xff;
		dataIndex++;
		return value;
	}
	
	public int read(byte[] buffer) throws IOException
	{
		return read(buffer, 0, buffer.length);
	}
	
	public int read(byte[] buffer, int offset, int length) throws IOException {
		if (dataIndex == dataSize) {
			receive();
		}

		int lengthRemaining = length;
		  
		while(available() < lengthRemaining) {
			System.arraycopy(inputData, dataIndex, buffer, offset + (length - lengthRemaining), available());
		    lengthRemaining = lengthRemaining - available();
		    receive();
		}

		System.arraycopy(inputData, dataIndex, buffer, offset + (length - lengthRemaining), lengthRemaining);
		dataIndex = dataIndex + lengthRemaining;
		return length;
	}
	
	private void receive() throws IOException {
		packet = new DatagramPacket(inputData, PACKET_BUFFER_SIZE);
		socket.receive(packet);
		dataIndex = 0;
		dataSize = packet.getLength();
	}
	
	public void write(byte[] message)
	{
		try {
			sendPacket = new DatagramPacket(message, message.length, sendIP, HOST_PORT);
			socket.send(sendPacket);
		} catch (Exception e) {
			e.getMessage();
		}
	}

}