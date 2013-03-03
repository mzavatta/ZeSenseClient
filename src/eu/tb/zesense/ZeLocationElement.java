package eu.tb.zesense;

public class ZeLocationElement extends ZeSensorElement {

	double x;
	double y;
	double z;
	
	/* 0 if normal value, 1 if "hold previous", 2 if "invalid". */
	int meaning;
	
}
