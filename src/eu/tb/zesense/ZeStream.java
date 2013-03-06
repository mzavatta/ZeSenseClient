package eu.tb.zesense;

public class ZeStream {
	
	/* Stream identifiers: token and resource. */
	byte[] token;
	String resource;

	/* Timing information. */
	long latestWallclockPair;
	int latestTimestampPair;
	boolean timingReady = false;
	int streamFrequency;
	
	/* Sender side statistics. */
	int packetCount;
	int octectCount;
	
	/* Internal statistics. */
	int minSeqNumber = -1;
	int maxSeqNumber = -1;
	int oooCount; //out of order count
	int packetsReceived; //to be compared with packetCount

	public ZeStream(byte[] token, String resource, int streamFrequency) {
		this.token = token;
		this.resource = resource;
		this.streamFrequency = streamFrequency;
	}
	
	public void updateTiming(int timestamp, long ntpts) {
		timingReady = true;
		latestTimestampPair = timestamp;
		latestWallclockPair = ntpts;
	}
	
	public void toWallclock(ZeSensorElement e) {
		
		// XXX cuts to the integer, discards the reminder
		long freqScale = Registry.WALLCLOCK_FREQ/Registry.TIMESTAMP_FREQ;
		long tsAdvance = e.timestamp-latestTimestampPair;
		long commonAdvance = freqScale*tsAdvance;
		e.wallclock = latestWallclockPair+commonAdvance;
		e.expiry = e.wallclock + (Registry.WALLCLOCK_FREQ/streamFrequency);
		
		//System.out.println(Long.toString(freqScale)+" "+Long.toString(tsAdvance)+" "+Long.toString(commonAdvance));

		// p.playout viene assegnato dall'inter-stream synchronizer
	}
	
	public void registerArrival(ZeSensorElement event) {
		packetsReceived++;
		if (maxSeqNumber==-1) { //first packet ever received
			maxSeqNumber = event.sequenceNumber;
		}
		else {
			if (event.sequenceNumber < maxSeqNumber) //out of order
				oooCount++;
		}
	}
	
	public void finalStatistics() {
		// here I print the stats that i need.
		System.out.println("---- Statistics for "+resource+" ----");
		
		
		
	}
}
