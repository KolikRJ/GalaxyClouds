package ru.kolik.core;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Server {
	
	public static void main(String args[]) throws IOException {
		ServerSocket serv = new ServerSocket(9999);
		Socket client = serv.accept();
		System.out.println("Connect");
		System.out.println("accept command");
		Path path = Paths.get("D:\\CloudServer\\qwe.rar");
		System.out.println(path);
		System.out.println("Input file");
		FileOutputStream fos = new FileOutputStream(path.toFile());
		int length;
		byte[] buffer = new byte[1024];
		while ((length = client.getInputStream().read(buffer)) != -1) {
			System.out.println(length);
			fos.write(buffer, 0, length);
		}
		System.out.println("Accept file");
	}
}
