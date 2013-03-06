package eu.tb.zesense;

public class Registry {
	
	// resource URI path used for discovery
	public static final String DISCOVERY_RESOURCE = "/.well-known/core";

	// indices of command line parameters
	public static final int IDX_METHOD          = 0;
	public static final int IDX_URI             = 1;
	public static final int IDX_PAYLOAD         = 2;

	// exit codes for runtime errors
	public static final int ERR_MISSING_METHOD  = 1;
	public static final int ERR_UNKNOWN_METHOD  = 2;
	public static final int ERR_MISSING_URI     = 3;
	public static final int ERR_BAD_URI         = 4;
	public static final int ERR_REQUEST_FAILED  = 5;
	public static final int ERR_RESPONSE_FAILED = 6;
	public static final int ERR_BAD_LINK_FORMAT = 7;
	
	public static final int DATAPOINT	= 1;
	public static final int SENDREPORT	= 2;
	public static final int RECREPORT	= 3;
	
	/* <source>/hardware/libhardware/include/hardware/sensors.h */
	public static final int SENSOR_TYPE_ACCELEROMETER 		= 1;
	public static final int SENSOR_TYPE_MAGNETIC_FIELD 		= 2;
	public static final int SENSOR_TYPE_ORIENTATION			= 3;
	public static final int SENSOR_TYPE_GYROSCOPE			= 4;
	public static final int SENSOR_TYPE_LIGHT				= 5;
	public static final int SENSOR_TYPE_PRESSURE				= 6;
	public static final int SENSOR_TYPE_TEMPERATURE			= 7;  // deprecated
	public static final int SENSOR_TYPE_PROXIMITY			= 8;
	public static final int SENSOR_TYPE_GRAVITY				= 9;
	public static final int SENSOR_TYPE_LINEAR_ACCELERATION	= 10;
	public static final int SENSOR_TYPE_ROTATION_VECTOR		= 11;
	public static final int SENSOR_TYPE_RELATIVE_HUMIDITY	= 12;
	public static final int SENSOR_TYPE_AMBIENT_TEMPERATURE	= 13;
	// Definitions for our custom types
	public static final int ZESENSE_SENSOR_TYPE_LOCATION		= 14;
	
	
	public static final int PAYLOAD_HDR_LENGTH = 4; //bytes
	
	/* Modeled as agreed in advance e.g. RTP A/V profile. */
	public static final int WALLCLOCK_FREQ = 1000000000; //Hz
	public static final int TIMESTAMP_FREQ = 1000; //Hz
	
	/* Debug value, in a good implementation this value is decided
	 * request by request and communicated to the server as a resource
	 * query string or something similar.
	 */
	public static final int ACCEL_STREAM_FREQ = 5;
	
	
	public static final int ACCEL_PLAYOUT_FREQ = 10; //HZ
	public static final int ACCEL_PLAYOUT_PERIOD = 100; //msec
	public static final int ACCEL_PLAYOUT_HALF_PERIOD = 50; //msec
	
	
	public static final int PLAYOUT_HOLD = 1;
	public static final int PLAYOUT_VALID = 2;
	public static final int PLAYOUT_INVALID = 3;

}
