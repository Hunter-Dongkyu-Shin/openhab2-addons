/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.broadlink.internal;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

import static org.junit.Assert.assertEquals;

public class ThingLoggerTest {

    private Thing mockThing = Mockito.mock(Thing.class);
    private Logger mockLogger = Mockito.mock(Logger.class);

    private ThingLogger thingLogger = new ThingLogger(mockThing, mockLogger);

    @Before
    public void before() {
        when(mockThing.getUID()).thenReturn(new ThingUID("broadlink:td1:1234"));
        when(mockThing.getStatus()).thenReturn(ThingStatus.ONLINE);

        when(mockLogger.isDebugEnabled()).thenReturn(true);
        when(mockLogger.isTraceEnabled()).thenReturn(true);
    }
    @Test
    public void canDescribeStatusOnline()  {
        String result = thingLogger.describeStatus();
        assertEquals("^", result);
    }

    @Test
    public void canDescribeStatusOffline() {
        when(mockThing.getStatus()).thenReturn(ThingStatus.OFFLINE);
        String result = thingLogger.describeStatus();
        assertEquals("v", result);
    }

    @Test
    public void canDescribeStatusIndeterminate() {
        when(mockThing.getStatus()).thenReturn(ThingStatus.INITIALIZING);
        String result = thingLogger.describeStatus();
        assertEquals("?", result);
    }

    @Test
    public void prependArgumentsNoArgs() {
        Object[] result = thingLogger.prependDescription();
        assertArrayEquals(new Object[] { "td1:1234", "^"}, result);
    }
    @Test
    public void prependArgumentsOneArg() {
        Object[] result = thingLogger.prependDescription( "a1");
        assertArrayEquals(new Object[] { "td1:1234", "^", "a1" }, result);
    }

    @Test
    public void prependArgumentsTwoArgs() {
        Object[] result = thingLogger.prependDescription( "a1", "a2");
        assertArrayEquals(new Object[] { "td1:1234", "^", "a1", "a2" }, result);
    }
    @Test
    public void prependArgumentsThreeArgs() {
        Object[] result = thingLogger.prependDescription( "a1", "a2", "a3");
        assertArrayEquals(new Object[] { "td1:1234", "^", "a1", "a2", "a3" }, result);
    }
    @Test
    public void logDebugDoesNothingIfDisabled() {
        when(mockLogger.isDebugEnabled()).thenReturn(false);
        thingLogger.logDebug("message");
        verify(mockLogger, times(0)).debug(anyString());
    }

    @Test
    public void logDebugPrependsThingInfoForZeroVarargs() {
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> argCaptor = ArgumentCaptor.forClass(Object[].class);
        thingLogger.logDebug("message");
        verify(mockLogger).debug(stringCaptor.capture(), argCaptor.capture());
        assertEquals("{}[{}]: {}", stringCaptor.getValue());
        List<Object> expected = new ArrayList<Object>();
        expected.add("td1:1234");
        expected.add("^");
        expected.add("message");
        assertEquals(
            expected,
            argCaptor.getAllValues()
        );
    }

    @Test
    public void logDebugPrependsThingInfoForOneVararg() {
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> argCaptor = ArgumentCaptor.forClass(Object[].class);
        thingLogger.logDebug("message with arg {}", "extra");
        verify(mockLogger).debug(stringCaptor.capture(), argCaptor.capture());
        assertEquals("{}[{}]: message with arg {}", stringCaptor.getValue());
        List<Object> expected = new ArrayList<Object>();
        expected.add("td1:1234");
        expected.add("^");
        expected.add("extra");
        assertEquals(
                expected,
                argCaptor.getAllValues()
        );
    }
    @Test
    public void logDebugPrependsThingInfoForTwoVarargs() {
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> argCaptor = ArgumentCaptor.forClass(Object[].class);
        thingLogger.logDebug("message with first {} second {}", "extra", "extra2");
        verify(mockLogger).debug(stringCaptor.capture(), argCaptor.capture());
        assertEquals("{}[{}]: message with first {} second {}", stringCaptor.getValue());
        List<Object> expected = new ArrayList<Object>();
        expected.add("td1:1234");
        expected.add("^");
        expected.add("extra");
        expected.add("extra2");
        assertEquals(
                expected,
                argCaptor.getAllValues()
        );
    }

    @Test
    public void logErrorPrependsThingInfoBeforeMessageWithNoThrowable() {
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> argCaptor = ArgumentCaptor.forClass(Object[].class);
        thingLogger.logError("error message");
        verify(mockLogger).error(stringCaptor.capture(), argCaptor.capture());
        assertEquals("{}[{}]: {}", stringCaptor.getValue());
        List<Object> expected = new ArrayList<Object>();
        expected.add("td1:1234");
        expected.add("^");
        expected.add("error message");
        assertEquals(
                expected,
                argCaptor.getAllValues()
        );
    }

    @Test
    public void logErrorPrependsThingInfoBeforeThrowable() {
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Throwable> throwableCaptor = ArgumentCaptor.forClass(Throwable.class);
        Throwable t = new IllegalArgumentException("foo");
        thingLogger.logError("error description", t);
        verify(mockLogger).error(stringCaptor.capture(), throwableCaptor.capture());
        assertEquals("td1:1234[^]: error description", stringCaptor.getValue());
        assertEquals(
                t,
                throwableCaptor.getValue()
        );
    }


    @Test
    public void logWarnPrependsThingInfoForZeroVarargs() {
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> argCaptor = ArgumentCaptor.forClass(Object[].class);
        thingLogger.logWarn("message");
        verify(mockLogger).warn(stringCaptor.capture(), argCaptor.capture());
        assertEquals("{}[{}]: {}", stringCaptor.getValue());
        List<Object> expected = new ArrayList<Object>();
        expected.add("td1:1234");
        expected.add("^");
        expected.add("message");
        assertEquals(
                expected,
                argCaptor.getAllValues()
        );
    }

    @Test
    public void logWarnPrependsThingInfoForOneVararg() {
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> argCaptor = ArgumentCaptor.forClass(Object[].class);
        thingLogger.logWarn("message with arg {}", "extra");
        verify(mockLogger).warn(stringCaptor.capture(), argCaptor.capture());
        assertEquals("{}[{}]: message with arg {}", stringCaptor.getValue());
        List<Object> expected = new ArrayList<Object>();
        expected.add("td1:1234");
        expected.add("^");
        expected.add("extra");
        assertEquals(
                expected,
                argCaptor.getAllValues()
        );
    }
    @Test
    public void logWarnPrependsThingInfoForTwoVarargs() {
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> argCaptor = ArgumentCaptor.forClass(Object[].class);
        thingLogger.logWarn("message with first {} second {}", "extra", "extra2");
        verify(mockLogger).warn(stringCaptor.capture(), argCaptor.capture());
        assertEquals("{}[{}]: message with first {} second {}", stringCaptor.getValue());
        List<Object> expected = new ArrayList<Object>();
        expected.add("td1:1234");
        expected.add("^");
        expected.add("extra");
        expected.add("extra2");
        assertEquals(
                expected,
                argCaptor.getAllValues()
        );
    }
    @Test
    public void logInfoPrependsThingInfoForZeroVarargs() {
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> argCaptor = ArgumentCaptor.forClass(Object[].class);
        thingLogger.logInfo("message");
        verify(mockLogger).info(stringCaptor.capture(), argCaptor.capture());
        assertEquals("{}[{}]: {}", stringCaptor.getValue());
        List<Object> expected = new ArrayList<Object>();
        expected.add("td1:1234");
        expected.add("^");
        expected.add("message");
        assertEquals(
                expected,
                argCaptor.getAllValues()
        );
    }

    @Test
    public void logInfoPrependsThingInfoForOneVararg() {
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> argCaptor = ArgumentCaptor.forClass(Object[].class);
        thingLogger.logInfo("message with arg {}", "extra");
        verify(mockLogger).info(stringCaptor.capture(), argCaptor.capture());
        assertEquals("{}[{}]: message with arg {}", stringCaptor.getValue());
        List<Object> expected = new ArrayList<Object>();
        expected.add("td1:1234");
        expected.add("^");
        expected.add("extra");
        assertEquals(
                expected,
                argCaptor.getAllValues()
        );
    }
    @Test
    public void logInfoPrependsThingInfoForTwoVarargs() {
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> argCaptor = ArgumentCaptor.forClass(Object[].class);
        thingLogger.logInfo("message with first {} second {}", "extra", "extra2");
        verify(mockLogger).info(stringCaptor.capture(), argCaptor.capture());
        assertEquals("{}[{}]: message with first {} second {}", stringCaptor.getValue());
        List<Object> expected = new ArrayList<Object>();
        expected.add("td1:1234");
        expected.add("^");
        expected.add("extra");
        expected.add("extra2");
        assertEquals(
                expected,
                argCaptor.getAllValues()
        );
    }

    @Test
    public void logTraceDoesNothingIfDisabled() {
        when(mockLogger.isTraceEnabled()).thenReturn(false);
        thingLogger.logTrace("message");
        verify(mockLogger, times(0)).trace(anyString());
    }

    @Test
    public void logTracePrependsThingInfoForZeroVarargs() {
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> argCaptor = ArgumentCaptor.forClass(Object[].class);
        thingLogger.logTrace("message");
        verify(mockLogger).trace(stringCaptor.capture(), argCaptor.capture());
        assertEquals("{}[{}]: {}", stringCaptor.getValue());
        List<Object> expected = new ArrayList<Object>();
        expected.add("td1:1234");
        expected.add("^");
        expected.add("message");
        assertEquals(
                expected,
                argCaptor.getAllValues()
        );
    }

    @Test
    public void logTracePrependsThingInfoForOneVararg() {
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> argCaptor = ArgumentCaptor.forClass(Object[].class);
        thingLogger.logTrace("message with arg {}", "extra");
        verify(mockLogger).trace(stringCaptor.capture(), argCaptor.capture());
        assertEquals("{}[{}]: message with arg {}", stringCaptor.getValue());
        List<Object> expected = new ArrayList<Object>();
        expected.add("td1:1234");
        expected.add("^");
        expected.add("extra");
        assertEquals(
                expected,
                argCaptor.getAllValues()
        );
    }
    @Test
    public void logTracePrependsThingInfoForTwoVarargs() {
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> argCaptor = ArgumentCaptor.forClass(Object[].class);
        thingLogger.logTrace("message with first {} second {}", "extra", "extra2");
        verify(mockLogger).trace(stringCaptor.capture(), argCaptor.capture());
        assertEquals("{}[{}]: message with first {} second {}", stringCaptor.getValue());
        List<Object> expected = new ArrayList<Object>();
        expected.add("td1:1234");
        expected.add("^");
        expected.add("extra");
        expected.add("extra2");
        assertEquals(
                expected,
                argCaptor.getAllValues()
        );
    }
}
