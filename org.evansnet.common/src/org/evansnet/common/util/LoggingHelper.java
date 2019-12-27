package org.evansnet.common.util;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingHelper {
	
	public static void printStackTrace(Logger l, StackTraceElement[] s) {
		int member = 0;
		StringBuilder stack = new StringBuilder();
		while(member < s.length) {
			stack.append(s[member] + "\n");
			member++;
		}
		l.log(Level.SEVERE, stack.toString());	

	}
}
