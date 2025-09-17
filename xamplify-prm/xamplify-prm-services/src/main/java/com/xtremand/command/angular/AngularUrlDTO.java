package com.xtremand.command.angular;

public class AngularUrlDTO {
	
	public boolean authorizeUrl(AngularUrlCommand angularUrlCommand) {
		return angularUrlCommand.isAuthorizedUrl();
	}

}
