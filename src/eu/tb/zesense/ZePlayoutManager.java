package eu.tb.zesense;

import java.util.TreeSet;

public class ZePlayoutManager<E extends ZeSensorElement> extends TreeSet<E> {
	
	/*
	 * Does not make sense to have stream-specific data
	 * here.
	 */
	
	/* Relationship between playout device clock
	 * and our system clock. */
	int playoutFreq;// = Registry.ACCEL_PLAYOUT_FREQ;
	int playoutPer;// = Registry.ACCEL_PLAYOUT_PERIOD * 1000000;
	int playoutHalfPer;// = Registry.ACCEL_PLAYOUT_HALF_PERIOD * 1000000;
	long playoutFirstTime;
	int playoutTicks=0;
	int incominSampleFreq;
	
	
	/* Internal non-functional stats. */
	int holdcount = 0;
	int underflowCount  = 0;
	/* Only count stuff that I have, not that I don't have.
	 * Stuff that I don't have we take it by subtraction
	 * with the total.
	 */
	int skipped = 0;
	int played = 0;
	
	E current;
	
	ZeMasterPlayoutManager master;
	
	ZeStream sourceStream;

	
	public ZePlayoutManager() {
		super();
	}
	
	
	public synchronized ZePlayoutElement<E> get() {
		
		long now = playoutToSystem();
		
		while ( !isEmpty() ) {
			System.out.println(Long.toString(master.mpo+first().wallclock)+" interval:"+Long.toString(now-playoutHalfPer)+":"+Long.toString(now+playoutHalfPer));
			if ((first().wallclock+master.mpo) < (now-playoutHalfPer)) {
				E s = pollFirst();
				System.out.println("Late skipped "+s.sensorId);
				skipped++;
			}
			else if ( (first().wallclock+master.mpo) >= (now-playoutHalfPer) && 
					(first().wallclock+master.mpo) <= (now+playoutHalfPer) ) {
				//System.out.println("Giving data");
				resetHoldCount();
				ZePlayoutElement<E> elem = new ZePlayoutElement<E>();
				current = pollFirst();
				elem.element = current;
				elem.meaning = Registry.PLAYOUT_VALID;
				played++;
				return elem;
			}
			else {
				if (current != null) { //not in the first interrupt requests
					if (now < current.expiry+master.mpo) { //current not expired
						// give hold
						holdcount++;
						//System.out.println("Giving hold and buffer still full");
						ZePlayoutElement<E> elem = new ZePlayoutElement<E>();
						elem.element = null;
						elem.meaning = Registry.PLAYOUT_HOLD;
						return elem;
					}
				}
				//System.out.println("Giving invalidate");
				resetHoldCount();
				ZePlayoutElement<E> elem = new ZePlayoutElement<E>();
				elem.element = null;
				elem.meaning = Registry.PLAYOUT_INVALID;
				return elem;
			}
		}
		if (current != null) { //not in the first interrupt requests
			if (now < current.expiry+master.mpo) { //current not expired
				// give hold
				holdcount++;
				//System.out.println("Found empty but holding");
				ZePlayoutElement<E> elem = new ZePlayoutElement<E>();
				elem.element = null;
				elem.meaning = Registry.PLAYOUT_HOLD;
				return elem;
			}
			else {
				//System.out.println("Found empty and current expired: underflow");
				underflowCount++;
				ZePlayoutElement<E> elem = new ZePlayoutElement<E>();
				elem.element = null;
				elem.meaning = Registry.PLAYOUT_INVALID;
				return elem;
			}
		}
		//System.out.println("Found empty but playout not yet started");
		ZePlayoutElement<E> elem = new ZePlayoutElement<E>();
		elem.element = null;
		elem.meaning = Registry.PLAYOUT_NOTSTARTED;
		return elem; //buffer found empty
	}
	
	void resetHoldCount() {
		holdcount = 0;
		//System.out.println("Holdcount reset at "+holdcount);
	}
		
	
	public synchronized boolean add(E elem) {
		return super.add(elem);
	}
	
	

	/* Working version ------------------------------------
	public synchronized ZeSensorElement get() {
		
		System.out.println("Get, now size is "+Integer.toString(size()));
		//ZeSensorElement element = null;
		
		long now = System.nanoTime();

		while ( !isEmpty() ) {
			System.out.println(Long.toString(master.mpo+first().wallclock)+" interval:"+Long.toString(now-playoutHalfPer)+":"+Long.toString(now+playoutHalfPer));
			if ((first().wallclock+master.mpo) < (now-playoutHalfPer)) {
				pollFirst();
				System.out.println("Skip!");
			}
			else if ( (first().wallclock+master.mpo) >= (now-playoutHalfPer) && 
							(first().wallclock+master.mpo) <= (now+playoutHalfPer) ) {
				System.out.println("Giving data");
				return pollFirst();
			}
			else {
				System.out.println("Giving invalidate");
				ZeAccelElement silence = new ZeAccelElement();
				silence.meaning = Registry.PLAYOUT_HOLD; //actually 1 means hold
				return silence;
			}
			//
			 // later here I should give hold if I have a future sample in the buffer
			 // but the current one is still valid at now time
			 // (because for example the playout freq is greater than the
			 // sample freq)
			 //
		}
		System.out.println("Buffer underflow");
		ZeAccelElement empty = new ZeAccelElement();
		empty.meaning = Registry.PLAYOUT_BUFFER_EMPTY;
		return empty;
	} ------------------------------------------------------*/
	
	
	long playoutToSystem() {
		playoutTicks++;
		if ((playoutTicks%10) == 1) {	//first playout interrupt request
			playoutTicks=0;
			playoutFirstTime = System.nanoTime();
			return playoutFirstTime;
		}
		long freqFactor = Registry.WALLCLOCK_FREQ / playoutFreq;
		return playoutFirstTime+(freqFactor*(playoutTicks-1));
	}
	/*
	try {
		 element = first();
	} catch (Exception e) {
		
	}
	
	System.out.println(Long.toString(master.mpo+element.wallclock)+" interval:"+Long.toString(now-playoutHalfPer)+":"+Long.toString(now+playoutHalfPer));

	if ((element.wallclock+master.mpo) >= (now-playoutHalfPer) && 
			(element.wallclock+master.mpo) <= (now+playoutHalfPer)) {
		System.out.println("Giving data");
		return pollFirst();
	}
	else if ( (element.wallclock+master.mpo) < (now-playoutHalfPer) ) {
		pollFirst();
		element = first();
		while ( (element.wallclock+master.mpo) < (now-playoutHalfPer) ) {
			pollFirst();
			element = first();
		}
		if ((element.wallclock+master.mpo) >= (now-playoutHalfPer) && 
				(element.wallclock+master.mpo) <= (now+playoutHalfPer)) {
			System.out.println("Giving data");
			return pollFirst();
		}
	}
System.out.println("Giving invalidate");
ZeAccelElement silence = new ZeAccelElement();
silence.meaning = 1;
return silence;
*/
	
	//get
	//guarda common + dynamic offset del primo
	//se è il momento giusto i.e. now = common + dynamic offset return il campione
	//se quel campione è vecchio (common + dynamic offset)<now scartalo e guarda quello dopo
	//se quel campione verrà mostrato in futuro, return silence
	//(il buffer è ordinato)
	
	
	//strict equality is too extreme, perfectly matching values are very unlikely
	//do an interval
	//common + dynamic offset within [now-tau, now+tau]
	//dove tau = 1/playoutFreq
}
