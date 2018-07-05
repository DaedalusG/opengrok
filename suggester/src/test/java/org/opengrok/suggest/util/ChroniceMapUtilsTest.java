/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 *
 * See LICENSE.txt included in this distribution for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at LICENSE.txt.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */

/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 */
package org.opengrok.suggest.util;

import net.openhft.chronicle.map.ChronicleMap;
import org.apache.lucene.util.BytesRef;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

public class ChroniceMapUtilsTest {

    private static final String FIELD = "test";

    private ChronicleMap<BytesRef, Integer> map;

    private Path tempFile;

    @Before
    public void setUp() throws IOException {
        tempFile = Files.createTempFile("opengrok", "test");

        map = ChronicleMap.of(BytesRef.class, Integer.class)
                .name(FIELD)
                .averageKeySize(3)
                .entries(10)
                .keyReaderAndDataAccess(BytesRefSizedReader.INSTANCE, new BytesRefDataAccess())
                .createOrRecoverPersistedTo(tempFile.toFile());
    }

    @After
    public void tearDown() throws IOException {
        Files.delete(tempFile);
    }

    @Test
    public void dataNotLostAfterResizeTest() throws IOException {
        fillData(0, 10, map);
        ChronicleMap<BytesRef, Integer> newMap = ChronicleMapUtils.resize(tempFile.toFile(), map, 20, 20);
        checkData(10, newMap);
    }

    private void fillData(int start, int end, ChronicleMap<BytesRef, Integer> map) {
        for (int i = start; i < end; i++) {
            map.put(new BytesRef("" + i), i);
        }
    }

    private void checkData(int count, ChronicleMap<BytesRef, Integer> map) {
        for (int i = 0; i < count; i++) {
            assertEquals(Integer.valueOf(i), map.get(new BytesRef("" + i)));
        }
    }

    @Test
    public void testResize() throws IOException {
        fillData(0, 10, map);
        ChronicleMap<BytesRef, Integer> newMap = ChronicleMapUtils.resize(tempFile.toFile(), map, 500, 20);

        fillData(10, 500, newMap);

        checkData(500, newMap);
    }

}
