package com.example.login;

import java.net.SocketAddress;

public class ThreadLocalVariablesKeeper {

	private static ThreadLocal<SensorDevice> SENSOR_DEVICE = new ThreadLocal<SensorDevice>();
	private static ThreadLocal<SocketAddress> SERVER_ADDRESS = new ThreadLocal<SocketAddress>();
	
	public static SensorDevice getSensorDevice(){
		if (SENSOR_DEVICE == null){
			return null;
		}
		return SENSOR_DEVICE.get();
	}
	
	public static void setSensorDevice(SensorDevice newSensorDevice){
		if (SENSOR_DEVICE == null){
			SENSOR_DEVICE =  new ThreadLocal<SensorDevice>();
		}
		SENSOR_DEVICE.set(newSensorDevice);
	}
	
	public static void setServerAddress(SocketAddress newAddress){
		if (SERVER_ADDRESS == null){
			SERVER_ADDRESS = new ThreadLocal<SocketAddress>();
		}
		SERVER_ADDRESS.set(newAddress);
	}
	
	public static SocketAddress getServerAddress(){
		if (SERVER_ADDRESS == null){
			return null;
		}
		return SERVER_ADDRESS.get();
	}
	
	public static void clean(){
		SENSOR_DEVICE = null;
		SERVER_ADDRESS = null;
	}

}
