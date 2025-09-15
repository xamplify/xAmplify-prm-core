package com.xtremand.util;


import java.security.SecureRandom;

import org.slf4j.LoggerFactory;


public final class GenerateRandomPassword {
	private  static final  org.slf4j.Logger logger = LoggerFactory.getLogger(GenerateRandomPassword.class); 
	String password;
	public GenerateRandomPassword(){
		try{
			SecureRandom wheel = SecureRandom.getInstance("SHA1PRNG");
			char[] alphaNumberic = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
					'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
					'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};

			StringBuilder password = new StringBuilder("");

			for (int i = 0; i < 8; i++) {
				int random = wheel.nextInt(alphaNumberic.length);
				password.append(alphaNumberic[random]);
			}

			logger.debug("Generated Password "+password);
			this.password = password.toString();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public String getPassword(){
		return password;
	}
	
}
