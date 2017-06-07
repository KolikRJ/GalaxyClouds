package ru.kolik.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;

public class CloudCommand {
	
	private static Socket server;
	private static DataOutputStream out;
	private static DataInputStream in;
	private static ArrayList<Object[]> tasks = new ArrayList<Object[]>();
	private static Path cloudDirectory;
	
	private static boolean connectServer() {
		try {
			server = new Socket("localhost", 9999);
			out = new DataOutputStream(server.getOutputStream());
			in = new DataInputStream(server.getInputStream());
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	/*
	 * Основная команда для отправки
	 */
	private synchronized static boolean cloudCommands(StandartCloudCommand command, Path... paths) throws IOException {
		switch (command) {
		case CREATE_DIRECTORY:
			return createDirectory(paths[0]);
		case DELETE_DIRECTORY:
			return deleteDirectory(paths[0]);
		case CREATE_FILE:
			return createFile(paths[0]);
		case DELETE_FILE:
			return deleteFile(paths[0]);
		case UPDATE_FILE:
			return updateFile(paths[0]);
		case RENAME_FILE:
			return renameFile(paths[0], paths[1]);
		default:
			return false;
		}
	}
	
	private synchronized static boolean createDirectory(Path path) throws IOException {
		String command = StandartCloudCommand.CREATE_DIRECTORY.name() + ">" + path.toString();
		out.write(encode(command.getBytes()));
		return in.readBoolean();
	}
	
	private synchronized static boolean deleteDirectory(Path path) throws IOException {
		String command = StandartCloudCommand.DELETE_DIRECTORY.name() + ">" + path.toString();
		out.write(encode(command.getBytes()));
		return in.readBoolean();
	}
	
	private synchronized static boolean createFile(Path path) throws IOException {
		String command = StandartCloudCommand.CREATE_FILE.name() + ">" + path.toString() + ">" + path.toFile().length();
		out.write(encode(command.getBytes()));
		if (in.readBoolean())
			if (sendFile(path))
				if (in.readBoolean())
					return true;
				else
					return false;
			else
				return false;
		else
			return false;
	}
	
	private synchronized static boolean deleteFile(Path path) throws IOException {
		String command = StandartCloudCommand.DELETE_FILE.name() + ">" + path.toString();
		out.write(encode(command.getBytes()));
		return in.readBoolean();
	}
	
	private synchronized static boolean updateFile(Path path) throws IOException {
		String command = StandartCloudCommand.UPDATE_FILE.name() + ">" + path.toString() + ">" + path.toFile().length();
		out.write(encode(command.getBytes()));
		if (in.readBoolean())
			if (sendFile(path))
				if (in.readBoolean())
					return true;
				else
					return false;
			else
				return false;
		else
			return false;
	}
	
	private synchronized static boolean renameFile(Path oldPath, Path newPath) throws IOException {
		String command = StandartCloudCommand.RENAME_FILE.name() + ">" + oldPath.toString() + ">" + newPath.toString();
		out.write(encode(command.getBytes()));
		return in.readBoolean();
	}
	
	private synchronized static boolean sendFile(Path path) {
		try {
			FileInputStream fis = new FileInputStream(path.toFile());
			int length;
			byte[] buffer = new byte[1024];
			while ((length = fis.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}
			fis.close();
			return true;
		} catch (IOException e) {
			System.err.println("Failed send file!!" + e);
			return false;
		}
	}
	
	private static byte[] decode(byte[] command) {
		return Base64.getDecoder().decode(command);
	}
	
	private static byte[] encode(byte[] command) {
		return Base64.getEncoder().encode(command);
	}
	
	public static void addTasks(StandartCloudCommand command, Path... path) {
		Object[] object = new Object[] { command, path };
		tasks.add(object);
	}
	
	private static void stop() throws IOException {
		in.close();
		out.close();
		server.close();
	}
	
	/*
	 * Запускает службу по отправке данных на сервер
	 */
	public synchronized static void startService() throws IOException {
		if (connectServer())
			while (server.isConnected())
				for (Object[] objects : tasks) {
					if (objects[0] instanceof StandartCloudCommand)
						if (objects[1] instanceof Path[]) {
							if (cloudCommands((StandartCloudCommand) objects[0], (Path[]) objects[1])) {
								tasks.remove(objects);
							} else {
								System.err.println("Error command!! Stop connect server");
								stop();
								break;
							}
						} else
							System.err.println("File not Path[] class!!");
					else
						System.err.println("File not StandartCloudCommand class!!");
				}
		else
			System.err.println("Failed connect to server!");
	}
	
	public static Path getCloudDirectory() {
		return cloudDirectory;
	}
	
	public static void setCloudDirectory(Path cloudDirectory) {
		CloudCommand.cloudDirectory = cloudDirectory;
	}
}
