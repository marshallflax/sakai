package org.sakaiproject.delegatedaccess.logic;

import java.util.Observable;
import java.util.Observer;

import lombok.Getter;
import lombok.Setter;

import org.apache.log4j.Logger;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.tool.api.SessionManager;

/**
 * This is an Observer for Delegated Access.  It listens for a user to login and checks
 * if they have any delegated access.  If so, then it populates the user's session with
 * the access information.
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
public class DelegatedAccessObserver implements Observer {
	@Getter @Setter
	private ProjectLogic projectLogic;
	@Getter @Setter
	private EventTrackingService eventTrackingService;	
	@Getter @Setter
	private SessionManager sessionManager;

	private static final Logger log = Logger.getLogger(DelegatedAccessObserver.class);

	public void init() {
		log.info("init()");
		eventTrackingService.addObserver(this);
	}

	@Override
	public void update(Observable arg0, Object arg) {
		if (!(arg instanceof Event))
			return;

		Event event = (Event) arg;

		// check the event function against the functions we have notifications watching for
		if (UsageSessionService.EVENT_LOGIN.equals(event.getEvent())
				|| UsageSessionService.EVENT_LOGIN_CONTAINER.equals(event.getEvent())) {
			projectLogic.initializeDelegatedAccessSession(sessionManager.getCurrentSessionUserId());
		}
	}

}
