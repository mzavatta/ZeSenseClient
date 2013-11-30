package eu.tb.zesense;

public class ZeSampleRegisterEntry implements Comparable<ZeSampleRegisterEntry> {
	
	int timestamp;
	
	/* true if original has arrived first, false if duplicate arrived first. */
	boolean firstOriginal;

	boolean gotOriginal	;
	boolean gotDuplicate;
	
	/* either the original or the duplicate played i.e. buffered. */
	boolean useful;
	
	/* true if the one that arrived second would have played. */
	boolean twiceUseful;
	
	public ZeSampleRegisterEntry(int timestamp) {
		this.timestamp = timestamp;
		gotOriginal = false;
		gotDuplicate = false;
		useful = false;
		twiceUseful = false;
		firstOriginal = false;
	}
	
	@Override
	public int compareTo(ZeSampleRegisterEntry obj) {
		if (this.timestamp < obj.timestamp) return -1;
		else if (this.timestamp > obj.timestamp) return +1;
		return 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + timestamp;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ZeSampleRegisterEntry other = (ZeSampleRegisterEntry) obj;
		if (timestamp != other.timestamp)
			return false;
		return true;
	}
}
