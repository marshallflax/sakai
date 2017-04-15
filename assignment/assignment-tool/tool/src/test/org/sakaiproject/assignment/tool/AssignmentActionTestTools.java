package org.sakaiproject.assignment.tool;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.FormattedText;

import lombok.extern.slf4j.Slf4j;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests for AssignmentAction
 */
@SuppressWarnings("deprecation")
@RunWith(PowerMockRunner.class)
@PrepareForTest({ComponentManager.class})
@Slf4j
public class AssignmentActionTestTools { 

    private AssignmentAction assignmentAction;
    @Mock
    private AssignmentService assignmentService;

	@Before
    public void setUp() {
        BasicConfigurator.configure();
        PowerMockito.mockStatic(ComponentManager.class);
        // A mock component manager.
        when(ComponentManager.get(any(Class.class))).then(new Answer<Object>() {
            private Map<Class, Object> mocks = new HashMap<>();
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Class classToMock = (Class) invocation.getArguments()[0];
                return mocks.computeIfAbsent(classToMock, k -> mock(classToMock));
            }
        });
        
        when(ComponentManager.get(SessionManager.class).getCurrentSession()).thenReturn(mock(Session.class));
        when(FormattedText.getDecimalSeparator()).thenReturn(".");
        
		when(FormattedText.getNumberFormat()).thenReturn(NumberFormat.getInstance(Locale.ENGLISH));
        assignmentAction = new AssignmentAction();

        Mockito.when(ComponentManager.get(AssignmentService.class)).thenReturn(assignmentService);

    }
	
	//This probably should also be moved from AssignmentAction to a util or something
	public Integer getScaleFactor(Integer decimals) {		
		return (int)Math.pow(10.0, decimals);
	}

    @Test
    public void testScalePointGrade() {
        SessionState state = mock(SessionState.class);
        //Simple state?
        final Map <String,Object> stateAttribute = new HashMap<String,Object> ();
        // mock for method setAttributepersist
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                if (arguments != null && arguments.length > 1 && arguments[0] != null) {
                    stateAttribute.put((String)arguments[0], arguments[1]);
                }
                return null;
            }
        }).when(state).setAttribute(any(String.class),any(Object.class));

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                if (arguments != null && arguments.length > 0 && arguments[0] != null) {
                	if (arguments[0] instanceof String) {
                		return stateAttribute.get((String)arguments[0]);
                	}
                }
                return null;
            }
        }).when(state).getAttribute(any(String.class));

        Integer decimals=2;
        when(ServerConfigurationService.getInt("assignment.grading.decimals", AssignmentConstants.DEFAULT_DECIMAL_POINT)).thenReturn(decimals);
        String scaledGrade = assignmentAction.scalePointGrade(state, ".7",getScaleFactor(decimals));
        assertEquals(scaledGrade,"70");
        //Verify the state message is null
        assertEquals(stateAttribute.get(AssignmentAction.STATE_MESSAGE), null);
        stateAttribute.clear();
        
        /* This case is broken at the moment but it does return invalid in the state
         */
        scaledGrade = assignmentAction.scalePointGrade(state, "1.23456789",getScaleFactor(decimals));
        assertEquals(scaledGrade,"1.23456789");
        //Verify the state message isn't null (indicating an error)
        assertNotEquals(stateAttribute.get(AssignmentAction.STATE_MESSAGE), null);
        stateAttribute.clear();

        decimals=4;
        when(ServerConfigurationService.getInt("assignment.grading.decimals", AssignmentConstants.DEFAULT_DECIMAL_POINT)).thenReturn(decimals);
        scaledGrade = assignmentAction.scalePointGrade(state, ".7",getScaleFactor(decimals));
        assertEquals(scaledGrade,"7000");
        //Verify the state message is null
        assertEquals(stateAttribute.get(AssignmentAction.STATE_MESSAGE), null);
        stateAttribute.clear();
    }
}
