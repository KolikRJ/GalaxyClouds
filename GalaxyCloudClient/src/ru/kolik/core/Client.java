package ru.kolik.core;

import java.io.IOException;
import java.nio.file.Paths;

import ru.kolik.util.CloudClientUtils;

public class Client {
	
	public static void main(String[] args) throws IOException, InterruptedException {
		CloudClientUtils.setCloudDirectory(Paths.get("D:\\CloudClient"));
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					CloudClientUtils.startService();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
		new MyWatchService().startService();
	}
}
