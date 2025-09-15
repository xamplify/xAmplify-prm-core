package com.xtremand.util;

import org.springframework.stereotype.Component;

@Component
public class EmailValidatorUtil {

	
	public boolean validate(String emailAddress)
	{
	    boolean result = false;
	    
	    CustomEmailValidator emailValidator = new CustomEmailValidator();
	    if(emailValidator.validate(emailAddress)) 
	    {
	        result = true;
	    }
	  
	    return result;
	}
}
