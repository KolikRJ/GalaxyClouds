package ru.kolik.core;

import java.io.IOException;
import java.nio.file.Paths;

public class Client {
	
	public static void main(String[] args) throws IOException, InterruptedException {
		CloudCommand.setCloudDirectory(Paths.get("D:\\CloudClient"));
		new MyWatchService().startService();
	}
}
