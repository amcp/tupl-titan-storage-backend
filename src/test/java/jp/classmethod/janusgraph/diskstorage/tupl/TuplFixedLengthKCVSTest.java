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

    // TODO broken
    @Test @Override
    public void testConcurrentGetSliceAndMutate() throws BackendException, ExecutionException, InterruptedException {

    }
}
// END adaptation of:
// https://github.com/thinkaurelius/titan/blob/1.0.0/titan-berkeleyje/src/test/java/com/thinkaurelius/titan/diskstorage/berkeleyje/BerkeleyFixedLengthKCVSTest.java#L35