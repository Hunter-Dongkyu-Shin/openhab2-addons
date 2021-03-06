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
package org.openhab.binding.broadlink.handler;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationHelper;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.broadlink.internal.Utils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Remote blaster handler
 *
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public class BroadlinkRemoteHandler extends BroadlinkBaseThingHandler {

    public BroadlinkRemoteHandler(Thing thing) {
        super(thing, LoggerFactory.getLogger(BroadlinkRemoteHandler.class));
    }

    public BroadlinkRemoteHandler(Thing thing, Logger logger) {
        super(thing, logger);
    }

    protected void sendCode(byte code[]) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            byte[] preamble = new byte[4];
            preamble[0] = 2;
            outputStream.write(preamble);
            outputStream.write(code);
            byte[] padded = Utils.padTo(outputStream.toByteArray(), 16);
            byte[] message = buildMessage((byte) 0x6a, padded);
            sendAndReceiveDatagram(message, "remote code");
        } catch (IOException e) {
            thingLogger.logError("Exception while sending code", e);
        }
    }

    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!Utils.isOnline(getThing())) {
            thingLogger.logDebug("Can't handle command " + command +" because handler for thing " + getThing().getLabel() + " is not ONLINE");
            return;
        }
        if (command instanceof RefreshType) {
            updateItemStatus();
            return;
        }
        Channel channel = thing.getChannel(channelUID.getId());
        if (channel == null) {
            thingLogger.logError("Unexpected null channel while handling command " + command.toFullString());
            return;
        }
        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        if (channelTypeUID == null) {
            thingLogger.logError("Unexpected null channelTypeUID while handling command " + command.toFullString());
            return;
        }
        String s;
        switch ((s = channelTypeUID.getId()).hashCode()) {
        case 950394699: // FIXME WTF?!?!
            if (s.equals("command")) {
                thingLogger.logDebug("Handling ir/rf command {} on channel {} of thing {}",
                        new Object[] { command, channelUID.getId(), getThing().getLabel() });
                byte code[] = lookupCode(command, channelUID);
                if (code != null)
                    sendCode(code);
                break;
            }
            // fall through

        default:
            thingLogger.logDebug("Thing " + getThing().getLabel() + " has unknown channel type " + channelTypeUID.getId());
            break;
        }
    }

    private byte @Nullable [] lookupCode(Command command, ChannelUID channelUID) {
        if (command.toString() == null) {
            thingLogger.logDebug("Unable to perform transform on null command string");
            return null;
        }
        String mapFile = (String) thing.getConfiguration().get("mapFilename");
        if (StringUtils.isEmpty(mapFile)) {
            thingLogger.logDebug("MAP file is not defined in configuration of thing " + getThing().getLabel());
            return null;
        }
        BundleContext bundleContext = FrameworkUtil.getBundle(BroadlinkRemoteHandler.class).getBundleContext();
        TransformationService transformService = TransformationHelper.getTransformationService(bundleContext, "MAP");
        if (transformService == null) {
            thingLogger.logError("Failed to get MAP transformation service for thing " + getThing().getLabel() +"; is bundle installed?");
            return null;
        }
        byte code[] = null;
        String value;
        try {
            value = transformService.transform(mapFile, command.toString());
            code = HexUtils.hexToBytes(value);
        } catch (TransformationException e) {
            thingLogger.logError(
            "Failed to transform command '" + command + "' for thing " + getThing().getLabel() + " using map file '" + mapFile + "'",
            e);
            return null;
        }
        if (StringUtils.isEmpty(value)) {
            thingLogger.logError(
                "No entry for command '" + command + "' in map file '" + mapFile + "' for thing getThing().getLabel()"
            );
            return null;
        }
        thingLogger.logDebug("Transformed command '{}' for thing {} with map file '{}'",
                new Object[] { command, getThing().getLabel(), mapFile });
        return code;
    }

}
