package eu.tb.zesense;

public class ZeStream {
	
	/* Stream identifiers: token and resource. */
	byte[] token;
	String resource;

	/* Timing information. */
	long latestWallclockPair;
	int latestTimestampPair;
	boolean timingReady = false;
	
	/* Reception information. */
	int packetCount;
	int octectCount;

	public ZeStream(byte[] token, String resource) {
		this.token = token;
		this.resource = resource;
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
		
		//System.out.println(Long.toString(freqScale)+" "+Long.toString(tsAdvance)+" "+Long.toString(commonAdvance));

		// p.playout viene assegnato dall'inter-stream synchronizer
	}
}
