/*
 * Copyright 2016 Classmethod, Inc. or its affiliates. All Rights Reserved.
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
package jp.classmethod.janusgraph.graphdb.tupl;

import jp.classmethod.janusgraph.diskstorage.tupl.TuplStoreManager;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.janusgraph.core.*;
import org.janusgraph.core.schema.ConsistencyModifier;
import org.janusgraph.diskstorage.BackendTransaction;
import org.janusgraph.diskstorage.configuration.WriteConfiguration;
import org.janusgraph.graphdb.JanusGraphTest;

import jp.classmethod.janusgraph.TuplStorageSetup;
import org.janusgraph.graphdb.transaction.StandardJanusGraphTx;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

/**
 * 
 * @author Alexander Patrikalakis
 *
 */
public class TuplGraphTest extends JanusGraphTest {

    @Rule
    public TestName testName = new TestName();

    @Override
    protected boolean isLockingOptimistic() {
        return true;
    }

    @Override
    public WriteConfiguration getConfiguration() {
        return TuplStorageSetup.getTuplStorageWriteConfiguration("TuplGraphTest#" + testName.getMethodName());
    }

    @Override
    @Test
    public void testConsistencyEnforcement() {
        PropertyKey uid = makeVertexIndexedUniqueKey("uid", Integer.class);
        PropertyKey name = makeKey("name", String.class);
        mgmt.setConsistency(uid, ConsistencyModifier.LOCK);
        mgmt.setConsistency(name, ConsistencyModifier.LOCK);
        mgmt.setConsistency(mgmt.getGraphIndex("uid"), ConsistencyModifier.LOCK);
        EdgeLabel knows = mgmt.makeEdgeLabel("knows").multiplicity(Multiplicity.SIMPLE).make();
        EdgeLabel spouse = mgmt.makeEdgeLabel("spouse").multiplicity(Multiplicity.ONE2ONE).make();
        EdgeLabel connect = mgmt.makeEdgeLabel("connect").multiplicity(Multiplicity.MULTI).make();
        EdgeLabel related = mgmt.makeEdgeLabel("related").multiplicity(Multiplicity.MULTI).make();
        mgmt.setConsistency(knows, ConsistencyModifier.LOCK);
        mgmt.setConsistency(spouse, ConsistencyModifier.LOCK);
        mgmt.setConsistency(related, ConsistencyModifier.FORK);
        finishSchema();

        name = tx.getPropertyKey("name");
        connect = tx.getEdgeLabel("connect");
        related = tx.getEdgeLabel("related");

        JanusGraphVertex v1 = tx.addVertex("uid", 1);
        JanusGraphVertex v2 = tx.addVertex("uid", 2);
        JanusGraphVertex v3 = tx.addVertex("uid", 3);

        Edge e1 = v1.addEdge(connect.name(), v2, name.name(), "e1");
        Edge e2 = v1.addEdge(related.name(), v2, name.name(), "e2");

        newTx();
        v1 = getV(tx, v1);
        /*
         ==== check fork, no fork behavior
         */
        long e1id = getId(e1);
        long e2id = getId(e2);
        e1 = getOnlyElement((Iterable<JanusGraphEdge>) v1.query().direction(Direction.OUT).labels("connect").edges());
        assertEquals("e1", e1.value("name"));
        assertEquals(e1id, getId(e1));
        e2 = getOnlyElement((Iterable<JanusGraphEdge>) v1.query().direction(Direction.OUT).labels("related").edges());
        assertEquals("e2", e2.value("name"));
        assertEquals(e2id, getId(e2));
        //Update edges - one should simply update, the other fork
        e1.property("name", "e1.2");
        e2.property("name", "e2.2");

        newTx();
        v1 = getV(tx, v1);

        e1 = getOnlyElement((Iterable<JanusGraphEdge>) v1.query().direction(Direction.OUT).labels("connect").edges());
        assertEquals("e1.2", e1.value("name"));
        assertEquals(e1id, getId(e1)); //should have same id
        e2 = getOnlyElement((Iterable<JanusGraphEdge>) v1.query().direction(Direction.OUT).labels("related").edges());
        assertEquals("e2.2", e2.value("name"));
        assertNotEquals(e2id, getId(e2)); //should have different id since forked

        clopen();

        /*
         === check cross transaction
         */
        final Random random = new Random();
        final long vids[] = {getId(v1), getId(v2), getId(v3)};
        //1) Index uniqueness
        executeLockConflictingTransactionJobs(graph, new TransactionJob() {
            private int pos = 0;

            @Override
            public void run(JanusGraphTransaction tx) {
                long vid = vids[pos];
                System.out.println("getting vertex at position " + pos + " in tx " + tx);
                pos++;
                JanusGraphVertex u = getV(tx, vid);
                System.out.println("before changing property uid to 5 for vertex at pos " + pos + " in tx " + tx);
                u.property(VertexProperty.Cardinality.single, "uid", 5);
                System.out.println("after changing property uid to 5 for vertex at pos " + pos + " in tx " + tx);
            }
        });
        //2) Property out-uniqueness
        executeLockConflictingTransactionJobs(graph, new TransactionJob() {
            @Override
            public void run(JanusGraphTransaction tx) {
                JanusGraphVertex u = getV(tx, vids[0]);
                u.property(VertexProperty.Cardinality.single, "name", "v" + random.nextInt(10));
            }
        });
        //3) knows simpleness
        executeLockConflictingTransactionJobs(graph, new TransactionJob() {
            @Override
            public void run(JanusGraphTransaction tx) {
                JanusGraphVertex u1 = getV(tx, vids[0]), u2 = getV(tx, vids[1]);
                u1.addEdge("knows", u2);
            }
        });
        //4) knows one2one (in 2 separate configurations)
        executeLockConflictingTransactionJobs(graph, new TransactionJob() {
            private int pos = 1;

            @Override
            public void run(JanusGraphTransaction tx) {
                JanusGraphVertex u1 = getV(tx, vids[0]),
                        u2 = getV(tx, vids[pos++]);
                u1.addEdge("spouse", u2);
            }
        });
        executeLockConflictingTransactionJobs(graph, new TransactionJob() {
            private int pos = 1;

            @Override
            public void run(JanusGraphTransaction tx) {
                JanusGraphVertex u1 = getV(tx, vids[pos++]),
                        u2 = getV(tx, vids[0]);
                u1.addEdge("spouse", u2);
            }
        });

        //######### TRY INVALID CONSISTENCY
        try {
            //Fork does not apply to constrained types
            mgmt.setConsistency(mgmt.getPropertyKey("name"), ConsistencyModifier.FORK);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * A piece of logic to be executed in a transactional context
     */
    private interface TransactionJob {
        void run(JanusGraphTransaction tx);
    }

    /**
     * Executes a transaction job in two parallel transactions under the assumptions that the two transactions
     * should conflict and the one committed later should throw a locking exception.
     *
     * @param graph
     * @param job
     */
    private void executeLockConflictingTransactionJobs(JanusGraph graph, TransactionJob job) {
        JanusGraphTransaction tx1 = graph.newTransaction();
        BackendTransaction tx1back = ((StandardJanusGraphTx) tx1).getTxHandle();
        JanusGraphTransaction tx2 = graph.newTransaction();
        BackendTransaction tx2back = ((StandardJanusGraphTx) tx2).getTxHandle();
        System.out.println("\nbefore change index property in tx1");
        job.run(tx1);
        System.out.println("after change index property in tx1\n");
        System.out.println("before change index property in tx2");
        job.run(tx2);
        System.out.println("after change index property in tx2");
        /*
         * Under pessimistic locking, tx1 should abort and tx2 should commit.
         * Under optimistic locking, tx1 may commit and tx2 may abort.
         */
        if (isLockingOptimistic()) {
            System.out.println("before commit tx1");
            tx1.commit();
            System.out.println("after commit tx1");
            try {
                System.out.println("before commit tx2");
                tx2.commit();
                System.out.println("after commit tx2");
                fail("Storage backend does not abort conflicting transactions");
            } catch (JanusGraphException e) {
            }
        } else {
            try {
                System.out.println("before commit tx1");
                tx1.commit();
                System.out.println("after commit tx1");
                fail("Storage backend does not abort conflicting transactions");
            } catch (JanusGraphException e) {
            }
            System.out.println("before commit tx2");
            tx2.commit();
            System.out.println("after commit tx2");
        }
    }

}
