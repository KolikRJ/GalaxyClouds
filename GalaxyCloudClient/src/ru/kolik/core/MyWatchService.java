package ru.kolik.core;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import ru.kolik.util.CloudClientUtils;
import ru.kolik.util.StandartCloudCommand;

public class MyWatchService {
	
	private WatchService watch;
	private Map<WatchKey, Path> keys;
	private long sizeDir;
	
	public MyWatchService() throws IOException, InterruptedException {
		watch = FileSystems.getDefault().newWatchService();
		keys = new HashMap<WatchKey, Path>();
		walkAndRegisterDir(CloudClientUtils.getCloudDirectory());
	}
	
	private void registerDir(Path dir) throws IOException {
		WatchKey key = dir.register(watch, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
		keys.put(key, dir);
	}
	
	private void walkAndRegisterDir(Path start) throws IOException {
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				return FileVisitResult.CONTINUE;
			}
			
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				registerDir(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}
	
	private void create(Path child) {
		if (Files.isDirectory(child))
			CloudClientUtils.addTasks(StandartCloudCommand.CREATE_DIRECTORY, child);
		else
			CloudClientUtils.addTasks(StandartCloudCommand.CREATE_FILE, child);
	}
	
	private void delete(Path child) {
		CloudClientUtils.addTasks(StandartCloudCommand.DELETE, child);
	}
	
	private void update(Path child) {
		if (Files.isRegularFile(child))
			CloudClientUtils.addTasks(StandartCloudCommand.UPDATE_FILE, child);
	}
	
	public void startService() {
		try {
			for (;;) {
				WatchKey key = watch.take();
				Path dir = keys.get(key);
				for (WatchEvent<?> event : key.pollEvents()) {
					Path child = dir.resolve(event.context().toString());
					switch (event.kind().name()) {
					case "ENTRY_CREATE":
						create(child);
						walkAndRegisterDir(child);
						break;
					case "ENTRY_DELETE":
						delete(child);
						break;
					case "ENTRY_MODIFY":
						update(child);
						break;
					}
				}
				boolean valid = key.reset();
				if (!valid)
					keys.remove(key);
				if (keys.isEmpty())
					break;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
