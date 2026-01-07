package com.kt.utils;

public class PrivacyUtils {
	public static String maskName(String name) {
		if (name == null || name.length() < 2) return name;

		if (name.length() == 2) {
			return name.charAt(0) + "*";
		}
		return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1);
	}
}