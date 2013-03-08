/*******************************************************************************
 * Copyright (c) 2012, Institute for Pervasive Computing, ETH Zurich.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * 
 * This file is part of the Californium (Cf) CoAP framework.
 ******************************************************************************/

/**
 * This class implements a simple CoAP client for testing purposes. Usage:
 * <p>
 * {@code java -jar SampleClient.jar [-l] METHOD URI [PAYLOAD]}
 * <ul>
 * <li>METHOD: {GET, POST, PUT, DELETE, DISCOVER, OBSERVE}
 * <li>URI: The URI to the remote endpoint or resource}
 * <li>PAYLOAD: The data to send with the request}
 * </ul>
 * Options:
 * <ul>
 * <li>-l: Loop for multiple responses}
 * </ul>
 * Examples:
 * <ul>
 * <li>{@code SampleClient DISCOVER coap://localhost}
 * <li>{@code SampleClient POST coap://someServer.org:5683 my data}
 * </ul>
 *  
 * @author Dominique Im Obersteg, Daniel Pauli, and Matthias Kovatsch
 */

package eu.tb.zesense;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.ui.RefineryUtilities;

import ch.ethz.inf.vs.californium.coap.CommunicatorFactory;
import ch.ethz.inf.vs.californium.coap.CommunicatorFactory.Communicator;
import ch.ethz.inf.vs.californium.coap.DELETERequest;
import ch.ethz.inf.vs.californium.coap.GETRequest;
import ch.ethz.inf.vs.californium.coap.Option;
import ch.ethz.inf.vs.californium.coap.POSTRequest;
import ch.ethz.inf.vs.californium.coap.PUTRequest;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.coap.TokenManager;
import ch.ethz.inf.vs.californium.coap.registries.MediaTypeRegistry;
import ch.ethz.inf.vs.californium.coap.registries.OptionNumberRegistry;
import ch.ethz.inf.vs.californium.endpoint.resources.RemoteResource;
import ch.ethz.inf.vs.californium.endpoint.resources.Resource;
import ch.ethz.inf.vs.californium.util.Log;

public class ZeSenseClient extends JFrame {

	//long mpo; //master playout dynamic offset
	//calculated as the offset from the common sender
	//time and the playout time of that instant in common time
	
	ZeMeters meters;
	ZeMasterPlayoutManager masterPlayoutManager;
	
	ArrayList<ZeStream> streams;
	
	ZePlayoutManager<ZeAccelElement> accelPlayoutManager;
	ZeAccelDisplayDevice accelDev;
	
	ZePlayoutManager<ZeProxElement> proxPlayoutManager;
	ZeProxDisplayDevice proxDev;
	
	ZePlayoutManager<ZeLightElement> lightPlayoutManager;
	ZeLightDisplayDevice lightDev;
	
	public static final String HOST = "coap://192.168.43.1:5683";
	public static final String ACCEL_RESOURCE_PATH = "/accel";
	public static final String PROX_RESOURCE_PATH = "/proximity";
	public static final String LIGHT_RESOURCE_PATH = "/light";
	
	static boolean loop = false;

	public ZeSenseClient() {
	    setTitle("ZeSenseClient");
	    setSize(300, 200);
	    //setLocationRelativeTo(null);
	    setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	void zeSenseClient () {
		
		System.out.println("This thread:"+Thread.currentThread().getId());
	
		JPanel panel = new JPanel();
		getContentPane().add(panel);
		panel.setLayout(null);
		JButton quitButton = new JButton("Quit");
		quitButton.setBounds(50, 60, 80, 30);
		quitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				loop = false;
			}
		});
		panel.add(quitButton);
		setVisible(true);
		
		meters = new ZeMeters("ZeSense Monitors Panel");
	    meters.pack();
	    RefineryUtilities.centerFrameOnScreen(meters);
	    meters.setVisible(true);
	    
	    masterPlayoutManager = new ZeMasterPlayoutManager();
		
		accelPlayoutManager = new ZePlayoutManager<ZeAccelElement>();
		accelPlayoutManager.master = masterPlayoutManager;
		accelPlayoutManager.playoutFreq = Registry.ACCEL_PLAYOUT_FREQ;
		accelPlayoutManager.playoutPer = Registry.ACCEL_PLAYOUT_PERIOD * 1000000;
		accelPlayoutManager.playoutHalfPer = Registry.ACCEL_PLAYOUT_HALF_PERIOD * 1000000;
		
		accelDev = new ZeAccelDisplayDevice();
		accelDev.playoutManager = accelPlayoutManager;
		accelDev.meter = meters;
		accelDev.start();
		
		proxPlayoutManager = new ZePlayoutManager<ZeProxElement>();
		proxPlayoutManager.master = masterPlayoutManager;
		proxPlayoutManager.playoutFreq = Registry.PROX_PLAYOUT_FREQ;
		proxPlayoutManager.playoutPer = Registry.PROX_PLAYOUT_PERIOD * 1000000;
		proxPlayoutManager.playoutHalfPer = Registry.PROX_PLAYOUT_HALF_PERIOD * 1000000;
		
		proxDev = new ZeProxDisplayDevice();
		proxDev.playoutManager = proxPlayoutManager;
		proxDev.meter = meters;
		proxDev.start();
		
		lightPlayoutManager = new ZePlayoutManager<ZeLightElement>();
		lightPlayoutManager.master = masterPlayoutManager;
		lightPlayoutManager.playoutFreq = Registry.LIGHT_PLAYOUT_FREQ;
		lightPlayoutManager.playoutPer = Registry.LIGHT_PLAYOUT_PERIOD * 1000000;
		lightPlayoutManager.playoutHalfPer = Registry.LIGHT_PLAYOUT_HALF_PERIOD * 1000000;
		
		lightDev = new ZeLightDisplayDevice();
		lightDev.playoutManager = lightPlayoutManager;
		lightDev.meter = meters;
		lightDev.start();
		
		streams = new ArrayList<ZeStream>();
		
		
		CommunicatorFactory.getInstance().setUdpPort(48225);
		
		// initialize parameters
		String method = null;
		URI uri = null;
		String payload = null;
		
		boolean firstSR = true;
		//ZePlayoutBuffer<ZeAccelElement> a = new ZePlayoutBuffer<ZeAccelElement>()
		
		
		Log.setLevel(Level.ALL);
		Log.init();


		/*
		// check if mandatory parameters specified
		if (method == null) {
			System.err.println("Method not specified");
			System.exit(Registry.ERR_MISSING_METHOD);
		}
		if (uri == null) {
			System.err.println("URI not specified");
			System.exit(Registry.ERR_MISSING_URI);
		}
		*/
		/*----------------------------------------------------------------------*/
		
		method = "OBSERVE";
		try {
			uri = new URI(HOST+LIGHT_RESOURCE_PATH);
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		// create request according to specified method
		Request lightRequest = new GETRequest() {
			@Override
			protected void handleResponse(Response response) {
				System.out.println(" !!!!!!!!!!light TID:"+Thread.currentThread().getId());
			}
		};
		if (lightRequest == null) {
			System.err.println("Unknown method: " + method);
			System.exit(Registry.ERR_UNKNOWN_METHOD);
		}
		// set request URI
		if (method.equals("DISCOVER") && (uri.getPath() == null || uri.getPath().isEmpty() || uri.getPath().equals("/"))) {
			// add discovery resource path to URI
			try {
				uri = new URI(uri.getScheme(), uri.getAuthority(), Registry.DISCOVERY_RESOURCE, uri.getQuery());
				
			} catch (URISyntaxException e) {
				System.err.println("Failed to parse URI: " + e.getMessage());
				System.exit(Registry.ERR_BAD_URI);
			}
		}
		// if we want to observe, set the option
		if (method.equals("OBSERVE")) {
			lightRequest.setOption(new Option(0, OptionNumberRegistry.OBSERVE));
			loop = true;
		}
		// cook the request
		lightRequest.setURI(uri);
		lightRequest.setPayload(payload);
		byte[] lightToken = TokenManager.getInstance().acquireToken();
		lightRequest.setToken( lightToken );
		lightRequest.setContentType(MediaTypeRegistry.TEXT_PLAIN);
		// register a new stream associated with this token
		streams.add(new ZeStream(lightToken, LIGHT_RESOURCE_PATH, Registry.LIGHT_STREAM_FREQ));
		// enable response queue in order to use blocking I/O
		lightRequest.enableResponseQueue(true);		
		lightRequest.prettyPrint();
		// execute request
		try {
			lightRequest.execute();
		} catch (UnknownHostException e) {
			System.err.println("Unknown host: " + e.getMessage());
			System.exit(Registry.ERR_REQUEST_FAILED);
		} catch (IOException e) {
			System.err.println("Failed to execute request: " + e.getMessage());
			System.exit(Registry.ERR_REQUEST_FAILED);
		}
		
		/*----------------------------------------------------------------------*/
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
		
		/*----------------------------------------------------------------------*/
		
		method = "OBSERVE";
		try {
			uri = new URI(HOST+PROX_RESOURCE_PATH);
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		// create request according to specified method
		Request proxRequest = new GETRequest() {
			@Override
			protected void handleResponse(Response response) {
				System.out.println(" !!!!!!!!!!prox TID:"+Thread.currentThread().getId());
			}
		};
		if (proxRequest == null) {
			System.err.println("Unknown method: " + method);
			System.exit(Registry.ERR_UNKNOWN_METHOD);
		}
		// set request URI
		if (method.equals("DISCOVER") && (uri.getPath() == null || uri.getPath().isEmpty() || uri.getPath().equals("/"))) {
			// add discovery resource path to URI
			try {
				uri = new URI(uri.getScheme(), uri.getAuthority(), Registry.DISCOVERY_RESOURCE, uri.getQuery());
				
			} catch (URISyntaxException e) {
				System.err.println("Failed to parse URI: " + e.getMessage());
				System.exit(Registry.ERR_BAD_URI);
			}
		}
		// if we want to observe, set the option
		if (method.equals("OBSERVE")) {
			proxRequest.setOption(new Option(0, OptionNumberRegistry.OBSERVE));
			loop = true;
		}
		// cook the request
		proxRequest.setURI(uri);
		proxRequest.setPayload(payload);
		byte[] proxToken = TokenManager.getInstance().acquireToken();
		proxRequest.setToken( proxToken );
		proxRequest.setContentType(MediaTypeRegistry.TEXT_PLAIN);
		// register a new stream associated with this token
		streams.add(new ZeStream(proxToken, PROX_RESOURCE_PATH, Registry.PROX_STREAM_FREQ));
		// enable response queue in order to use blocking I/O
		proxRequest.enableResponseQueue(true);		
		proxRequest.prettyPrint();
		// execute request
		try {
			proxRequest.execute();
		} catch (UnknownHostException e) {
			System.err.println("Unknown host: " + e.getMessage());
			System.exit(Registry.ERR_REQUEST_FAILED);
		} catch (IOException e) {
			System.err.println("Failed to execute request: " + e.getMessage());
			System.exit(Registry.ERR_REQUEST_FAILED);
		}
		
		/*----------------------------------------------------------------------*/
		try {
			Thread.sleep(500);
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
		/*---------------------------------------------------------------------*/
		method = "OBSERVE";
		try {
			uri = new URI(HOST+ACCEL_RESOURCE_PATH);
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}		
		// create request according to specified method
		Request accelRequest = new GETRequest() {
			@Override
			protected void handleResponse(Response response) {
				System.out.println(" !!!!!!!!!!accel TID:"+Thread.currentThread().getId());
			}
		};
		if (accelRequest == null) {
			System.err.println("Unknown method: " + method);
			System.exit(Registry.ERR_UNKNOWN_METHOD);
		}
		// set request URI
		if (method.equals("DISCOVER") && (uri.getPath() == null || uri.getPath().isEmpty() || uri.getPath().equals("/"))) {
			// add discovery resource path to URI
			try {
				uri = new URI(uri.getScheme(), uri.getAuthority(), Registry.DISCOVERY_RESOURCE, uri.getQuery());
				
			} catch (URISyntaxException e) {
				System.err.println("Failed to parse URI: " + e.getMessage());
				System.exit(Registry.ERR_BAD_URI);
			}
		}
		// if we want to observe, set the option
		if (method.equals("OBSERVE")) {
			accelRequest.setOption(new Option(0, OptionNumberRegistry.OBSERVE));
			loop = true;
		}
		// cook the request
		accelRequest.setURI(uri);
		accelRequest.setPayload(payload);
		byte[] accelToken = TokenManager.getInstance().acquireToken();
		accelRequest.setToken( accelToken );
		accelRequest.setContentType(MediaTypeRegistry.TEXT_PLAIN);
		// register a new stream associated with this token
		streams.add(new ZeStream(accelToken, ACCEL_RESOURCE_PATH, Registry.ACCEL_STREAM_FREQ));
		// enable response queue in order to use blocking I/O
		accelRequest.enableResponseQueue(true);		
		accelRequest.prettyPrint();
		// execute request
		try {
			accelRequest.execute();
		} catch (UnknownHostException e) {
			System.err.println("Unknown host: " + e.getMessage());
			System.exit(Registry.ERR_REQUEST_FAILED);
		} catch (IOException e) {
			System.err.println("Failed to execute request: " + e.getMessage());
			System.exit(Registry.ERR_REQUEST_FAILED);
		}
		/*----------------------------------------------------------------------*/

		int selector = 1;
		
		/*------> HERE <---------*/
		

		// Print out some stats
		
		
		// finish
		System.out.println();
	}
	
	/*
	 * Outputs user guide of this program.
	 */
	public static void printInfo() {
		System.out.println("Californium (Cf) Example Client");
		System.out.println("(c) 2012, Institute for Pervasive Computing, ETH Zurich");
		System.out.println();
		System.out.println("Usage: " + ZeSenseClient.class.getSimpleName() + " [-l] METHOD URI [PAYLOAD]");
		System.out.println("  METHOD  : {GET, POST, PUT, DELETE, DISCOVER, OBSERVE}");
		System.out.println("  URI     : The CoAP URI of the remote endpoint or resource");
		System.out.println("  PAYLOAD : The data to send with the request");
		System.out.println("Options:");
		System.out.println("  -l      : Loop for multiple responses");
		System.out.println("           (automatic for OBSERVE and separate responses)");
		System.out.println();
		System.out.println("Examples:");
		System.out.println("  ExampleClient DISCOVER coap://localhost");
		System.out.println("  ExampleClient POST coap://vs0.inf.ethz.ch:5683/storage my data");
	}

	/*
	 * Instantiates a new request based on a string describing a method.
	 * 
	 * @return A new request object, or null if method not recognized
	 */
	private static Request newRequest(String method) {
		if (method.equals("GET")) {
			return new GETRequest();
		} else if (method.equals("POST")) {
			return new POSTRequest();
		} else if (method.equals("PUT")) {
			return new PUTRequest();
		} else if (method.equals("DELETE")) {
			return new DELETERequest();
		} else if (method.equals("DISCOVER")) {
			return new GETRequest();
		} else if (method.equals("OBSERVE")) {
			return new GETRequest();
		} else {
			return null;
		}
	}
	
	ZeStream findStream(ArrayList<ZeStream> list, byte[] token, String resource) {
		for (ZeStream s : list) {
			System.out.println("Iter");
		    if (Arrays.equals(s.token, token) && s.resource.equals(resource))
		    	return s;
		}
		return null;
	}

}
