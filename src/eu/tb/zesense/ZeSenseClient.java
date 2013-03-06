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
	ZePlayoutManager accelPlayoutManager;
	ZeAccelDisplayDevice accelDev;
	
	public static final String HOST = "coap://192.168.43.1:5683";
	public static final String ACCEL_RESOURCE_PATH = "/proximity";
	
	static boolean loop = false;

	public ZeSenseClient() {
	    setTitle("ZeSenseClient");
	    setSize(300, 200);
	    //setLocationRelativeTo(null);
	    setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	void zeSenseClient () {
	
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
		
		accelPlayoutManager = new ZePlayoutManager();
		accelPlayoutManager.master = masterPlayoutManager;
		accelPlayoutManager.playoutFreq = Registry.ACCEL_PLAYOUT_FREQ;
		accelPlayoutManager.playoutPer = Registry.ACCEL_PLAYOUT_PERIOD * 1000000;
		accelPlayoutManager.playoutHalfPer = Registry.ACCEL_PLAYOUT_HALF_PERIOD * 1000000;
		
		accelDev = new ZeAccelDisplayDevice();
		accelDev.playoutManager = accelPlayoutManager;
		accelDev.meter = meters;
		accelDev.start();
	    
		streams = new ArrayList<ZeStream>();
		
		
		CommunicatorFactory.getInstance().setUdpPort(48225);
		
		// initialize parameters
		String method = null;
		URI uri = null;
		String payload = null;
		
		boolean firstSR = true;
		//ZePlayoutBuffer<ZeAccelElement> a = new ZePlayoutBuffer<ZeAccelElement>();
		
		
		Log.setLevel(Level.ALL);
		Log.init();

		method = "OBSERVE";
		try {
			uri = new URI(HOST+ACCEL_RESOURCE_PATH);
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		// check if mandatory parameters specified
		if (method == null) {
			System.err.println("Method not specified");
			System.exit(Registry.ERR_MISSING_METHOD);
		}
		if (uri == null) {
			System.err.println("URI not specified");
			System.exit(Registry.ERR_MISSING_URI);
		}
		
		// create request according to specified method
		Request request = newRequest(method);
		if (request == null) {
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
			request.setOption(new Option(0, OptionNumberRegistry.OBSERVE));
			loop = true;
		}
		
		// cook the request
		request.setURI(uri);
		request.setPayload(payload);
		byte[] sendToken = TokenManager.getInstance().acquireToken();
		request.setToken( sendToken );
		request.setContentType(MediaTypeRegistry.TEXT_PLAIN);
		
		// enable response queue in order to use blocking I/O
		request.enableResponseQueue(true);		
		
		// register a new stream associated with this token
		streams.add(new ZeStream(sendToken, ACCEL_RESOURCE_PATH, Registry.ACCEL_STREAM_FREQ));
		
		request.prettyPrint();
		// execute request
		try {
			request.execute();
		} catch (UnknownHostException e) {
			System.err.println("Unknown host: " + e.getMessage());
			System.exit(Registry.ERR_REQUEST_FAILED);
		} catch (IOException e) {
			System.err.println("Failed to execute request: " + e.getMessage());
			System.exit(Registry.ERR_REQUEST_FAILED);
		}
		
		// loop for receiving multiple responses
		do {
	
			// receive response
			System.out.println("Receiving response...");
			Response response = null;
			try {
				response = request.receiveResponse();
			} catch (InterruptedException e) {
				System.err.println("Failed to receive response: " + e.getMessage());
				System.exit(Registry.ERR_RESPONSE_FAILED);
			}
	
			// process response
			if (response != null) {
				
				// get token and corresponding resource to identify the stream
				byte[] recToken = response.getToken();
				String recResource = response.getRequest().getUriPath();
				System.out.println("Token:"+new String(recToken)+" resource:"+recResource);
				ZeStream recStream = findStream(streams, recToken, recResource);
				boolean streamFound = false;
				if (recStream != null) streamFound = true;
				
				// check if it can be part of any stream
				ArrayList<Option> observeOptList = 
						(ArrayList<Option>) response.getOptions(OptionNumberRegistry.OBSERVE);
				boolean isNotification = false;
				if ( !observeOptList.isEmpty() ) isNotification = true;
				
				byte[] pay = response.getPayload();
				DataInputStream dataStream = new DataInputStream(new ByteArrayInputStream(pay));
				boolean validLength = false;
				if (pay.length >= Registry.PAYLOAD_HDR_LENGTH)
					validLength = true;
				
				if (streamFound && isNotification && validLength) {

					try {
						
						byte packetType = dataStream.readByte();
						byte sensorType = dataStream.readByte();
						short length = dataStream.readShort();
						
						Option observeOpt = observeOptList.get(0);
						int sequenceNumber = observeOpt.getIntValue();
						
						if (packetType == Registry.DATAPOINT) {
							
							int timestamp = dataStream.readInt();
		
							if (sensorType == Registry.SENSOR_TYPE_ACCELEROMETER) {
								
								ZeAccelElement event = new ZeAccelElement();
								event.x = Float.parseFloat(new String(Arrays.copyOfRange(pay, 8, 27)));
								event.y = Float.parseFloat(new String(Arrays.copyOfRange(pay, 28, 47)));
								event.z = Float.parseFloat(new String(Arrays.copyOfRange(pay, 48, 67)));
								event.timestamp = timestamp;
								event.sequenceNumber = sequenceNumber;
								event.meaning = Registry.PLAYOUT_VALID;
								
								recStream.registerArrival(event);
								
								System.out.println("packet:"+packetType+
										" sensor:"+sensorType+
										" length:"+length+
										" ts:"+timestamp+
										" sn"+sequenceNumber+
										" x:"+event.x+
										" y:"+event.y+
										" z:"+event.z);
								
								/* Cannot send to playout if I haven't got at least
								 * an Sender Report with timing mapping. Although
								 * conceptually this evaluation should be moved
								 * inside the playout manager.. */
								if (recStream.timingReady) {
									recStream.toWallclock(event);
									/* Yes the playouts should belong to a stream... */
									accelPlayoutManager.add(event);
									meters.accelBufferSeries.add(meters.accelBufferSeries.getItemCount()+1,
											accelPlayoutManager.size());
								}
								else
									System.out.println("Not sending to playout, timing still unknown.");
								
								if (recStream.packetsReceived == 40) {
									masterPlayoutManager.mpo+=500000000L;
								}
							}
		
						}
						else if (packetType == Registry.SENDREPORT) {
							
							long ntpts = dataStream.readLong();							
							int rtpts = dataStream.readInt();
							int packetCount = dataStream.readInt();
							int octectCount = dataStream.readInt();
							byte[] cname = new byte[length];
							dataStream.readFully(cname);
							System.out.println("packet:"+packetType+
									" sensor:"+sensorType+
									" length:"+length+
									" sn"+sequenceNumber+
									" ntpts:"+ntpts+
									" rtpts:"+rtpts+
									" packetCount:"+packetCount+
									" octectCount:"+octectCount+
									" cname:"+new String(cname));
							
							if (firstSR) {
								firstSR = false;
								long blindDelay = 1000000000L;
								masterPlayoutManager.mpo = System.nanoTime() + blindDelay - ntpts;
							}
							
							recStream.updateTiming(rtpts, ntpts);
							recStream.octectCount = octectCount;
							recStream.packetCount = packetCount;
						}
						else 
							System.out.println("Unknown payload format, drop.");
					
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
				else if ( !streamFound && isNotification && validLength) {
					System.out.println("Stream record not found but packet seems well formed.");
				}
				else {
					System.out.println("Some problems in the response..");
				}

				/*
				//DataInputStream stream = new DataInputStream(new ByteArrayInputStream(pay));
				//byte[] z = new byte[4];
				//stream.skipBytes(39);
				//stream.read(z, 36, 3);
				*/
				System.out.println("Time elapsed (ms): " + response.getRTT());

				//response.prettyPrint();
				
				// check of response contains resources
				if (response.getContentType()==MediaTypeRegistry.APPLICATION_LINK_FORMAT) {

					String linkFormat = response.getPayloadString();

					// create resource three from link format
					Resource root = RemoteResource.newRoot(linkFormat);
					if (root != null) {

						// output discovered resources
						System.out.println("\nDiscovered resources:");
						root.prettyPrint();

					} else {
						System.err.println("Failed to parse link format");
						System.exit(Registry.ERR_BAD_LINK_FORMAT);
					}
				} else {

					// check if link format was expected by client
					if (method.equals("DISCOVER")) {
						System.out.println("Server error: Link format not specified");
					}
				}

			} else {

				// no response received	
				System.err.println("Request timed out");
				break;
			}

		} while (loop);
		

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
