/*******************************************************************************
 * Copyright (c) 2005, 2010 Stein K. Skytteren and Christian Schwarz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Stein K. Skytteren and Christian Schwarz - initial API and implementation
 *******************************************************************************/
package org.cubictest.runner.selenium.server.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import javax.net.ServerSocketFactory;

import org.cubictest.exporters.selenium.ElementContext;
import org.cubictest.selenium.custom.ICustomTestStep;
import org.cubictest.selenium.custom.IElementContext;

import com.thoughtworks.selenium.Selenium;

/**
 * 
 * @author Christian Schwarz
 *
 */
public class CubicTestRemoteRunnerServer implements Runnable{
	
	private final int port;
	private Selenium selenium;
	private boolean finished = false;
	private IElementContext context;

	public CubicTestRemoteRunnerServer(int port, int seleniumClientProxyPort) {
		this.port = port;
		try {
			selenium = new CubicSelenium(seleniumClientProxyPort);
		} catch (Exception e) {
			System.err.println("Could not connect to Selenium through CubicTest");
			e.printStackTrace();
		}
	}

	/**
	   * Server program for the "Hello, world!" example.
	   * @param argv The command line arguments which are ignored.
	   */
	public static void main (String[] argv) {
		try {
			int port = -1;
			int seleniumClientProxyPort = -1;
			for(String arg : argv) {
				if(arg.startsWith("-port:")) {
					port = Integer.parseInt(arg.substring(6));
				}else if(arg.startsWith("-seleniumClientProxyPort:")){
					seleniumClientProxyPort = Integer.parseInt(arg.substring(25));
				}
			}
			if(port < 0 || seleniumClientProxyPort < 0 ){
				return;
			}
			CubicTestRemoteRunnerServer server = 
				new CubicTestRemoteRunnerServer(port, seleniumClientProxyPort);
			new Thread(server).start();
		} catch (Exception e) {
			System.err.println("Error setting up the launcher");
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			ServerSocket serverSocket = 
				ServerSocketFactory.getDefault().createServerSocket(port);
			Socket socket = serverSocket.accept();
			while (!finished ){
				InputStreamReader isr = new InputStreamReader(socket.getInputStream());
				BufferedReader br = new BufferedReader(isr);
				String commando = br.readLine();

				int numParams = Integer.parseInt(br.readLine());
				
				String[] params = new String[numParams];
				for(int i = 0; i < numParams; i++){
					String value = br.readLine();
					params[i] = value;
				}
				
				String result = execute(commando, params);
				OutputStreamWriter writer = 
					new OutputStreamWriter(socket.getOutputStream());
				writer.write(result + "\n");
				writer.flush();
			}
			socket.close();
		} catch (IOException e) {
			System.err.println("Error communicating with CubicTest");
			e.printStackTrace();
		}
	}
	
	public String execute(String command, String... values){
		if("cubicTestCustomStep".equals(command)){
			try {
				ICustomTestStep testStep = (ICustomTestStep) Class.forName(values[0]).newInstance();
				if(context == null){
					context = new ElementContext();
				}
				Map<String, String> arguments = new HashMap<String, String>();
				for(int i = 1 ; i < values.length ; i += 2){
					arguments.put(values[i], values[i+1]);
				}
				testStep.execute(arguments, context, selenium);
				return "OK";
			}
			catch (Exception e) {
				System.err.println("Exception in custom test step \"" + values[0] + "\":");
				e.printStackTrace();
				return "Error: " + e;
			}
			catch (AssertionError e) {
				System.err.println(values[0] + ": " + e.toString());
				e.printStackTrace();
				return "Failure: " + e;
			}
		}else if("stop".equals(command)){
			finished = true;
			return "Closed";
		}
		return "Not Valid Command";
	}
}
