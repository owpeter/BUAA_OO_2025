import com.oocourse.spec1.exceptions.*;
import com.oocourse.spec1.main.PersonInterface;
import com.oocourse.spec1.main.TagInterface; // Assuming you have a Tag implementation
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class NetworkTest {

    private Network network; // Use your Network class
    private Person p1, p2, p3, p4, p5, p6;

    @Before
    public void setUp() throws Exception {
        network = new Network(); // Instantiate your Network class
        p1 = new Person(1, "Alice", 20);
        p2 = new Person(2, "Bob", 25);
        p3 = new Person(3, "Charlie", 30);
        p4 = new Person(4, "David", 35);
        p5 = new Person(5, "Eve", 40);
        p6 = new Person(6, "Frank", 45); // Add more persons for complex tests

        // Add persons to the network
        network.addPerson(p1);
        network.addPerson(p2);
        network.addPerson(p3);
        network.addPerson(p4);
        network.addPerson(p5);
        network.addPerson(p6);
    }

    // --- Basic Tests ---

    @Test
    public void testInitialTripleSumIsEmpty() {
        assertEquals(0, network.queryTripleSum());
    }

    @Test
    public void testTripleSumAfterOneRelation() throws Exception {
        network.addRelation(1, 2, 100);
        assertEquals(0, network.queryTripleSum());
    }

    @Test
    public void testTripleSumAfterTwoDisconnectedRelations() throws Exception {
        network.addRelation(1, 2, 100);
        network.addRelation(3, 4, 50);
        assertEquals(0, network.queryTripleSum());
    }

    @Test
    public void testTripleSumAfterTwoConnectedRelations() throws Exception {
        network.addRelation(1, 2, 100);
        network.addRelation(2, 3, 50); // Path 1-2-3
        assertEquals(0, network.queryTripleSum());
    }


    @Test
    public void testTripleSumFormingFirstTriple() throws Exception {
        network.addRelation(1, 2, 100);
        assertEquals(0, network.queryTripleSum());
        network.addRelation(2, 3, 50);
        assertEquals(0, network.queryTripleSum());
        network.addRelation(1, 3, 75);
        assertEquals(1, network.queryTripleSum());
    }

    @Test
    public void testTripleSumBuildingK4Stepwise() throws Exception {
        network.addRelation(1, 2, 10);
        assertEquals(0, network.queryTripleSum());

        network.addRelation(1, 3, 20);
        assertEquals(0, network.queryTripleSum());

        network.addRelation(2, 3, 30);
        assertEquals(1, network.queryTripleSum());

        network.addRelation(1, 4, 40);
        assertEquals(1, network.queryTripleSum());

        network.addRelation(2, 4, 50);
        assertEquals(2, network.queryTripleSum());

        network.addRelation(3, 4, 60);
        assertEquals(4, network.queryTripleSum());
    }

    @Test
    public void testTripleSumRemovingEdgesFromK4() throws Exception {
        // Setup K4: Expected sum = 4
        network.addRelation(1, 2, 10);
        network.addRelation(1, 3, 20);
        network.addRelation(2, 3, 30);
        network.addRelation(1, 4, 40);
        network.addRelation(2, 4, 50);
        network.addRelation(3, 4, 60);
        assertEquals(4, network.queryTripleSum());
        network.modifyRelation(3, 4, -60);
        assertEquals( 2, network.queryTripleSum()); // Remaining: (1,2,3), (1,2,4)

        network.modifyRelation(1, 2, -100); // Value becomes < 0, edge removed
        assertEquals( 0, network.queryTripleSum());

        // Re-add edge (3,4)
        network.addRelation(3, 4, 5);
        assertEquals(2, network.queryTripleSum());

        // Re-add edge (1,2)
        network.addRelation(1, 2, 5);
        // Now we have edges (1,3), (2,3), (1,4), (2,4), (3,4), (1,2)
        // Common neighbors of 1 and 2 are 3 and 4. Triples (1,2,3) and (1,2,4) are reformed. Count = 2.
        // Common neighbors of 3 and 4 are 1 and 2. Triples (1,3,4) and (2,3,4) are reformed. Count = 2 + 2 = 4.
        assertEquals(4, network.queryTripleSum());
    }


    // --- Modify Relation Tests ---

    @Test
    public void testTripleSumModifyRelationIncreaseValueNoChange() throws Exception {
        network.addRelation(1, 2, 10);
        network.addRelation(2, 3, 20);
        network.addRelation(1, 3, 30); // Sum = 1
        assertEquals(1, network.queryTripleSum());

        network.modifyRelation(1, 2, 50); // Value becomes 60 > 0
        assertEquals(1, network.queryTripleSum());
    }

    @Test
    public void testTripleSumModifyRelationDecreaseValuePositiveNoChange() throws Exception {
        network.addRelation(1, 2, 50);
        network.addRelation(2, 3, 20);
        network.addRelation(1, 3, 30); // Sum = 1
        assertEquals(1, network.queryTripleSum());

        network.modifyRelation(1, 2, -10); // Value becomes 40 > 0
        assertEquals(1, network.queryTripleSum());
    }

    @Test
    public void testTripleSumModifyRelationRemoveEdgeExactlyZero() throws Exception {
        network.addRelation(1, 2, 10);
        network.addRelation(2, 3, 20);
        network.addRelation(1, 3, 30); // Sum = 1
        assertEquals(1, network.queryTripleSum());

        // Remove edge (1,3), breaking the only triple (1,2,3)
        network.modifyRelation(1, 3, -30); // Value becomes exactly 0
        assertEquals(0, network.queryTripleSum());
    }

    @Test
    public void testTripleSumModifyRelationRemoveEdgeNegative() throws Exception {
        network.addRelation(1, 2, 10);
        network.addRelation(2, 3, 20);
        network.addRelation(1, 3, 30); // Sum = 1
        assertEquals(1, network.queryTripleSum());

        network.modifyRelation(2, 3, -50);
        assertEquals("Removing edge (2,3) by making value negative", 0, network.queryTripleSum());
    }

    @Test
    public void testTripleSumModifyRelationRemoveEdgeNoTriplesInvolved() throws Exception {
        network.addRelation(1, 2, 10); // Edge 1
        network.addRelation(3, 4, 20); // Edge 2 (disconnected)
        network.addRelation(1, 5, 30); // Edge 3 (forms no triple)
        assertEquals(0, network.queryTripleSum());

        network.modifyRelation(3, 4, -20);
        assertEquals(0, network.queryTripleSum());

        network.modifyRelation(1, 2, -10);
        assertEquals(0, network.queryTripleSum());
    }

    @Test
    public void testTripleSumModifyRelationAcrossZeroBoundary() throws Exception {
        network.addRelation(1, 2, 10);
        network.addRelation(2, 3, 20);
        network.addRelation(1, 3, 30); // Sum = 1
        assertEquals(1, network.queryTripleSum());

        // Remove edge (1,2)
        network.modifyRelation(1, 2, -10); // Value 0
        assertEquals(0, network.queryTripleSum());

        network.addRelation(1, 2, 5);
        assertEquals(1, network.queryTripleSum());

        network.modifyRelation(1, 2, -5); // Value 0
        assertEquals(0, network.queryTripleSum());
    }

    // --- More Complex Graph Structures ---

    @Test
    public void testTripleSumLollipopGraph() throws Exception {

        network.addRelation(1, 2, 10);
        network.addRelation(2, 3, 20);
        network.addRelation(1, 3, 30); // Forms triple (1,2,3)
        assertEquals("K3 part forms 1 triple", 1, network.queryTripleSum());

        // Attach path
        network.addRelation(3, 4, 40);
        assertEquals("Attaching path start (3-4) doesn't change count", 1, network.queryTripleSum());
        network.addRelation(4, 5, 50);
        assertEquals("Attaching path end (4-5) doesn't change count", 1, network.queryTripleSum());

        network.addRelation(1, 4, 60);
        assertEquals("Adding (1,4) forms triple (1,3,4)", 2, network.queryTripleSum());

        network.modifyRelation(3, 4, -40);
        assertEquals("Removing bridge (3,4) breaks (1,3,4)", 1, network.queryTripleSum()); // Only (1,2,3) left

        network.modifyRelation(1, 2, -10);
        assertEquals("Removing K3 edge (1,2) breaks (1,2,3)", 0, network.queryTripleSum());
    }

    @Test
    public void testTripleSumTwoDisconnectedTriangles() throws Exception {
        // Triangle 1: (1,2,3)
        network.addRelation(1, 2, 10);
        network.addRelation(2, 3, 20);
        network.addRelation(1, 3, 30);
        assertEquals(1, network.queryTripleSum());

        // Triangle 2: (4,5,6)
        network.addRelation(4, 5, 40);
        network.addRelation(5, 6, 50);
        network.addRelation(4, 6, 60);
        assertEquals("Added second disjoint triangle", 2, network.queryTripleSum());

        // Bridge them: add edge (1,4)
        network.addRelation(1, 4, 70);

        assertEquals(2, network.queryTripleSum());

        network.addRelation(3, 4, 80);
        assertEquals(3, network.queryTripleSum());

        network.modifyRelation(1, 2, -100);
        assertEquals(2, network.queryTripleSum());

        network.modifyRelation(4, 5, -100);
        assertEquals(1, network.queryTripleSum());
    }

    // --- Orthogonality Tests (Operations that shouldn't affect triple sum) ---

    @Test
    public void testTripleSumUnaffectedByTagOperations() throws Exception {
        // Setup K4: sum = 4
        network.addRelation(1, 2, 10);
        network.addRelation(1, 3, 20);
        network.addRelation(2, 3, 30);
        network.addRelation(1, 4, 40);
        network.addRelation(2, 4, 50);
        network.addRelation(3, 4, 60);
        assertEquals(4, network.queryTripleSum());

        // Add tag
        TagInterface tag1 = new Tag(101); // Use the simple Tag implementation
        network.addTag(1, tag1);
        assertEquals(4, network.queryTripleSum());

        // Add person to tag
        network.addPersonToTag(2, 1, 101); // Add p2 to p1's tag 101
        assertEquals(4, network.queryTripleSum());

        // Query tag (pure operation)
        network.queryTagAgeVar(1, 101);
        assertEquals(4, network.queryTripleSum());

        // Delete person from tag
        network.delPersonFromTag(2, 1, 101);
        assertEquals(4, network.queryTripleSum());

        // Delete tag
        network.delTag(1, 101);
        assertEquals(4, network.queryTripleSum());
    }

    @Test
    public void testTripleSumUnaffectedByQueryBestAcquaintance() throws Exception {
        // Setup K4: sum = 4
        network.addRelation(1, 2, 10);
        network.addRelation(1, 3, 20);
        network.addRelation(2, 3, 30);
        network.addRelation(1, 4, 40);
        network.addRelation(2, 4, 50);
        network.addRelation(3, 4, 60);
        assertEquals(4, network.queryTripleSum());

        network.queryBestAcquaintance(1);
        assertEquals(4, network.queryTripleSum());

        network.queryBestAcquaintance(4);
        assertEquals(4, network.queryTripleSum());
    }

    @Test
    public void testTripleSumUnaffectedByQueryValue() throws Exception {
        // Setup K4: sum = 4
        network.addRelation(1, 2, 10);
        network.addRelation(1, 3, 20);
        network.addRelation(2, 3, 30);
        network.addRelation(1, 4, 40);
        network.addRelation(2, 4, 50);
        network.addRelation(3, 4, 60);
        assertEquals(4, network.queryTripleSum());

        network.queryValue(1, 2);
        assertEquals(4, network.queryTripleSum());

        network.queryValue(3, 4);
        assertEquals(4, network.queryTripleSum());
    }

    @Test
    public void testTripleSumUnaffectedByIsCircle() throws Exception {
        // Setup K4: sum = 4
        network.addRelation(1, 2, 10);
        network.addRelation(1, 3, 20);
        network.addRelation(2, 3, 30);
        network.addRelation(1, 4, 40);
        network.addRelation(2, 4, 50);
        network.addRelation(3, 4, 60);
        assertEquals(4, network.queryTripleSum());

        network.isCircle(1, 4);
        assertEquals(4, network.queryTripleSum());

        network.isCircle(1, 5); // Person 5 exists but is not connected
        assertEquals(4, network.queryTripleSum());
    }

    // --- Idempotency ---
    @Test
    public void testQueryTripleSumIdempotency() throws Exception {
        // Setup K4: sum = 4
        network.addRelation(1, 2, 10);
        network.addRelation(1, 3, 20);
        network.addRelation(2, 3, 30);
        network.addRelation(1, 4, 40);
        network.addRelation(2, 4, 50);
        network.addRelation(3, 4, 60);
        assertEquals(4, network.queryTripleSum());
        assertEquals(4, network.queryTripleSum());
        assertEquals(4, network.queryTripleSum());

        // Remove an edge
        network.modifyRelation(3, 4, -60);
        assertEquals(2, network.queryTripleSum());
        assertEquals(2, network.queryTripleSum());
        assertEquals(2, network.queryTripleSum());
    }

    @Test
    public void testTripleSumUnaffectedByIrrelevantOpsOnGeneralGraph() throws Exception {

        network.addRelation(1, 2, 10);
        network.addRelation(2, 3, 20);
        network.addRelation(1, 3, 30);
        assertEquals(1, network.queryTripleSum());

        network.addRelation(3, 4, 40);
        network.addRelation(4, 5, 50);
        network.addRelation(3, 5, 60);
        assertEquals(2, network.queryTripleSum());

        // Add another person - should not change sum
        Person p7 = new Person(7, "Grace", 50);
        network.addPerson(p7);
        assertEquals(2, network.queryTripleSum());

        // Add a relation involving the new person - still no triples involving p7
        network.addRelation(1, 7, 70);
        assertEquals(2, network.queryTripleSum());


        // Perform various query operations
        assertTrue(network.isCircle(1, 3));
        assertEquals(2, network.queryTripleSum());
        assertTrue(network.isCircle(1, 5));
        assertEquals(2, network.queryTripleSum());
        assertEquals(30, network.queryValue(1, 3));
        assertEquals(2, network.queryTripleSum());
        network.queryBestAcquaintance(3); // Should return 5 or 4 depending on tie-breaking
        assertEquals(2, network.queryTripleSum());

        // Perform Tag operations
        TagInterface tag2 = new Tag(202);
        network.addTag(3, tag2);
        assertEquals(2, network.queryTripleSum());
        network.addPersonToTag(4, 3, 202); // Add p4 to p3's tag
        assertEquals(2, network.queryTripleSum());
        network.queryTagAgeVar(3, 202);
        assertEquals(2, network.queryTripleSum());
        network.delPersonFromTag(4, 3, 202);
        assertEquals(2, network.queryTripleSum());
        network.delTag(3, 202);
        assertEquals(2, network.queryTripleSum());

        // Modify relation value without removing it
        network.modifyRelation(1, 2, 100); // Value becomes 110
        assertEquals(2, network.queryTripleSum());
        network.modifyRelation(4, 5, -10); // Value becomes 40
        assertEquals(2, network.queryTripleSum());

        // Final check
        assertEquals(2, network.queryTripleSum());

        // Now, modify to remove a link and check change
        network.modifyRelation(1, 3, -30); // Remove link (1,3), breaks triple (1,2,3)
        assertEquals(1, network.queryTripleSum());
    }
}