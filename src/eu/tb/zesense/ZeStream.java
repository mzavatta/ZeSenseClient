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
	
	/* Sender side statistics (they are not information on the actual stuff received). */
	int packetCount = 0;
	int octectCount = 0;
	
	/* Effective reception statistics as we see them.
	 * They only count data packets and not sender reports.
	 * Octects received account only for payload, to be comparable
	 * to octectCount that arrives from sender reports, which by RFC3550
	 * must account only for payload octects. */
	int octectsReceived = 0;
	int samplesReceived = 0;
	
	/* Reference for the bandwidth check for receiver reports. */
	int octectsReceivedAtLastRR = 0;
	
	/* Internal statistics. */
	int minSeqNumber = -1;
	int maxSeqNumber = -1;
	int oooCount; //out of order count


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
	
	// Total payload length received and sequence number
	public void registerSampleArrival() {
		//octectsReceived+=length;
		samplesReceived++;
		/*
		if (maxSeqNumber==-1) { //first packet ever received
			maxSeqNumber = sequenceNumber;
		}
		else {
			if (sequenceNumber < maxSeqNumber) //out of order
				oooCount++;
		}
		*/
	}
	
	public void registerDataArrival(int length) {
		octectsReceived+=length;
		/*
		if (maxSeqNumber==-1) { //first packet ever received
			maxSeqNumber = sequenceNumber;
		}
		else {
			if (sequenceNumber < maxSeqNumber) //out of order
				oooCount++;
		}
		*/
	}
	
	public void finalStatistics() {
		// here I print the stats that i need.
		System.out.println("---- Statistics for "+resource+" ----");
	}
}
