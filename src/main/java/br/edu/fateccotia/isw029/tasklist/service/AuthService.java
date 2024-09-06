package br.edu.fateccotia.isw029.tasklist.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.edu.fateccotia.isw029.tasklist.model.Token;
import br.edu.fateccotia.isw029.tasklist.model.User;
import br.edu.fateccotia.isw029.tasklist.repository.TokenRepository;
import br.edu.fateccotia.isw029.tasklist.repository.UserRepository;

@Service
public class AuthService {
	private Integer TOKEN_TTL = 60; // in seconds
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private TokenRepository tokenRepository;
	
	public void signup(String email, String password) throws Exception {
		User user = new User();
		user.setEmail(email);
		user.setPassword(generateHash(password));
		
		Optional<User> userFound = userRepository.findByEmail(email);
		if (userFound.isPresent()) {
			throw new Exception("Email already exists");
		} else {
			userRepository.save(user);
		}
	}
		
	

	private String generateHash(String password) {
		
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(password.getBytes());
			byte[] digest = md.digest();
			return toHex(digest);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private String toHex(byte[] digest) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < digest.length; i++) {
			sb.append(
				Integer.toString(
						digest[i]& 0xff + 0x100, 16
						).substring(1)
				);
		}
		return sb.toString();
	}



	public Token signin(String email, String password) {
		User user = new User();
		user.setEmail(email);
		user.setPassword(generateHash(password));
		
		Optional<User> found = userRepository.findByEmail(email);
		if (found.isPresent() && found.get().getPassword().equals(user.getPassword())) {
			Token token = new Token();
			token.setUser(found.get());
			token.setToken(UUID.randomUUID().toString());
			token.setExpirationTime(Instant.now().plusSeconds(TOKEN_TTL).toEpochMilli());
			tokenRepository.save(token);
			return token;
		}
		return null;
	}
	
	public void signout(String token) {
		Optional<Token> found = tokenRepository.findByToken(token);
		found.ifPresent(t -> {
			t.setExpirationTime(Instant.now().toEpochMilli()); 
			tokenRepository.save(t);
		});
		
	}
	
	public Boolean validate(String token) {
		Optional<Token> found = tokenRepository.findByToken(token);
		return found.isPresent() 
				&&
				found.get().getExpirationTime() 
				> Instant.now().toEpochMilli();
	}
}
