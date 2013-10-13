package eu.tb.zesense;

import java.util.TreeSet;

public class ZePlayoutManager<E extends ZeSensorElement> extends TreeSet<E> {
	
	/* Does not make sense to have stream-specific data
	 * here.
	 */
	
	/* Relationship between playout device clock
	 * and our system clock. */
	int playoutFreq;// = Registry.ACCEL_PLAYOUT_FREQ;
	int playoutPer;// = Registry.ACCEL_PLAYOUT_PERIOD * 1000000;
	int playoutHalfPer;// = Registry.ACCEL_PLAYOUT_HALF_PERIOD * 1000000;
	long playoutFirstTime;
	int playoutTicks;
	
	/* Internal non-functional stats. */
	int holdcount;
	int underflowCount;
	
	/* Only count stuff that I have, not that I don't have.
	 * Stuff that I don't have we take it by subtraction with the total. */
	int skipped;
	int played;
	
	/* Count duplicates that arrived before their original but anyway arrived late. */
	int duplicatesSkipped;
	
	/* Holds the last played sample. Starts off as null. Once any sample has played,
	 * it never turns null again, so it also indicates if playout has started. */
	E current;
	
	ZeMasterPlayoutManager master;
	
	ZeStream sourceStream;
	
	public ZePlayoutManager() {
		super();
		playoutTicks = 0;
		holdcount = 0;
		underflowCount = 0;
		skipped = 0;
		played = 0;
		duplicatesSkipped = 0;
		current = null;
	}
	
	
	public synchronized ZePlayoutElement<E> get() {
		
		/* sample current absolute time. */
		long now = playoutToSystem();
		long leftInterval = now-playoutHalfPer;
		long rightInterval = now+playoutHalfPer;

		/* sample it once from the master and use
		 * the same value for the whole playout request
		 * to avoid inconsistencies (mpo can be changed by
		 * the master at any time)
		 */
		long mpo = master.mpo;
		
		//System.out.println(Long.toString(mpo+first().wallclock)+
		//		" interval:"+Long.toString(leftInterval)+":"+Long.toString(rightInterval));
		
		while ( !isEmpty() ) {
			if ((first().wallclock+mpo) < leftInterval) {
				E s = pollFirst();
				System.out.println(Long.toString(mpo+s.wallclock)+
						" interval:"+Long.toString(leftInterval)+":"+Long.toString(rightInterval)+" -> "+
						"late skipped from sensor "+s.sensorId);
				skipped++;
				if (s.duplicate) duplicatesSkipped++;
			}
			else if ( (first().wallclock+mpo) >= leftInterval && 
					(first().wallclock+mpo) <= rightInterval ) {
				resetHoldCount();
				current = pollFirst();
				ZePlayoutElement<E> elem = new ZePlayoutElement<E>();
				elem.element = current;
				elem.meaning = Registry.PLAYOUT_VALID;
				System.out.println(Long.toString(mpo+current.wallclock)+
						" interval:"+Long.toString(leftInterval)+":"+Long.toString(rightInterval)+" -> "+
						"played from sensor "+current.sensorId);
				played++;
				return elem;
			}
			else { //samples to be played in the future is at queue head
				if (current != null) { //playout started
					if (now < current.expiry+mpo) { //current not expired
						//current still valid: hold it
						holdcount++;
						ZePlayoutElement<E> elem = new ZePlayoutElement<E>();
						elem.element = null;
						elem.meaning = Registry.PLAYOUT_HOLD;
						return elem;
					} else {
						resetHoldCount();
						ZePlayoutElement<E> elem = new ZePlayoutElement<E>();
						elem.element = null;
						elem.meaning = Registry.PLAYOUT_INVALID;
						return elem;
					}
				} else {
				//System.out.println("Giving invalidate");
					resetHoldCount();
					ZePlayoutElement<E> elem = new ZePlayoutElement<E>();
					elem.element = null;
					elem.meaning = Registry.PLAYOUT_INVALID;
					return elem;
				}
			}
		}
		
		//nothing found in the queue
		if (current != null) { //playout started
			if (now < current.expiry+mpo) { //current not expired
				//found empty but current still valid: holding
				holdcount++;
				ZePlayoutElement<E> elem = new ZePlayoutElement<E>();
				elem.element = null;
				elem.meaning = Registry.PLAYOUT_HOLD;
				return elem;
			}
			else {
				//found empty and current expired: underflow
				underflowCount++;
				ZePlayoutElement<E> elem = new ZePlayoutElement<E>();
				elem.element = null;
				elem.meaning = Registry.PLAYOUT_INVALID;
				return elem;
			}
		}
		//Found empty but playout not yet started
		ZePlayoutElement<E> elem = new ZePlayoutElement<E>();
		elem.element = null;
		elem.meaning = Registry.PLAYOUT_NOTSTARTED;
		return elem;		
	}
	
	void resetHoldCount() {
		holdcount = 0;
		//System.out.println("Holdcount reset at "+holdcount);
	}
		
	//In order to synchronize the superclass' add method
	public synchronized boolean add(E elem) {
		return super.add(elem);
	}
	
	/*
	 * we could sample the system time at every iteration
	 * but it would result in always different periods
	 * imperfect to be divided in half by the constant playoutHalfPer
	 * increase of fixed periods from a base system time
	 * adjust to the correct system time every x periods
	 */
	/* MIRROR CHANGES IN ZeDumbPlayoutManager!! */
	long playoutToSystem() {
		System.out.println("Inside playoutToSystem, playoutTicks:"+Integer.toString(playoutTicks));
		int ticksWrap = playoutTicks % 10;
		playoutTicks++;
		if ( true /*ticksWrap == 0*/) { //first call or every ten calls
			//playoutTicks=1;
			System.out.println("tickswrap = 0, adjusting");
			playoutFirstTime = System.nanoTime();
			return playoutFirstTime;
		}
		long freqFactor = Registry.WALLCLOCK_FREQ / playoutFreq;
		System.out.println("ticksWrap:"+Integer.toString(ticksWrap)+" playoutTicks:"+Integer.toString(playoutTicks));
		return playoutFirstTime+(freqFactor*ticksWrap);
	}

}