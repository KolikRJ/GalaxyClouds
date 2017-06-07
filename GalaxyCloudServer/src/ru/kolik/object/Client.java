package ru.kolik.object;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

import ru.kolik.util.CloudServerUtils;
import ru.kolik.util.StandartCloudCommand;

public class Client implements Runnable {
	
	private Socket client;
	private DataInputStream in;
	private DataOutputStream out;
	private Thread thread;
	private Path root;
	
	public Client(Socket client, int num, Path rootDirectory) {
		try {
			this.client = client;
			root = rootDirectory;
			in = new DataInputStream(client.getInputStream());
			out = new DataOutputStream(client.getOutputStream());
			if (thread == null) {
				thread = new Thread(this, "Client thread " + num);
				thread.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		thread.interrupt();
		try {
			in.close();
			out.close();
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void readCommands() throws IOException {
		String read = CloudServerUtils.decode(in.readUTF());
		String[] commands = read.split(">");
		StandartCloudCommand command = StandartCloudCommand.valueOf(commands[0]);
		Path newPath = root.resolve(commands[1]);
		Path oldPath = null;
		long fileLength = 0;
		if (commands.length == 3 && command == StandartCloudCommand.RENAME_FILE)
			oldPath = root.resolve(commands[2]);
		if (commands.length == 3)
			if (command == StandartCloudCommand.CREATE_FILE || command == StandartCloudCommand.UPDATE_FILE)
				fileLength = Long.parseLong(commands[2]);
		switch (command) {
		case CREATE_DIRECTORY:
			System.out.println(command.name() + " " + newPath);
			createDirectory(newPath);
			break;
		case CREATE_FILE:
			System.out.println(command.name() + " " + newPath + " " + fileLength);
			createFile(newPath, fileLength);
			break;
		case DELETE:
			System.out.println(command.name() + " " + newPath);
			delete(newPath);
			break;
		case UPDATE_FILE:
			break;
		case RENAME_FILE:
			break;
		}
	}
	
	private void createDirectory(Path path) {
		try {
			Files.createDirectory(path);
			out.writeBoolean(true);
		} catch (IOException e) {
			try {
				out.writeBoolean(false);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}
	
	private void createFile(Path path, long lengthFile) {
		try {
			out.writeBoolean(true);
			FileOutputStream fos = new FileOutputStream(path.toFile());
			int length = 0;
			byte[] buffer = new byte[1024];
			while (true) {
				length += in.read(buffer);
				if (length != lengthFile) {
					fos.write(buffer, 0, length);
					System.out.println(length);
				} else
					break;
			}
			fos.close();
			System.out.println("File copied");
			out.writeBoolean(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void delete(Path path) {
		try {
			Files.delete(path);
			out.writeBoolean(true);
		} catch (IOException e) {
			try {
				out.writeBoolean(false);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		while (client.isConnected())
			try {
				readCommands();
			} catch (Exception e) {
				close();
			}
	}
}
