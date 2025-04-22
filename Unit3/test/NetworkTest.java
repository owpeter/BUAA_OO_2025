import com.oocourse.spec1.exceptions.*;
import com.oocourse.spec1.main.PersonInterface;
import com.oocourse.spec1.main.TagInterface; // Assuming you have a Tag implementation
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class NetworkTest {

    private Network network;
    private Person p1, p2, p3, p4, p5;
    @Before
    public void setUp() throws Exception {
        network = new Network();
        p1 = new Person(1, "Alice", 20);
        p2 = new Person(2, "Bob", 25);
        p3 = new Person(3, "Charlie", 30);
        p4 = new Person(4, "David", 35);
        p5 = new Person(5, "Eve", 40);

        // Add persons to the network
        network.addPerson(p1);
        network.addPerson(p2);
        network.addPerson(p3);
        network.addPerson(p4);
        network.addPerson(p5);
    }

    @Test
    public void testInitialTripleSum() {
        assertEquals(0, network.queryTripleSum());
    }

    @Test
    public void testTripleSumAfterFirstRelation() throws Exception {

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
    public void testTripleSumFormingFirstTriple() throws Exception {

        network.addRelation(1, 2, 100);
        assertEquals(0, network.queryTripleSum());
        network.addRelation(2, 3, 50);
        assertEquals(0, network.queryTripleSum());

        network.addRelation(1, 3, 75);
        assertEquals(1, network.queryTripleSum());
    }

    @Test
    public void testTripleSumFormingMultipleTriplesK4() throws Exception {

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
    public void testTripleSumAddRelationNoNewTriples() throws Exception {

        network.addRelation(1, 2, 10);
        network.addRelation(2, 3, 20);
        network.addRelation(1, 3, 30);
        assertEquals(1, network.queryTripleSum());

        network.addRelation(4, 5, 40);
        assertEquals(1, network.queryTripleSum());

        network.addRelation(1, 4, 50);
        assertEquals(1, network.queryTripleSum());
    }

    @Test
    public void testTripleSumModifyRelationIncreaseValue() throws Exception {

        network.addRelation(1, 2, 10);
        network.addRelation(2, 3, 20);
        network.addRelation(1, 3, 30);
        assertEquals(1, network.queryTripleSum());

        network.modifyRelation(1, 2, 5);
        assertEquals(1, network.queryTripleSum());

        network.modifyRelation(2, 3, 100);
        assertEquals(1, network.queryTripleSum());
    }

    @Test
    public void testTripleSumModifyRelationDecreaseValuePositive() throws Exception {

        network.addRelation(1, 2, 10);
        network.addRelation(2, 3, 20);
        network.addRelation(1, 3, 30);
        assertEquals(1, network.queryTripleSum());

        network.modifyRelation(1, 2, -5);
        assertEquals(1, network.queryTripleSum());
    }

    @Test
    public void testTripleSumModifyRelationRemoveEdgeFromTriple() throws Exception {

        network.addRelation(1, 2, 10);
        network.addRelation(2, 3, 20);
        network.addRelation(1, 3, 30);
        assertEquals(1, network.queryTripleSum());

        network.modifyRelation(1, 3, -30);
        assertEquals(0, network.queryTripleSum());

        network.addRelation(1, 3, 5);
        assertEquals(1, network.queryTripleSum());

        network.modifyRelation(1, 2, -15);
        assertEquals(0, network.queryTripleSum());
    }

    @Test
    public void testTripleSumModifyRelationRemoveEdgeFromMultipleTriplesK4() throws Exception {
        // Setup K4: sum = 4
        network.addRelation(1, 2, 10);
        network.addRelation(1, 3, 20);
        network.addRelation(2, 3, 30); // sum=1
        network.addRelation(1, 4, 40);
        network.addRelation(2, 4, 50); // sum=2
        network.addRelation(3, 4, 60); // sum=4
        assertEquals(4, network.queryTripleSum());

        network.modifyRelation(3, 4, -60);
        assertEquals(2, network.queryTripleSum());

        network.modifyRelation(1, 2, -100);
        assertEquals(0, network.queryTripleSum());

    }
    @Test
    public void testTripleSumModifyRelationRemoveEdgeNoTriples() throws Exception {
        // Setup: 1-2, 3-4. Sum = 0
        network.addRelation(1, 2, 10);
        network.addRelation(3, 4, 20);
        assertEquals(0, network.queryTripleSum());

        // Remove edge 1-2. It wasn't part of any triple. Common neighbors = 0. Sum change = 0.
        network.modifyRelation(1, 2, -10);
        assertEquals(0, network.queryTripleSum());

        // Remove edge 3-4. Also not part of any triple. Sum change = 0.
        network.modifyRelation(3, 4, -30);
        assertEquals(0, network.queryTripleSum());
    }

    @Test
    public void testTripleSumWithTagOperations() throws Exception {

        network.addRelation(1, 2, 10);
        network.addRelation(2, 3, 20);
        network.addRelation(1, 3, 30);
        assertEquals(1, network.queryTripleSum());

        // Add tag
        TagInterface tag1 = new Tag(101);
        network.addTag(1, tag1);
        assertEquals(1, network.queryTripleSum());

        network.addPersonToTag(2, 1, 101); // Add p2 to p1's tag 101
        assertEquals(1, network.queryTripleSum());

        network.queryTagAgeVar(1, 101);
        assertEquals(1, network.queryTripleSum());

        network.delPersonFromTag(2, 1, 101);
        assertEquals(1, network.queryTripleSum());

        network.delTag(1, 101);
        assertEquals(1, network.queryTripleSum());
    }

    @Test
    public void testComplexSequenceAddRemove() throws Exception {
        // Build K4: sum = 4
        network.addRelation(1, 2, 10);
        network.addRelation(1, 3, 20);
        network.addRelation(2, 3, 30); // sum=1
        network.addRelation(1, 4, 40);
        network.addRelation(2, 4, 50); // sum=2
        network.addRelation(3, 4, 60); // sum=4
        assertEquals(4, network.queryTripleSum());

        // Remove 3-4 (breaks 2 triples: (1,3,4), (2,3,4)). Common neighbors 1, 2. Sum change -2.
        network.modifyRelation(3, 4, -60);
        assertEquals(2, network.queryTripleSum()); // Remaining: (1,2,3), (1,2,4)

        network.modifyRelation(1, 2, -10);
        assertEquals(0, network.queryTripleSum()); // Remaining: none

        network.addRelation(1, 2, 5);
        assertEquals(2, network.queryTripleSum());

        network.addRelation(3, 4, 5);
        assertEquals(4, network.queryTripleSum()); // Triples: (1,3,4), (2,3,4)

        network.modifyRelation(2, 3, -30);
        assertEquals(2, network.queryTripleSum());
    }
}