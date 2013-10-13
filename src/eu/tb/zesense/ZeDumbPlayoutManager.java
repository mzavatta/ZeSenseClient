package eu.tb.zesense;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ZeDumbPlayoutManager<E extends ZeSensorElement> extends ConcurrentLinkedQueue<E> {
	
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
	int playedLate;
	int playedEarly;
	
	/* Holds the last played sample. Starts off as null. Once any sample has played,
	 * it never turns null again, so it also indicates if playout has started. */
	E current;
	
	ZeMasterPlayoutManager master;
	
	ZeStream sourceStream;
	
	boolean playoutStarted;
	
	public ZeDumbPlayoutManager() {
		super();
		playoutTicks = 0;
		holdcount = 0;
		underflowCount = 0;
		skipped = 999999;
		played = 0;
		current = null;
		playoutStarted = false;
	}
	
	/* assumes samples to arrive when they are to be played. 
	 * always pass the earliest arrived sample
	 * if the network is perfect the queue will contain at most
	 * one sample, this method will hand it out at the first opportunity
	 * playing the sample in its time slot
	 * (assuming the playout request rate is enough greater than the
	 * sample arrival rate)
	 */
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
		
		//take queue head
		current = poll();
		ZePlayoutElement<E> elem = new ZePlayoutElement<E>();
		elem.element = current;
		
		//if a sample was found, play it
		if (current != null) {
			elem.meaning = Registry.PLAYOUT_VALID;
			//playout has started
			playoutStarted = true;
			//account if by chance it plays in its time slot
			if ((current.wallclock+mpo) >= leftInterval && 
					(current.wallclock+mpo) <= rightInterval )
				played++;
			else if ((current.wallclock+mpo) <= leftInterval)
				playedLate++;
			else playedEarly++;
		}
		else {
			if (playoutStarted) elem.meaning = Registry.PLAYOUT_HOLD;
			else elem.meaning = Registry.PLAYOUT_NOTSTARTED;
		}
		
		return elem;
	}
	
	/*
	 * we could sample the system time at every iteration
	 * but it would result in always different periods
	 * imperfect to be divided in half by the constant playoutHalfPer
	 * increase of fixed periods from a base system time
	 * adjust to the correct system time every x periods
	 */
	/* MIRROR CHANGES IN ZePlayoutManager!! */
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
