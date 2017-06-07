package ru.kolik.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.util.Base64;
import java.util.concurrent.CopyOnWriteArrayList;

public class CloudClientUtils {
	
	private static Socket server;
	private static DataOutputStream out;
	private static DataInputStream in;
	private static CopyOnWriteArrayList<Object[]> tasks = new CopyOnWriteArrayList<Object[]>();
	private static Path cloudDirectory;
	
	private static boolean connectServer() {
		try {
			if (server == null) {
				server = new Socket("localhost", 9999);
				out = new DataOutputStream(server.getOutputStream());
				in = new DataInputStream(server.getInputStream());
				return true;
			} else
				return true;
		} catch (IOException e) {
			System.err.println("Error connect server!!! " + e);
			return false;
		}
	}
	
	/*
	 * Основная команда для отправки
	 */
	private synchronized static boolean sendCommands(StandartCloudCommand command, Path... paths) throws IOException {
		switch (command) {
		case CREATE_DIRECTORY:
			return createDirectory(paths[0]);
		case DELETE:
			return delete(paths[0]);
		case CREATE_FILE:
			return createFile(paths[0]);
		case UPDATE_FILE:
			return updateFile(paths[0]);
		case RENAME_FILE:
			return renameFile(paths[0], paths[1]);
		default:
			return false;
		}
	}
	
	private synchronized static boolean createDirectory(Path path) throws IOException {
		Path child = cloudDirectory.relativize(path);
		String command = StandartCloudCommand.CREATE_DIRECTORY.name() + ">" + child.toString();
		out.writeUTF(encode(command.getBytes()));
		return in.readBoolean();
	}
	
	private synchronized static boolean createFile(Path path) {
		Path child = cloudDirectory.relativize(path);
		try {
			String command = StandartCloudCommand.CREATE_FILE.name() + ">" + child.toString() + ">" + path.toFile().length();
			out.writeUTF(encode(command.getBytes()));
			if (in.readBoolean())
				if (sendFile(path))
					if (in.readBoolean()) {
						System.out.println("success");
						return true;
					} else
						return false;
				else
					return false;
			else
				return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private synchronized static boolean delete(Path path) throws IOException {
		Path child = cloudDirectory.relativize(path);
		String command = StandartCloudCommand.DELETE.name() + ">" + child.toString();
		out.writeUTF(encode(command.getBytes()));
		return in.readBoolean();
	}
	
	private synchronized static boolean updateFile(Path path) throws IOException {
		Path child = cloudDirectory.relativize(path);
		String command = StandartCloudCommand.UPDATE_FILE.name() + ">" + child.toString() + ">" + path.toFile().length();
		out.writeUTF(encode(command.getBytes()));
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
		Path oldChild = cloudDirectory.relativize(oldPath);
		Path newChild = cloudDirectory.relativize(newPath);
		String command = StandartCloudCommand.RENAME_FILE.name() + ">" + oldChild.toString() + ">" + newChild.toString();
		out.writeUTF(encode(command.getBytes()));
		return in.readBoolean();
	}
	
	private synchronized static boolean sendFile(Path path) {
		try {
			FileInputStream fis = new FileInputStream(path.toFile());
			int length;
			byte[] buffer = new byte[1024];
			while ((length = fis.read(buffer)) != -1) {
				out.write(buffer, 0, length);
			}
			System.out.println("end file");
			fis.close();
			return true;
		} catch (IOException e) {
			System.err.println("Failed send file!! ");
			e.printStackTrace();
			return false;
		}
	}
	
	// private static byte[] decode(byte[] command) {
	// return Base64.getDecoder().decode(command);
	// }
	private synchronized static String encode(byte[] command) {
		return Base64.getEncoder().encodeToString(command);
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
			while (!server.isClosed())
				for (Object[] objects : tasks) {
					if (objects[0] instanceof StandartCloudCommand)
						if (objects[1] instanceof Path[]) {
							if (sendCommands((StandartCloudCommand) objects[0], (Path[]) objects[1])) {
								tasks.remove(objects);
								break;
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
		CloudClientUtils.cloudDirectory = cloudDirectory;
	}
}
