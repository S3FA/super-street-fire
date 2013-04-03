package ca.site3.ssf.gesturerecognizer;

import be.ac.ulg.montefiore.run.jahmm.io.FileFormatException;

/**
 * Exception class to signify errors specific to the formatting/syntax in the 
 * Gesture Recognition Engine (.eng) file format.
 * 
 * @author Callum
 *
 */
public class SSFEngFileFormatException extends FileFormatException {
	private static final long serialVersionUID = 1L;
	public SSFEngFileFormatException(String info) {
		super(info);
	}
}
