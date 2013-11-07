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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.concurrent.locks.Condition;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.ui.RefineryUtilities;

import ch.ethz.inf.vs.californium.coap.CommunicatorFactory;
import ch.ethz.inf.vs.californium.coap.GETRequest;
import ch.ethz.inf.vs.californium.coap.Message;
import ch.ethz.inf.vs.californium.coap.Option;
import ch.ethz.inf.vs.californium.coap.POSTRequest;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.coap.TokenManager;
import ch.ethz.inf.vs.californium.coap.registries.MediaTypeRegistry;
import ch.ethz.inf.vs.californium.coap.registries.OptionNumberRegistry;
import ch.ethz.inf.vs.californium.util.Log;


public class ZeSenseClient extends JFrame {

	/* master playout dynamic offset calculated as the offset
	 * from the common sender time and the playout time of
	 * that instant in common time
	 */
	
	ZeMeters meters;
	ZeMasterPlayoutManager masterPlayoutManager;
	
	ArrayList<ZeStream> streams;
	
	DatagramSocket testSocket;
	
	ZePlayoutManager<ZeAccelElement> accelPlayoutManager;
	HashMap<Integer, ZeSampleRegisterEntry> accelSampleRegister;
	ZeAccelDisplayDevice accelDev;
	ZeStream accelStream;
	
	ZePlayoutManager<ZeProxElement> proxPlayoutManager;
	HashMap<Integer, ZeSampleRegisterEntry> proxSampleRegister;
	ZeProxDisplayDevice proxDev;
	ZeStream proxStream;
	
	ZePlayoutManager<ZeLightElement> lightPlayoutManager;
	HashMap<Integer, ZeSampleRegisterEntry> lightSampleRegister;
	ZeLightDisplayDevice lightDev;
	ZeStream lightStream;
	
	ZePlayoutManager<ZeOrientElement> orientPlayoutManager;
	HashMap<Integer, ZeSampleRegisterEntry> orientSampleRegister;
	ZeOrientDisplayDevice orientDev;
	ZeStream orientStream;
	
	ZePlayoutManager<ZeGyroElement> gyroPlayoutManager;
	HashMap<Integer, ZeSampleRegisterEntry> gyroSampleRegister;
	ZeGyroDisplayDevice gyroDev;
	ZeStream gyroStream;
	
	static boolean loop = false;
	
	static int accelRRSent = 0;
	static int proxRRSent = 0;
	static int lightRRSent = 0;
	static int gyroRRSent = 0;
	static int orientRRSent = 0;
	
	static int accelSRRec = 0;
	static int proxSRRec  = 0;
	static int lightSRRec  = 0;
	static int gyroSRRec  = 0;
	static int orientSRRec  = 0;
	
	static int accelTotalNotifReceived = 0;
	static int accelDataNotifReceived = 0;
	static int accelDataNotifBeforeTiming = 0;
	static int accelBeforeRetransmissions = 0; //retransmitted packets that arrive before their original (those that arrive after their original are filtered by californium)
	static int accelArrivedDuplicates = 0;
	static int accelUsefulDuplicates = 0;
	static int accelBeforeDuplicates = 0;
	static int accelSamplesOnlyOriginal = 0;
	static int accelSamplesOnlyDuplicate = 0;
	
	static int proxTotalNotifReceived = 0;
	static int proxDataNotifReceived = 0;
	static int proxDataNotifBeforeTiming = 0;
	static int proxBeforeRetransmissions = 0;
	static int proxArrivedDuplicates = 0;
	static int proxUsefulDuplicates = 0;
	static int proxBeforeDuplicates = 0;
	static int proxSamplesOnlyOriginal = 0;
	static int proxSamplesOnlyDuplicate = 0;
	
	static int lightTotalNotifReceived = 0;
	static int lightDataNotifReceived = 0;
	static int lightDataNotifBeforeTiming = 0;
	static int lightBeforeRetransmissions = 0;
	static int lightArrivedDuplicates = 0;
	static int lightUsefulDuplicates = 0;
	static int lightBeforeDuplicates = 0;
	static int lightSamplesOnlyOriginal = 0;
	static int lightSamplesOnlyDuplicate = 0;
	
	static int gyroTotalNotifReceived = 0;
	static int gyroDataNotifReceived = 0;
	static int gyroDataNotifBeforeTiming = 0;
	static int gyroBeforeRetransmissions = 0;
	static int gyroArrivedDuplicates = 0;
	static int gyroUsefulDuplicates = 0;
	static int gyroBeforeDuplicates = 0;
	static int gyroSamplesOnlyOriginal = 0;
	static int gyroSamplesOnlyDuplicate = 0;
	
	static int rtCount = 0;

	public ZeSenseClient() {
	    setTitle("ZeSenseClient");
	    setSize(300, 200);
	    //setLocationRelativeTo(null);
	    setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	boolean firstSR = true;
	
	Condition globalExit;
	
	public class ZeAccelRecThread extends Thread {
		
		@Override
		public void run() {
			
			Thread.currentThread().setName("ZeAccelRecThread");
			
			System.out.println("Hello from thread "+Thread.currentThread().getName());
			
			/* Initialize playout manager. */
			accelPlayoutManager = new ZePlayoutManager<ZeAccelElement>();
			accelPlayoutManager.master = masterPlayoutManager;
			accelPlayoutManager.playoutFreq = Registry.ACCEL_PLAYOUT_FREQ;
			accelPlayoutManager.playoutPer = Registry.ACCEL_PLAYOUT_PERIOD * 1000000;
			accelPlayoutManager.playoutHalfPer = Registry.ACCEL_PLAYOUT_HALF_PERIOD * 1000000;
			
			/* Initialize samples register. */
			accelSampleRegister = new HashMap<Integer, ZeSampleRegisterEntry>();
			
			/* Tuno on display device. */
			accelDev = new ZeAccelDisplayDevice();
			accelDev.playoutManager = accelPlayoutManager;
			accelDev.meter = meters;
			accelDev.start();
			
			Request request = prepareObserveRequest(Registry.ACCEL_RESOURCE_PATH);
			accelStream = new ZeStream(request.getToken(), Registry.ACCEL_RESOURCE_PATH, Registry.ACCEL_STREAM_FREQ);
			streams.add(accelStream);
			executeRequest(request);
			
			while(loop) {
				
				Response response = null;
				
				try {
					
					response = request.receiveResponse();
					
					// get token and corresponding resource to identify the stream
					ZeStream recStream = findStream(streams, response.getToken(), response.getRequest().getUriPath());
					if (recStream != null)
						System.out.println("Stream found, token:"+new String(recStream.token)+" resource:"+recStream.resource);
					
					// check if it can be part of any stream
					ArrayList<Option> observeOptList = 
							(ArrayList<Option>) response.getOptions(OptionNumberRegistry.OBSERVE);
					
					// get payload
					byte[] pay = response.getPayload();
					DataInputStream dataStream = new DataInputStream(new ByteArrayInputStream(pay));
					
					if (recStream!=null && !observeOptList.isEmpty() && pay.length >= Registry.PAYLOAD_HDR_LENGTH) {
							
						byte packetType = dataStream.readByte();
						byte sensorType = dataStream.readByte();
						short length = dataStream.readShort();
						
						Option observeOpt = observeOptList.get(0);
						int sequenceNumber = observeOpt.getIntValue();
						
						accelTotalNotifReceived++;
						
						if (packetType == Registry.DATAPOINT ||
								packetType == Registry.RETRANSMISSION) {
							
							accelDataNotifReceived++;
							
							if (packetType == Registry.RETRANSMISSION) {
								accelBeforeRetransmissions++;
								System.out.println("Got Accel Retransmitted Packet MID:"+
								Integer.toString(response.getMID()));
							}
							
							/*
							 * number of samples in the data packet:
							 * (length - hdrlength)/(rtptslength + samplelength)
							 * PAYLOAD_HDR_LENGTH
							 * RTPTS_LENGTH
							 * ACCEL_SAMPLE_LENGTH
							 */
							int nSamples = (length-Registry.PAYLOAD_HDR_LENGTH)
									/(Registry.RTPTS_LENGTH+Registry.ACCEL_SAMPLE_LENGTH);
							
							/* Where starts, w.r.t. the beginning of the payload, the next data
							 * ascii-encoded float values. */
							int offsetValue = Registry.PAYLOAD_HDR_LENGTH + Registry.RTPTS_LENGTH;
							
							recStream.registerDataArrival(pay.length);
							
							for (int k=0; k<nSamples; k++) {
							//while (number of samples in the packet)
								
								int timestamp = dataStream.readInt();
								
								ZeSampleRegisterEntry entry = accelSampleRegister.get(timestamp);
								if (entry == null) {
									entry = new ZeSampleRegisterEntry(timestamp);
									accelSampleRegister.put(timestamp, entry);
								}
								
								ZeAccelElement event = new ZeAccelElement();
								event.x = Float.parseFloat(new String(Arrays
										.copyOfRange(pay, offsetValue, offsetValue+19 )));
								event.y = Float.parseFloat(new String(Arrays
										.copyOfRange(pay, offsetValue+20, offsetValue+39)));
								event.z = Float.parseFloat(new String(Arrays
										.copyOfRange(pay, offsetValue+40, offsetValue+59)));
								event.timestamp = timestamp;
								event.sequenceNumber = sequenceNumber;
								event.sensorId = Registry.SENSOR_TYPE_ACCELEROMETER;
								
								/* detect if this sample is a duplicate */
								if (packetType == Registry.RETRANSMISSION ||
										(k==0 && Registry.REPETITION_ENABLED) ) {
									event.duplicate = true;
									entry.gotDuplicate = true;
								}
								else {
									event.duplicate = false;
									entry.gotOriginal = true;
									if (!entry.gotDuplicate) entry.firstOriginal = true;
								}
								
								offsetValue += (Registry.ACCEL_SAMPLE_LENGTH + Registry.RTPTS_LENGTH);
								dataStream.skip(Registry.ACCEL_SAMPLE_LENGTH);
								
								recStream.registerSampleArrival();
								
								System.out.println("packet:"+packetType+
										" sensor:"+sensorType+
										" length:"+length+
										" ts:"+timestamp+
										" sn"+sequenceNumber+
										" x:"+event.x+
										" y:"+event.y+
										" z:"+event.z);
								
								if (recStream.timingReady) {
									recStream.toWallclock(event);
									boolean succ = accelPlayoutManager.add(event);
									if (succ) {
										/* This operation seems to give a race condition with its
										 * symmetrical one in ZeAccelDisplayDevice. For the moment
										 * only update the buffer chart in ZeAccelDisplayDevice. The
										 * chart is good enough anyhow.
										 */
										//meters.accelBufferSeries.add(meters.accelBufferSeries.getItemCount()+1,
										//		accelPlayoutManager.size());
										
										entry.useful = true;
									}
									else System.out.println("Accel sample already in buffer or late, "+
														"insertion rejected.");

								}
								else {
									System.out.println("Not sending to playout, timing still unknown.");
									accelDataNotifBeforeTiming++;
								}
							
							}
							//end while
						}
						else if (packetType == Registry.SENDREPORT) {
							
							accelSRRec++;
							
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
							
							/*
							if (firstSR) {
								firstSR = false;
								long blindDelay = Registry.BLIND_DELAY;
								masterPlayoutManager.mpo = System.nanoTime() + blindDelay - ntpts;
							}*/
							
							//if (recStream.timingReady == false) {
								recStream.updateTiming(rtpts, ntpts);
								recStream.octectCount = octectCount;
								recStream.packetCount = packetCount;
							//}
						} //sender report
						else System.out.println("Unknown payload format, drop.");
						
						// if bandwidth threshold trigger receiver report
						if (recStream.octectsReceived > recStream.octectsReceivedAtLastRR + Registry.RR_BANDWIDTH_THRESHOLD ) {
							recStream.octectsReceivedAtLastRR = recStream.octectsReceived;
							// for the moment do not fill it with any meaningful data,
							// at the receiver side it's not at all used.
							byte[] rrpay = new byte[Registry.PAYLOAD_RR_LENGTH];
							//for (int i=0; i<rrpay.length; i++) rrpay[i] = (byte)(rrpay[i] & (1<<i));
							//1,2,4,8...
							//DataOutputStream dataRRStream = new DataOutputStream(
							//	new ByteArrayOutputStream(Registry.PAYLOAD_RR_LENGTH));
							sendRR(Registry.ACCEL_RESOURCE_PATH, rrpay);
							accelRRSent++;
						}
						
						
					} //valid response
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}// while true loop
		}// run method of the thread
	}//thread class
	
	public class ZeProxRecThread extends Thread {
		
		@Override
		public void run() {
			
			Thread.currentThread().setName("ZeProxRecThread");
			
			System.out.println("Hello from thread "+Thread.currentThread().getName());
			
			/* Initialize playout manager. */
			proxPlayoutManager = new ZePlayoutManager<ZeProxElement>();
			proxPlayoutManager.master = masterPlayoutManager;
			proxPlayoutManager.playoutFreq = Registry.PROX_PLAYOUT_FREQ;
			proxPlayoutManager.playoutPer = Registry.PROX_PLAYOUT_PERIOD * 1000000;
			proxPlayoutManager.playoutHalfPer = Registry.PROX_PLAYOUT_HALF_PERIOD * 1000000;
					
			/* Initialize samples register. */
			proxSampleRegister = new HashMap<Integer, ZeSampleRegisterEntry>();
			
			/* Turn on display device. */
			proxDev = new ZeProxDisplayDevice();
			proxDev.playoutManager = proxPlayoutManager;
			proxDev.meter = meters;
			proxDev.start();
			
			Request request = prepareObserveRequest(Registry.PROX_RESOURCE_PATH);
			proxStream = new ZeStream(request.getToken(), Registry.PROX_RESOURCE_PATH, Registry.PROX_STREAM_FREQ);
			streams.add(proxStream);
			executeRequest(request);
		
			while(loop) {
				
				Response response = null;
				
				try {
					
					response = request.receiveResponse();
					
					// get token and corresponding resource to identify the stream
					ZeStream recStream = findStream(streams, response.getToken(), response.getRequest().getUriPath());
					if (recStream != null)
						System.out.println("Stream found, token:"+new String(recStream.token)+" resource:"+recStream.resource);
					
					// check if it can be part of any stream
					ArrayList<Option> observeOptList = 
							(ArrayList<Option>) response.getOptions(OptionNumberRegistry.OBSERVE);
					
					// get payload
					byte[] pay = response.getPayload();
					DataInputStream dataStream = new DataInputStream(new ByteArrayInputStream(pay));
					
					if (recStream!=null && !observeOptList.isEmpty() && pay.length >= Registry.PAYLOAD_HDR_LENGTH) {
							
						byte packetType = dataStream.readByte();
						byte sensorType = dataStream.readByte();
						short length = dataStream.readShort();
						
						Option observeOpt = observeOptList.get(0);
						int sequenceNumber = observeOpt.getIntValue();
						
						proxTotalNotifReceived++;
						
						if (packetType == Registry.DATAPOINT ||
								packetType == Registry.RETRANSMISSION) {
							
							proxDataNotifReceived++;
							
							if (packetType == Registry.RETRANSMISSION) {
								proxBeforeRetransmissions++;
								System.out.println("Got Prox Retransmitted Packet MID:"+
								Integer.toString(response.getMID()));
							}
							
							int nSamples = (length-Registry.PAYLOAD_HDR_LENGTH)
									/(Registry.RTPTS_LENGTH+Registry.PROX_SAMPLE_LENGTH);
							
							int offsetValue = Registry.PAYLOAD_HDR_LENGTH + Registry.RTPTS_LENGTH;
							
							recStream.registerDataArrival(pay.length);
							
							for (int k=0; k<nSamples; k++) {
								
								int timestamp = dataStream.readInt();
								
								ZeSampleRegisterEntry entry = proxSampleRegister.get(timestamp);
								if (entry == null) {
									entry = new ZeSampleRegisterEntry(timestamp);
									proxSampleRegister.put(timestamp, entry);
								}
								
								ZeProxElement pelement = new ZeProxElement();
								pelement.distance = Float.parseFloat(new String(Arrays
										.copyOfRange(pay, offsetValue, offsetValue+19)));
								pelement.timestamp = timestamp;
								pelement.sequenceNumber = sequenceNumber;
								pelement.sensorId = Registry.SENSOR_TYPE_PROXIMITY;
								
								/* detect if this sample is a duplicate */
								if (packetType == Registry.RETRANSMISSION ||
										(k==0 && Registry.REPETITION_ENABLED) ) {
									pelement.duplicate = true;
									entry.gotDuplicate = true;
								}
								else {
									pelement.duplicate = false;
									entry.gotOriginal = true;
									if (!entry.gotDuplicate) entry.firstOriginal = true;
								}
								
								offsetValue += (Registry.PROX_SAMPLE_LENGTH + Registry.RTPTS_LENGTH);
								dataStream.skip(Registry.PROX_SAMPLE_LENGTH);
								
								recStream.registerSampleArrival();
								
								System.out.println("packet:"+packetType+
										" sensor:"+sensorType+
										" length:"+length+
										" ts:"+timestamp+
										" sn"+sequenceNumber+
										" dist:"+pelement.distance);
								
								if (recStream.timingReady) {
									recStream.toWallclock(pelement);
									boolean succ = proxPlayoutManager.add(pelement);
									if (succ) {
										//meters.proxBufferSeries.add(meters.proxBufferSeries.getItemCount()+1,
										//		proxPlayoutManager.size());
										
										entry.useful = true;
										
									}
									else System.out.println("Prox sample already in buffer or late, "+
												"insertion rejected.");

								}
								else {
									proxDataNotifBeforeTiming++;
									System.out.println("Not sending to playout, timing still unknown.");
								}
							}
						}
						else if (packetType == Registry.SENDREPORT) {
							
							proxSRRec++;
							
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
							
							/*
							if (firstSR) {
								firstSR = false;
								long blindDelay = Registry.BLIND_DELAY;
								masterPlayoutManager.mpo = System.nanoTime() + blindDelay - ntpts;
							}*/
							
							//if (recStream.timingReady == false) {
								recStream.updateTiming(rtpts, ntpts);
								recStream.octectCount = octectCount;
								recStream.packetCount = packetCount;
							//}
						} //sender report
						else System.out.println("Unknown payload format, drop.");
						
						// if bandwidth threshold trigger receiver report
						if (recStream.octectsReceived > recStream.octectsReceivedAtLastRR + Registry.RR_BANDWIDTH_THRESHOLD ) {
							recStream.octectsReceivedAtLastRR = recStream.octectsReceived;

							byte[] rrpay = new byte[Registry.PAYLOAD_RR_LENGTH];

							sendRR(Registry.PROX_RESOURCE_PATH, rrpay);
							proxRRSent++;
						}
						
					} //valid response
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}// while true loop
		}// run method of the thread
	}//thread class
	
	public class ZeLightRecThread extends Thread {
		
		@Override
		public void run() {
			
			Thread.currentThread().setName("ZeLightRecThread");
			
			System.out.println("Hello from thread "+Thread.currentThread().getName());
			
			/* Initialize playout manager. */
			lightPlayoutManager = new ZePlayoutManager<ZeLightElement>();
			lightPlayoutManager.master = masterPlayoutManager;
			lightPlayoutManager.playoutFreq = Registry.LIGHT_PLAYOUT_FREQ;
			lightPlayoutManager.playoutPer = Registry.LIGHT_PLAYOUT_PERIOD * 1000000;
			lightPlayoutManager.playoutHalfPer = Registry.LIGHT_PLAYOUT_HALF_PERIOD * 1000000;
			
			/* Initialize samples register. */
			lightSampleRegister = new HashMap<Integer, ZeSampleRegisterEntry>();
			
			/* Turn on display device. */
			lightDev = new ZeLightDisplayDevice();
			lightDev.playoutManager = lightPlayoutManager;
			lightDev.meter = meters;
			lightDev.start();
			
			Request request = prepareObserveRequest(Registry.LIGHT_RESOURCE_PATH);
			lightStream = new ZeStream(request.getToken(), Registry.LIGHT_RESOURCE_PATH, Registry.LIGHT_STREAM_FREQ);
			streams.add(lightStream);
			executeRequest(request);
			
			while(loop) {
				
				Response response = null;
				
				try {
					
					response = request.receiveResponse();
					
					// get token and corresponding resource to identify the stream
					ZeStream recStream = findStream(streams, response.getToken(), response.getRequest().getUriPath());
					if (recStream != null)
						System.out.println("Stream found, token:"+new String(recStream.token)+" resource:"+recStream.resource);
					
					// check if it can be part of any stream
					ArrayList<Option> observeOptList = 
							(ArrayList<Option>) response.getOptions(OptionNumberRegistry.OBSERVE);
					
					// get payload
					byte[] pay = response.getPayload();
					DataInputStream dataStream = new DataInputStream(new ByteArrayInputStream(pay));
					
					if (recStream!=null && !observeOptList.isEmpty() && pay.length >= Registry.PAYLOAD_HDR_LENGTH) {
							
						byte packetType = dataStream.readByte();
						byte sensorType = dataStream.readByte();
						short length = dataStream.readShort();
						
						Option observeOpt = observeOptList.get(0);
						int sequenceNumber = observeOpt.getIntValue();
						
						lightTotalNotifReceived++;
						
						if (packetType == Registry.DATAPOINT ||
								packetType == Registry.RETRANSMISSION) {
							
							lightDataNotifReceived++;
							
							if (packetType == Registry.RETRANSMISSION) {
								lightBeforeRetransmissions++;
								System.out.println("Got Light Retransmitted Packet MID:"+
								Integer.toString(response.getMID()));
							}
							
							int nSamples = (length-Registry.PAYLOAD_HDR_LENGTH)
									/(Registry.RTPTS_LENGTH+Registry.LIGHT_SAMPLE_LENGTH);
							
							int offsetValue = Registry.PAYLOAD_HDR_LENGTH + Registry.RTPTS_LENGTH;
							
							recStream.registerDataArrival(pay.length);
							
							for (int k=0; k<nSamples; k++) {
								
								int timestamp = dataStream.readInt();
								
								ZeSampleRegisterEntry entry = lightSampleRegister.get(timestamp);
								if (entry == null) {
									entry = new ZeSampleRegisterEntry(timestamp);
									lightSampleRegister.put(timestamp, entry);
								}
								
								ZeLightElement lelement = new ZeLightElement();
								lelement.light = Float.parseFloat(new String(Arrays
										.copyOfRange(pay, offsetValue, offsetValue+19)));
								lelement.timestamp = timestamp;
								lelement.sequenceNumber = sequenceNumber;
								lelement.sensorId = Registry.SENSOR_TYPE_LIGHT;
								
								/* detect if this sample is a duplicate */
								if (packetType == Registry.RETRANSMISSION ||
										(k==0 && Registry.REPETITION_ENABLED) ) {
									lelement.duplicate = true;
									entry.gotDuplicate = true;
								}
								else {
									lelement.duplicate = false;
									entry.gotOriginal = true;
									if (!entry.gotDuplicate) entry.firstOriginal = true;
								}
								
								offsetValue += (Registry.LIGHT_SAMPLE_LENGTH + Registry.RTPTS_LENGTH);
								dataStream.skip(Registry.LIGHT_SAMPLE_LENGTH);
								
								recStream.registerSampleArrival();
								
								System.out.println("packet:"+packetType+
										" sensor:"+sensorType+
										" length:"+length+
										" ts:"+timestamp+
										" sn"+sequenceNumber+
										" light:"+lelement.light);
								
								if (recStream.timingReady) {
									recStream.toWallclock(lelement);
									boolean succ = lightPlayoutManager.add(lelement);
									if (succ) {
										//meters.lightBufferSeries.add(meters.lightBufferSeries.getItemCount()+1,
										//		lightPlayoutManager.size());
										
										entry.useful = true;
									}
									else System.out.println("Light sample already in buffer or late, "+
												"insertion rejected.");
								}
								else {
									lightDataNotifBeforeTiming++;
									System.out.println("Not sending to playout, timing still unknown.");
								}
							}
						}
						else if (packetType == Registry.SENDREPORT) {
							
							lightSRRec++;
							
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
							/*
							if (firstSR) {
								firstSR = false;
								long blindDelay = Registry.BLIND_DELAY;
								masterPlayoutManager.mpo = System.nanoTime() + blindDelay - ntpts;
							}*/
							
							//if (recStream.timingReady == false) {
								recStream.updateTiming(rtpts, ntpts);
								recStream.octectCount = octectCount;
								recStream.packetCount = packetCount;
							//}
						} //sender report
						else System.out.println("Unknown payload format, drop.");
						
						// if bandwidth threshold trigger receiver report
						if (recStream.octectsReceived > recStream.octectsReceivedAtLastRR + Registry.RR_BANDWIDTH_THRESHOLD ) {
							recStream.octectsReceivedAtLastRR = recStream.octectsReceived;

							byte[] rrpay = new byte[Registry.PAYLOAD_RR_LENGTH];

							sendRR(Registry.LIGHT_RESOURCE_PATH, rrpay);
							lightRRSent++;
						}
						
					} //valid response
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}// while true loop
		}//thread run()
	}//thread class
	
	
	public class ZeGyroRecThread extends Thread {
		
		@Override
		public void run() {
			
			Thread.currentThread().setName("ZeGyroRecThread");
			
			System.out.println("Hello from thread "+Thread.currentThread().getName());
			
			/* Initialize playout manager. */
			gyroPlayoutManager = new ZePlayoutManager<ZeGyroElement>();
			gyroPlayoutManager.master = masterPlayoutManager;
			gyroPlayoutManager.playoutFreq = Registry.GYRO_PLAYOUT_FREQ;
			gyroPlayoutManager.playoutPer = Registry.GYRO_PLAYOUT_PERIOD * 1000000;
			gyroPlayoutManager.playoutHalfPer = Registry.GYRO_PLAYOUT_HALF_PERIOD * 1000000;
			
			/* Initialize samples register. */
			gyroSampleRegister = new HashMap<Integer, ZeSampleRegisterEntry>();
			
			/* Turn on display device. */
			gyroDev = new ZeGyroDisplayDevice();
			gyroDev.playoutManager = gyroPlayoutManager;
			gyroDev.meter = meters;
			gyroDev.start();
			
			Request request = prepareObserveRequest(Registry.GYRO_RESOURCE_PATH);
			gyroStream = new ZeStream(request.getToken(), Registry.GYRO_RESOURCE_PATH, Registry.GYRO_STREAM_FREQ);
			streams.add(gyroStream);
			executeRequest(request);
			
			while(loop) {
				
				Response response = null;
				
				try {
					
					response = request.receiveResponse();
					
					// get token and corresponding resource to identify the stream
					ZeStream recStream = findStream(streams, response.getToken(), response.getRequest().getUriPath());
					if (recStream != null)
						System.out.println("Stream found, token:"+new String(recStream.token)+" resource:"+recStream.resource);
					
					// check if it can be part of any stream
					ArrayList<Option> observeOptList = 
							(ArrayList<Option>) response.getOptions(OptionNumberRegistry.OBSERVE);
					
					// get payload
					byte[] pay = response.getPayload();
					DataInputStream dataStream = new DataInputStream(new ByteArrayInputStream(pay));
					
					if (recStream!=null && !observeOptList.isEmpty() && pay.length >= Registry.PAYLOAD_HDR_LENGTH) {
							
						byte packetType = dataStream.readByte();
						byte sensorType = dataStream.readByte();
						short length = dataStream.readShort();
						
						Option observeOpt = observeOptList.get(0);
						int sequenceNumber = observeOpt.getIntValue();
						
						gyroTotalNotifReceived++;
						
						if (packetType == Registry.DATAPOINT ||
								packetType == Registry.RETRANSMISSION) {
							
							gyroDataNotifReceived++;
							
							if (packetType == Registry.RETRANSMISSION) {
								gyroBeforeRetransmissions++;
								System.out.println("Got Gyro Retransmitted Packet MID:"+
								Integer.toString(response.getMID()));
							}
							
							int nSamples = (length-Registry.PAYLOAD_HDR_LENGTH)
									/(Registry.RTPTS_LENGTH+Registry.GYRO_SAMPLE_LENGTH);
							
							int offsetValue = Registry.PAYLOAD_HDR_LENGTH + Registry.RTPTS_LENGTH;
							
							recStream.registerDataArrival(pay.length);
							
							for (int k=0; k<nSamples; k++) {
							
								int timestamp = dataStream.readInt();
								
								ZeSampleRegisterEntry entry = gyroSampleRegister.get(timestamp);
								if (entry == null) {
									entry = new ZeSampleRegisterEntry(timestamp);
									gyroSampleRegister.put(timestamp, entry);
								}
								
								ZeGyroElement event = new ZeGyroElement();
								event.x = Float.parseFloat(new String(Arrays
										.copyOfRange(pay, offsetValue, offsetValue+19)));
								event.y = Float.parseFloat(new String(Arrays
										.copyOfRange(pay, offsetValue+20, offsetValue+39)));
								event.z = Float.parseFloat(new String(Arrays
										.copyOfRange(pay, offsetValue+40, offsetValue+59)));
								event.timestamp = timestamp;
								event.sequenceNumber = sequenceNumber;
								event.sensorId = Registry.SENSOR_TYPE_GYROSCOPE;
								
								/* detect if this sample is a duplicate */
								if (packetType == Registry.RETRANSMISSION ||
										(k==0 && Registry.REPETITION_ENABLED) ) {
									event.duplicate = true;
									entry.gotDuplicate = true;
								}
								else {
									event.duplicate = false;
									entry.gotOriginal = true;
									if (!entry.gotDuplicate) entry.firstOriginal = true;
								}
								
								offsetValue += (Registry.GYRO_SAMPLE_LENGTH + Registry.RTPTS_LENGTH);
								dataStream.skip(Registry.GYRO_SAMPLE_LENGTH);
								
								recStream.registerSampleArrival();
								
								System.out.println("packet:"+packetType+
										" sensor:"+sensorType+
										" length:"+length+
										" ts:"+timestamp+
										" sn"+sequenceNumber+
										" x:"+event.x+
										" y:"+event.y+
										" z:"+event.z);
								
								if (recStream.timingReady) {
									recStream.toWallclock(event);
									boolean succ = gyroPlayoutManager.add(event);
									if (succ) {
										//meters.gyroBufferSeries.add(meters.gyroBufferSeries.getItemCount()+1,
										//			gyroPlayoutManager.size());
										
										entry.useful = true;
									}
									else System.out.println("Gyro sample already in buffer or late, "+
												"insertion rejected.");
								}
								else {
									gyroDataNotifBeforeTiming++;
									System.out.println("Not sending to playout, timing still unknown.");
								}
							}
						}
						else if (packetType == Registry.SENDREPORT) {
							
							gyroSRRec++;
							
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
							
							/*
							if (firstSR) {
								firstSR = false;
								long blindDelay = Registry.BLIND_DELAY;
								masterPlayoutManager.mpo = System.nanoTime() + blindDelay - ntpts;
							}*/
							
							//if (recStream.timingReady == false) {
								recStream.updateTiming(rtpts, ntpts);
								recStream.octectCount = octectCount;
								recStream.packetCount = packetCount;
							//}
						} //sender report
						else System.out.println("Unknown payload format, drop.");
						
						// if bandwidth threshold trigger receiver report
						if (recStream.octectsReceived > recStream.octectsReceivedAtLastRR + Registry.RR_BANDWIDTH_THRESHOLD ) {
							recStream.octectsReceivedAtLastRR = recStream.octectsReceived;

							byte[] rrpay = new byte[Registry.PAYLOAD_RR_LENGTH];

							sendRR(Registry.GYRO_RESOURCE_PATH, rrpay);
							gyroRRSent++;
						}
						
					} //valid response
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}// while true loop
		}// run method of the thread
	}//thread class
	
	
	public class TestThread extends Thread {
		
		/* Used to catch the (unique) message that arrives on the test socket.
		 * It will be a sender report and it is going to have, in our
		 * simulated environment, exactly the average network delay. */
		
		@Override
		public void run() {
			
			System.out.println("Hello from TestThead");
			
			byte[] buffer = new byte[500];
			DatagramPacket testDatagram = new DatagramPacket(buffer, buffer.length);
			try { //blocking until we receive one
				testSocket.receive(testDatagram);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			System.out.println("Something received on the test socket");
			
			// Use Californium to interpret the CoAP header
			byte[] message = Arrays.copyOfRange(testDatagram.getData(),
					testDatagram.getOffset(), testDatagram.getLength());
			Message msg = Message.fromByteArray(message);
			
			// Extract the payload of the CoAP message
			byte[] payload = msg.getPayload();
			DataInputStream payloadStream = new DataInputStream(new ByteArrayInputStream(payload));
			
			try {
				byte packetType = payloadStream.readByte();
				byte sensorType = payloadStream.readByte();
				short length = payloadStream.readShort();
				long ntpts = payloadStream.readLong();
				
				System.out.println("ptype:"+packetType+" sensor:"+sensorType+" length:"+length+" NTP TS:"+ntpts);
				
				masterPlayoutManager.mpo = System.nanoTime() + Registry.BLIND_DELAY - ntpts;
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}	
	}
	
	void zeSenseClient () {
		
		System.out.println("This thread:"+Thread.currentThread().getId());
		System.out.println(System.getProperty("java.version"));
	
		//Lock lock = new ReentrantLock();
		//globalExit = lock.newCondition();
		
		//quit button panel
		JPanel panel = new JPanel();
		getContentPane().add(panel);
		panel.setLayout(null);
		JButton quitButton = new JButton("Quit");
		quitButton.setBounds(50, 60, 80, 30);
		quitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				loop = false;
				//globalExit.signal();
			}
		});
		panel.add(quitButton);
		setVisible(true);
		
		//meters panel
		meters = new ZeMeters("ZeSense Monitors Panel");
	    meters.pack();
	    RefineryUtilities.centerFrameOnScreen(meters);
	    meters.setVisible(true);
	    
	    masterPlayoutManager = new ZeMasterPlayoutManager();

		streams = new ArrayList<ZeStream>();
		
		CommunicatorFactory.getInstance().setUdpPort(Registry.LOCAL_PORT);
		
		Log.setLevel(Level.ALL);
		Log.init();
		
		try {
			testSocket = new DatagramSocket(Registry.LOCAL_TEST_PORT);
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		System.out.println("Test socket opened on port:"+Registry.LOCAL_TEST_PORT);
		TestThread testThread = new TestThread();
		testThread.start();
		
		
		ZeProxRecThread proxThread = new ZeProxRecThread();
		proxThread.start();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
		
		
		/*
		ZeLightRecThread lightThread = new ZeLightRecThread();
		lightThread.start();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
		*/
		
		ZeAccelRecThread accelThread = new ZeAccelRecThread();
		accelThread.start();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
		
		
		/*
		ZeGyroRecThread gyroThread = new ZeGyroRecThread();
		gyroThread.start();
		*/
		
		
		while (loop) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		/*
		System.out.println("Joining..");
		try {
			accelThread.join();
			proxThread.join();
			lightThread.join();
			//orientThread.join();
			gyroThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		*/
		
		for (ZeSampleRegisterEntry entr : accelSampleRegister.values()) {
		    if (entr.gotDuplicate) {
		    	accelArrivedDuplicates++;
		    	if (!entr.gotOriginal) accelSamplesOnlyDuplicate++;
		    	if (!entr.firstOriginal) {
		    		accelBeforeDuplicates++;
		    		if (entr.useful)  accelUsefulDuplicates++;
		    	}
		    }
		    if (entr.gotOriginal) {
		    	if (!entr.gotDuplicate) accelSamplesOnlyOriginal++;
		    }
		}
		
		for (ZeSampleRegisterEntry entr : proxSampleRegister.values()) {
		    if (entr.gotDuplicate) {
		    	proxArrivedDuplicates++;
		    	if (!entr.gotOriginal) proxSamplesOnlyDuplicate++;
		    	if (!entr.firstOriginal) {
		    		proxBeforeDuplicates++;
		    		if (entr.useful)  proxUsefulDuplicates++;
		    	}
		    }
		    if (entr.gotOriginal) {
		    	if (!entr.gotDuplicate) proxSamplesOnlyOriginal++;
		    }
		}
		/*
		for (ZeSampleRegisterEntry entr : gyroSampleRegister.values()) {
		    if (entr.gotDuplicate) {
		    	gyroArrivedDuplicates++;
		    	if (!entr.gotOriginal) gyroSamplesOnlyDuplicate++;
		    	if (!entr.firstOriginal) {
		    		gyroBeforeDuplicates++;
		    		if (entr.useful)  gyroUsefulDuplicates++;
		    	}
		    }
		    if (entr.gotOriginal) {
		    	if (!entr.gotDuplicate) gyroSamplesOnlyOriginal++;
		    }
		}
		
		for (ZeSampleRegisterEntry entr : lightSampleRegister.values()) {
		    if (entr.gotDuplicate) {
		    	lightArrivedDuplicates++;
		    	if (!entr.gotOriginal) lightSamplesOnlyDuplicate++;
		    	if (!entr.firstOriginal) {
		    		lightBeforeDuplicates++;
		    		if (entr.useful)  lightUsefulDuplicates++;
		    	}
		    }
		    if (entr.gotOriginal) {
		    	if (!entr.gotDuplicate) lightSamplesOnlyOriginal++;
		    }
		}
		*/
		
		System.out.println("------- ZeSense Client ---------");
		System.out.println(new Date().toString());
		
		System.out.println("--- Accelerometer");
		System.out.println("Total notifications received:"+accelTotalNotifReceived);
		System.out.println("Data notifications received:"+accelDataNotifReceived);
		System.out.println("of which are retransmissions arrived before the original:"+accelBeforeRetransmissions);
		System.out.println("Samples received (not unique):"+accelStream.samplesReceived);
		System.out.println("Unique samples:"+accelSampleRegister.size());
		System.out.println("of which I got both original and duplicate:"+((accelSampleRegister.size()-accelSamplesOnlyOriginal)-accelSamplesOnlyDuplicate));
		System.out.println("of which got only original:"+accelSamplesOnlyOriginal);
		System.out.println("of which got only duplicate:"+accelSamplesOnlyDuplicate);
		System.out.println("Total arrived duplicates:"+accelArrivedDuplicates);
		System.out.println("of which arrived before original:"+accelBeforeDuplicates);
		System.out.println("among dups before original, those on time (useful):"+accelUsefulDuplicates);
		System.out.println("Sender reports received:"+accelSRRec);
		System.out.println("Receiver reports sent:"+accelRRSent);
		System.out.println("Data notifs arrived before timing:"+accelDataNotifBeforeTiming);
		System.out.println("Samples played:"+accelPlayoutManager.played);
		System.out.println("Queue size at stop:"+accelPlayoutManager.size());
		System.out.println("Samples skipped:"+accelPlayoutManager.skipped);
		System.out.println("---");
		
		System.out.println("--- Proximity");
		System.out.println("Total notifications received:"+proxTotalNotifReceived);
		System.out.println("Data notifications received:"+proxDataNotifReceived);
		System.out.println("of which are retransmissions arrived before the original:"+proxBeforeRetransmissions);
		System.out.println("Samples received (not unique):"+proxStream.samplesReceived);
		System.out.println("Unique samples:"+proxSampleRegister.size());
		System.out.println("of which I got both original and duplicate:"+((proxSampleRegister.size()-proxSamplesOnlyOriginal)-proxSamplesOnlyDuplicate));
		System.out.println("of which got only original:"+proxSamplesOnlyOriginal);
		System.out.println("of which got only duplicate:"+proxSamplesOnlyDuplicate);
		System.out.println("Total arrived duplicates:"+proxArrivedDuplicates);
		System.out.println("of which arrived before original:"+proxBeforeDuplicates);
		System.out.println("among dups before original, those on time (useful):"+proxUsefulDuplicates);
		System.out.println("Sender reports received:"+proxSRRec);
		System.out.println("Receiver reports sent:"+proxRRSent);
		System.out.println("Data notifs arrived before timing:"+proxDataNotifBeforeTiming);
		System.out.println("Samples played:"+proxPlayoutManager.played);
		System.out.println("Queue size at stop:"+proxPlayoutManager.size());
		System.out.println("Samples skipped:"+proxPlayoutManager.skipped);
		System.out.println("---");
		
		/*
		System.out.println("--- Gyroscope");
		System.out.println("Total notifications received:"+gyroTotalNotifReceived);
		System.out.println("Data notifications received:"+gyroDataNotifReceived);
		System.out.println("of which are retransmissions arrived before the original:"+gyroBeforeRetransmissions);
		System.out.println("Samples received (not unique):"+gyroStream.samplesReceived); //for the moment
		System.out.println("Unique samples:"+gyroSampleRegister.size());
		System.out.println("of which I got both original and duplicate:"+((gyroSampleRegister.size()-gyroSamplesOnlyOriginal)-gyroSamplesOnlyDuplicate));
		System.out.println("of which got only original:"+gyroSamplesOnlyOriginal);
		System.out.println("of which got only duplicate:"+gyroSamplesOnlyDuplicate);
		System.out.println("Total arrived duplicates:"+gyroArrivedDuplicates);
		System.out.println("of which arrived before original:"+gyroBeforeDuplicates);
		System.out.println("among dups before original, those on time (useful):"+gyroUsefulDuplicates);
		System.out.println("Sender reports received:"+gyroSRRec);
		System.out.println("Receiver reports sent:"+gyroRRSent);
		System.out.println("Data notifs arrived before timing:"+gyroDataNotifBeforeTiming);
		System.out.println("Samples played:"+gyroPlayoutManager.played);
		System.out.println("Queue size at stop:"+gyroPlayoutManager.size());
		System.out.println("Samples skipped:"+gyroPlayoutManager.skipped);
		System.out.println("---");
		
		System.out.println("--- Light");
		System.out.println("Total notifications received:"+lightTotalNotifReceived);
		System.out.println("Data notifications received:"+lightDataNotifReceived);
		System.out.println("of which are retransmissions arrived before the original:"+lightBeforeRetransmissions);
		System.out.println("Samples received (not unique):"+lightStream.samplesReceived); //for the moment
		System.out.println("Unique samples:"+lightSampleRegister.size());
		System.out.println("of which I got both original and duplicate:"+((lightSampleRegister.size()-lightSamplesOnlyOriginal)-lightSamplesOnlyDuplicate));
		System.out.println("of which got only original:"+lightSamplesOnlyOriginal);
		System.out.println("of which got only duplicate:"+lightSamplesOnlyDuplicate);
		System.out.println("Total arrived duplicates:"+lightArrivedDuplicates);
		System.out.println("of which arrived before original:"+lightBeforeDuplicates);
		System.out.println("among dups before original, those on time (useful):"+lightUsefulDuplicates);
		System.out.println("Sender reports received:"+lightSRRec);
		System.out.println("Receiver reports sent:"+lightRRSent);
		System.out.println("Data notifs arrived before timing:"+lightDataNotifBeforeTiming);
		System.out.println("Samples played:"+lightPlayoutManager.played);
		System.out.println("Queue size at stop:"+lightPlayoutManager.size());
		System.out.println("Samples skipped:"+lightPlayoutManager.skipped);
		System.out.println("---");
		*/
		
	}
	
	// payload given null if no payload applies
	public Request sendRR(String resourcePath, byte[] payload) {
		URI uri = null;
		try {
			uri = new URI(Registry.HOST+resourcePath);
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		// create request according to specified method
		Request request = new POSTRequest();
		// cook the request
		request.setURI(uri);
		request.setPayload(payload);
		request.setToken( TokenManager.getInstance().acquireToken() );
		request.setContentType(MediaTypeRegistry.TEXT_PLAIN);
		//enable response queue in order to use blocking I/O
		//request.enableResponseQueue(true);	
		request.prettyPrint();

		try {
			request.execute();
		} catch (UnknownHostException e) {
			System.err.println("Unknown host: " + e.getMessage());
			System.exit(Registry.ERR_REQUEST_FAILED);
		} catch (IOException e) {
			System.err.println("Failed to execute request: " + e.getMessage());
			System.exit(Registry.ERR_REQUEST_FAILED);
		}
		
		return request;
		/*	
		while (true) {
			Response r = null;
			try {
				r = request.receiveResponse();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			r.prettyPrint();
		}
		*/
	}
	
	synchronized ZeStream findStream(ArrayList<ZeStream> list, byte[] token, String resource) {
		for (ZeStream s : list) {
		    if (Arrays.equals(s.token, token) && s.resource.equals(resource))
		    	return s;
		}
		return null;
	}
	
	public Request prepareObserveRequest(String resourcePath) {
		String method = "OBSERVE";
		URI uri = null;
		String payload = null;
		try {
			uri = new URI(Registry.HOST+resourcePath);
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		// create request according to specified method
		Request request = new GETRequest();
		// if we want to observe, set the option
		if (method.equals("OBSERVE")) {
			request.setOption(new Option(0, OptionNumberRegistry.OBSERVE));
			loop = true;
		}
		// cook the request
		request.setURI(uri);
		request.setPayload(payload);
		request.setToken( TokenManager.getInstance().acquireToken() );
		request.setContentType(MediaTypeRegistry.TEXT_PLAIN);
		// enable response queue in order to use blocking I/O
		request.enableResponseQueue(true);		
		request.prettyPrint();
		return request;
	}
	
	public void executeRequest(Request request) {
		try {
			request.execute();
		} catch (UnknownHostException e) {
			System.err.println("Unknown host: " + e.getMessage());
			System.exit(Registry.ERR_REQUEST_FAILED);
		} catch (IOException e) {
			System.err.println("Failed to execute request: " + e.getMessage());
			System.exit(Registry.ERR_REQUEST_FAILED);
		}
	}

}
