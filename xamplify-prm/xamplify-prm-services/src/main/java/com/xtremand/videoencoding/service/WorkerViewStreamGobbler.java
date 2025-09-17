package com.xtremand.videoencoding.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WorkerViewStreamGobbler implements Runnable {
	private  static final  Logger logger = LoggerFactory.getLogger(WorkerViewStreamGobbler.class);
    String name;
    InputStream is;

    Thread thread;
    FFMPEGStatus status;
    
    public WorkerViewStreamGobbler (String name, InputStream is, FFMPEGStatus status) {
        this.name = name;
        this.is = is;
        this.status = status;
    }
    public void start () {
        thread = new Thread (this);
        thread.start ();
    }

    @Override
    public void run () {
        try {
            InputStreamReader isr = new InputStreamReader (is);
            BufferedReader br = new BufferedReader (isr);

            while (true) {
            	String s = br.readLine ();
            	if (s == null) 
            		break;
            	status.setStatus(s+"\n");
            }
            is.close ();

        } catch (Exception ex) {
        	logger.debug("Problem reading stream " + name + "... :" + ex);
        }finally{
        	if(is != null){
        		try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        }
        
    }
}
