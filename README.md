# mavlink

Instructions on how to use Android Application

Step 1
Get SITL (Virtual QuadCopter with MAVProxy for Windows or Linux 

http://dev.ardupilot.com/wiki/sitl-native-on-windows/

http://dev.ardupilot.com/wiki/setting-up-sitl-on-linux/

SITL can also be accessed through Mission Planner(Simulation Tab) but MAVProxy is not used in this version of SITL.

http://ardupilot.com/downloads/?did=82

Step 2

Since this is an Android Application, you need an Android phone or emulator.

		My Android Setup
			JAVA
			Eclipse
			Android SDK
			Android Dev Tools
			
Step 3
Once you have your SITL and you are able to run the application on Android, you must connect to the quadcopter.

	-You must find the ip address of the phone. ( adb shell netcfg)
	-Find the ip address of your computer.
	-And determine what your receiver port should be ( ex. UDP 14552)
	-In the android application, locate UDPStream.java( under the src folder)
		Input your Computer's IpAddress and the receiver port.
	- Run SITL and your application. 
		MAVProxy cmd box will start run with SITL
			-Inside MAVProxy, you must input this command for SITL to connect to your phone.
					"Output add IPAddress:14552"
	- Your Android Application will not connect to SITL at first, so you must find the "Sender Port"
		Download WireShark
			-Then once downloaded, capture whichever network you are running your application and computer ( ex Wireless Connection)
			-In the filter box on top input (udp.port ==14552)
			-The capture communication between your computer and the applicaton should be highlighted in black with lime green lettering.	
				- Click on this, and under the User Datagram Protocol you will find the port that SITL is trying to receive it's mavlink packets from the Android application.
				
	-Once you find the Sender Port, go back to your Android application and under UDPStream.java  input the Sender port (ex 5XXXX)
	-Run the application again, and now you should be able to connect to SITL. (Keep SITL running, do not restart)
		-Then once connected press Takeoff.
If you want to have more control of the Quadcopter, use Mission Planner. 
	- You can add gamecontroller  to control the YAW, Pitch and Roll. 
