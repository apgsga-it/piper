package com.apgsga.microservice.patch.groovy.crypto.util;

import java.util.Scanner;

import org.jasypt.util.text.BasicTextEncryptor;

public class Crypto {

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("encrypt key:");
		String encryptKey = scanner.next();
		System.out.println("Password:");
		String pwd = scanner.next();
		scanner.close();
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(encryptKey);
		String encryptedPwd = textEncryptor.encrypt(pwd);
		System.out.println("Encrypted pwd to be provided to OPS: " + encryptedPwd);
	}
}
