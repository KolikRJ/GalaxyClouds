package ru.kolik.object;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CloudServer implements Runnable {
	
	private ServerSocket server;
	private List<Client> clients;
	private Thread thread;
	private Path cloudDirectory;
	
	public boolean createServer() {
		try {
			if (server == null) {
				cloudDirectory = Paths.get("D:\\CloudServer");
				server = new ServerSocket(9999);
				clients = new ArrayList<Client>();
				if (thread == null) {
					thread = new Thread(this, "First thread");
					thread.start();
				}
				return true;
			} else
				return true;
		} catch (IOException e) {
			System.err.println("Error create server!!! " + e);
			return false;
		}
	}
	
	public void closeServer() {
		for (Client socket : clients) {
			socket.close();
			clients.remove(socket);
		}
		try {
			server.close();
		} catch (IOException e) {
			System.out.println("Error close serverSocket! " + e);
		}
	}
	
	@Override
	public void run() {
		while (!server.isClosed()) {
			try {
				clients.add(new Client(server.accept(), clients.size() + 1, cloudDirectory));
				System.out.println(clients.size());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
