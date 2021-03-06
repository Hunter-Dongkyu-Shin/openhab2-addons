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

import org.junit.Assert;
import org.junit.Test;
import org.openhab.binding.broadlink.BroadlinkBindingConstants;

import static org.junit.Assert.assertEquals;

public class ModelMapperTest {
    @Test
    public void mapsSpMini2ASp2() {
        assertEquals(
            BroadlinkBindingConstants.THING_TYPE_SP2,
            ModelMapper.getThingType(0x7539)
        );
    }
    @Test
    public void mapsRmMini3AsRm3() {
        assertEquals(
            BroadlinkBindingConstants.THING_TYPE_RM3,
            ModelMapper.getThingType(0x27c2)
        );
    }

    @Test
    public void mapsRm35f36AsRm4() {
        assertEquals(
            BroadlinkBindingConstants.THING_TYPE_RM4,
            ModelMapper.getThingType(0x5f36)
        );
    }

    @Test
    public void mapsRm4bAsRm4() {
        assertEquals(
                BroadlinkBindingConstants.THING_TYPE_RM4,
                ModelMapper.getThingType(0x51da)
        );
    }
    @Test
    public void mapsRm4ProAsRm4() {
        assertEquals(
                BroadlinkBindingConstants.THING_TYPE_RM4,
                ModelMapper.getThingType(0x61a2)
        );
    }

    @Test
    public void mapsRm462bcAsRm4() {
        assertEquals(
                BroadlinkBindingConstants.THING_TYPE_RM4,
                ModelMapper.getThingType(0x62bc)
        );
    }

    @Test
    public void throwsOnUnrecognisedDeviceModel() {
        try {
            ModelMapper.getThingType(0x6666);
            Assert.fail("Should have thrown on unmapped device model");
        } catch (Exception e) {
            assertEquals(
        "Device identifying itself as '26214' is not currently supported. Please report this to the developer!",
                e.getMessage()
            );
        }

    }
}
