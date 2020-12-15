package com.apgsga.artifact.query;

import java.util.List;

import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.repository.RemoteRepository;
import org.jasypt.util.text.BasicTextEncryptor;

import com.apgsga.artifact.query.impl.RepositorySystemFactoryImpl;

public interface RepositorySystemFactory {
	
	static RepositorySystemFactory create(String baseUrl, String repoName, String user, String userPwd) {
		return new RepositorySystemFactoryImpl(user,baseUrl,repoName,userPwd);
	}
	
	static RepositorySystemFactory create(String baseUrl, String repoName, String user, String encryptedPassword, String decryptKey) {
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(decryptKey == null || decryptKey.isEmpty() ? "apgdevops" : decryptKey);
		String decryptedPwd = textEncryptor.decrypt(encryptedPassword);
		return create(baseUrl,repoName,user,decryptedPwd);
	}
	
	List<RemoteRepository> newRepositories();
	
	RepositorySystem newRepositorySystem();
	
	DefaultRepositorySystemSession newRepositorySystemSession(RepositorySystem system, String localRepoPath);
}
