package com.citsgbt.mobile.gateway.security.sso.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by gary.fu on 2017/1/19.
 */
public class DateUtils {

	private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);

	private DateUtils() {

	}

	/**
	 * @param createdDate
	 * @param timeToLive
	 * @param futureTimeToLive
	 * @return
	 */
	public static boolean verifyCreated(Date createdDate, int timeToLive, int futureTimeToLive) {
		if (createdDate == null) {
			return false;
		}
		Date validCreation = new Date();
		long currentTime = validCreation.getTime();
		if (futureTimeToLive > 0) {
			validCreation.setTime(currentTime + ((long) futureTimeToLive * 1000L));
		}
		// Check to see if the created time is in the future
		if (createdDate.after(validCreation)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Validation of Created: The message was created in the future!");
			}
			return false;
		}
		// Calculate the time that is allowed for the message to travel
		currentTime -= ((long) timeToLive * 1000L);
		validCreation.setTime(currentTime);
		// Validate the time it took the message to travel
		if (createdDate.before(validCreation)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Validation of Created: The message was created too long ago");
			}
			return false;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Validation of Created: Everything is ok");
		}
		return true;
	}

}
