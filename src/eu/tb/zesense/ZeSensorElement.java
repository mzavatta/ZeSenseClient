package eu.tb.zesense;

public class ZeSensorElement implements Comparable<ZeSensorElement> {
	
	/* Sensor identifier. */
	int sensorId;
	
	/* CoAP Sequence number.
	 * Note that some samples may have the same sequence number
	 * e.g. multiple samples per packet */
	int sequenceNumber;
	
	/* Timing parameters in sender's terms. */
	int timestamp;
	long wallclock;
	long expiry; //in sender's wallclock terms
	
	/* In receiver's terms. */
	long arrivalTime;
	
	/* Duplicate flag.
	 * True if this sample was sent as the second or more attempt by the server.
	 * Second or more attempt may mean both in backward error correction (content of
	 * a retransmitted packet) or forward error correction (intentionally repeated in the
	 * next packet) */
	boolean duplicate;
	
	/* There's no need for a playout time field because this is not 
	 * a master stream and therefore it is not decided in advance,
	 * but changes continuously depending on the master stream.
	 */
	
	/* 0 if normal value, 1 if "hold previous", 2 if "invalid". */
	//int meaning;
	
	@Override
	public int compareTo(ZeSensorElement o) {
		if (this.timestamp < o.timestamp) return -1;
		else if (this.timestamp > o.timestamp) return +1;
		return 0;
	}
	
}