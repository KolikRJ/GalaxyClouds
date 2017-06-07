package ru.kolik.core;

import java.io.IOException;

import ru.kolik.object.CloudServer;

public class Server {
	
	public static void main(String args[]) throws IOException {
		CloudServer serv = new CloudServer();
		serv.createServer();
	}
}
