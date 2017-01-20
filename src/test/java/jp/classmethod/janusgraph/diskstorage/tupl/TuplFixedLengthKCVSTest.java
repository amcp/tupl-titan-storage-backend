/*
 * Copyright 2016 Classmethod, Inc. or its affiliates. All Rights Reserved.
 * Portions copyright Titan: Distributed Graph Database - Copyright 2012 and onwards Aurelius.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package jp.classmethod.janusgraph.diskstorage.tupl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import org.janusgraph.diskstorage.keycolumnvalue.StoreManager;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import jp.classmethod.janusgraph.TuplStorageSetup;
import jp.classmethod.janusgraph.diskstorage.tupl.TuplStoreManager;

import org.janusgraph.diskstorage.BackendException;
import org.janusgraph.diskstorage.KeyColumnValueStoreTest;
import org.janusgraph.diskstorage.keycolumnvalue.KeyColumnValueStoreManager;
import org.janusgraph.diskstorage.keycolumnvalue.keyvalue.OrderedKeyValueStoreManagerAdapter;

/**
 * @author Alexander Patrikalakis
 *
 */
// BEGIN adaptation of:
// https://github.com/thinkaurelius/titan/blob/1.0.0/titan-berkeleyje/src/test/java/com/thinkaurelius/titan/diskstorage/berkeleyje/BerkeleyFixedLengthKCVSTest.java#L14
public class TuplFixedLengthKCVSTest extends KeyColumnValueStoreTest {
    @Override
    public KeyColumnValueStoreManager openStorageManager() throws BackendException {
        final TuplStoreManager sm = new TuplStoreManager(TuplStorageSetup.getTuplStorageConfiguration());
        return new OrderedKeyValueStoreManagerAdapter(sm, ImmutableMap.of(storeName, 8));

    }

    @Before
    @Override
    public void setUp() throws Exception {
        Stopwatch watch = Stopwatch.createStarted();
        StoreManager m = openStorageManager();
        System.out.println("Time to open store manager: " + watch.elapsed(TimeUnit.MILLISECONDS));
        watch.reset();
        watch.start();
        m.clearStorage();
        System.out.println("Time to clear storage: " + watch.elapsed(TimeUnit.MILLISECONDS));
        watch.reset();
        watch.start();
        m.close();
        System.out.println("Time to close store manager: " + watch.elapsed(TimeUnit.MILLISECONDS));
        watch.reset();
        watch.start();
        open();
        System.out.println("Time to open: " + watch.elapsed(TimeUnit.MILLISECONDS));
        watch.stop();
    }

    // TODO broken
    @Test @Override
    public void testConcurrentGetSliceAndMutate() throws BackendException, ExecutionException, InterruptedException {

    }

    @Test
    public void storeAndRetrieveWithClosing() throws BackendException {
        Stopwatch watch = Stopwatch.createStarted();
        String[][] values = generateValues();
        System.out.println("Time to generate values: " + watch.elapsed(TimeUnit.MILLISECONDS));
        watch.reset();
        watch.start();
        loadValues(values);
        System.out.println("Time to load values: " + watch.elapsed(TimeUnit.MILLISECONDS));
        watch.reset();
        watch.start();
        clopen();
        System.out.println("Time to closeopen: " + watch.elapsed(TimeUnit.MILLISECONDS));
        watch.reset();
        watch.start();
        checkValueExistence(values);
        System.out.println("Time to check value existence: " + watch.elapsed(TimeUnit.MILLISECONDS));
        watch.reset();
        watch.start();
        checkValues(values);
        System.out.println("Time to check value existence: " + watch.elapsed(TimeUnit.MILLISECONDS));
        watch.stop();
    }
}
// END adaptation of:
// https://github.com/thinkaurelius/titan/blob/1.0.0/titan-berkeleyje/src/test/java/com/thinkaurelius/titan/diskstorage/berkeleyje/BerkeleyFixedLengthKCVSTest.java#L35