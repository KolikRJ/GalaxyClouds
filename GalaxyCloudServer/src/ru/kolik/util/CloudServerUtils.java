package ru.kolik.util;

import java.util.Base64;

public class CloudServerUtils {
	
	public static String decode(String command) {
		return new String(Base64.getDecoder().decode(command));
	}
}
