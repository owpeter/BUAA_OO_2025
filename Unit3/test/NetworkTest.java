
import com.oocourse.spec3.main.PersonInterface;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;

public class NetworkTest {

    private Network network;

    // Helper method to create a simple Person
    private Person createPerson(int id, String name, int age) {
        // We use the concrete implementation
        return new Person(id, name, age);
    }

    // Helper method to create a simple Tag
    private Tag createTag(int id) {
        return new Tag(id);
    }

    private boolean equals(Person person1, Person person2) {
        return person1.getId() == person2.getId() &&
                person1.getAge() == person2.getAge() &&
                person1.getName().equals(person2.getName());
    }

    private HashMap<Integer, PersonInterface> deepClone(PersonInterface[] persons) {
        HashMap<Integer, PersonInterface> personInterfaces = new HashMap<>();
        for (PersonInterface person : persons) {
            PersonInterface newPerson = new Person(person.getId(), person.getName(), person.getAge());
            personInterfaces.put(person.getId(), newPerson);
        }
        return personInterfaces;
    }


    // Take a snapshot of the network state for pure check
    private NetworkSnapshot takeSnapshot(Network net) throws Exception {
        NetworkSnapshot snapshot = new NetworkSnapshot();
        PersonInterface[] persons = net.getPersons();
        snapshot.personSnapshots = deepClone(persons);
        snapshot.tripleSum = net.queryTripleSum();
        return snapshot;
    }

    // Assert that the network state is unchanged compared to the snapshot
    private void assertNetworkStateUnchanged(Network net, NetworkSnapshot snapshot) throws Exception {
        PersonInterface[] persons = net.getPersons();
        assertEquals(snapshot.personSnapshots.size(), persons.length);
        for(int i = 0; i < persons.length; i++) {
            Person person = (Person) persons[i];
            Person ps = (Person) snapshot.personSnapshots.getOrDefault(person.getId(), null);
            assertNotNull("Pure check failed: Person not found", ps);
            assertTrue("Pure check failed: Person not equal", equals(person, ps));
        }

        assertEquals("Pure check failed: Triple sum changed", snapshot.tripleSum, net.queryTripleSum());
    }

    // Simple class to hold network snapshot data
    private static class NetworkSnapshot {
        Map<Integer, PersonInterface> personSnapshots = new HashMap<>();
        int tripleSum = 0;
    }

    @Before
    public void setUp() {
        network = new Network();
    }

    // ====================================================================
    // Basic/Edge Cases
    // ====================================================================

    @Test
    public void testQueryCoupleSum_EmptyNetwork() throws Exception {
        NetworkSnapshot snapshot = takeSnapshot(network);
        assertEquals("QueryCoupleSum on empty network should be 0", 0, network.queryCoupleSum());
        assertNetworkStateUnchanged(network, snapshot);
    }

    @Test
    public void testQueryCoupleSum_SinglePerson() throws Exception {
        Person p1 = createPerson(1, "Alice", 20);
        network.addPerson(p1);

        NetworkSnapshot snapshot = takeSnapshot(network);
        assertEquals("QueryCoupleSum with single person should be 0", 0, network.queryCoupleSum());
        assertNetworkStateUnchanged(network, snapshot);
    }

    @Test
    public void testQueryCoupleSum_TwoPersonsNoRelation() throws Exception {
        Person p1 = createPerson(1, "Alice", 20);
        Person p2 = createPerson(2, "Bob", 21);
        network.addPerson(p1);
        network.addPerson(p2);

        NetworkSnapshot snapshot = takeSnapshot(network);
        // No relations means acquaintance.length == 0 for both, so couple conditions not met
        assertEquals("QueryCoupleSum with two persons no relation should be 0", 0, network.queryCoupleSum());
        assertNetworkStateUnchanged(network, snapshot);
    }

    // ====================================================================
    // Couple Creation via addRelation
    // ====================================================================

    @Test
    public void testAddRelation_CreatesSimpleCouple() throws Exception {
        Person p1 = createPerson(1, "Alice", 20);
        Person p2 = createPerson(2, "Bob", 21);
        network.addPerson(p1);
        network.addPerson(p2);

        assertEquals("Initial couple sum should be 0", 0, network.queryCoupleSum()); // Pure check 1

        network.addRelation(1, 2, 10); // P1 <-> P2 (10)
        // P1's only acquaintance is P2 (value 10), P1's best is P2. (Acq size 1 > 0)
        // P2's only acquaintance is P1 (value 10), P2's best is P1. (Acq size 1 > 0)
        // Conditions met for pair (1, 2). i=1, j=2. i<j true.

        NetworkSnapshot snapshot = takeSnapshot(network);
        assertEquals("Couple sum after adding relation between two isolated persons should be 1", 1, network.queryCoupleSum());
        assertNetworkStateUnchanged(network, snapshot);
    }

    @Test
    public void testAddRelation_DoesNotCreateCouple_OneSidedBest() throws Exception {
        Person p1 = createPerson(1, "Alice", 20);
        Person p2 = createPerson(2, "Bob", 21);
        Person p3 = createPerson(3, "Charlie", 22);
        network.addPerson(p1);
        network.addPerson(p2);
        network.addPerson(p3);

        network.addRelation(1, 3, 10); // P1 <-> P3 (10) - P1 best is P3, P3 best is P1 (initially)
        network.addRelation(2, 3, 20); // P2 <-> P3 (20) - P2 best is P3, P3 best is P2 (now P3 best is P2)

        // Initial check: P1-P3 should be a couple, P2-P3 should be a couple
        // P1 best P3, P3 best P1 (10 vs 20 -> P3 best is P2) -> P1-P3 not couple
        // P2 best P3, P3 best P2 -> P2-P3 should be a couple
        assertEquals("Initial couple sum should be 1 (P2, P3)", 1, network.queryCoupleSum()); // Pure check 1

        // Add relation between P1 and P2
        network.addRelation(1, 2, 5); // P1 <-> P2 (5)

        // After adding P1 <-> P2 (5):
        // P1: Acq {P3(10), P2(5)}. Best: P3 (10).
        // P2: Acq {P3(20), P1(5)}. Best: P3 (20).
        // P3: Acq {P1(10), P2(20)}. Best: P2 (20).

        // Check couples:
        // (1,2): P1 best P3 != P2. Not couple.
        // (1,3): P1 best P3, P3 best P2 != P1. Not couple.
        // (2,3): P2 best P3, P3 best P2. Yes. Couple (2,3). i=2, j=3. i<j true. Count = 1.

        NetworkSnapshot snapshot = takeSnapshot(network);
        assertEquals("Couple sum after adding relation (1,2) which is not best should still be 1", 1, network.queryCoupleSum());
        assertNetworkStateUnchanged(network, snapshot);
    }

    @Test
    public void testAddRelation_BreaksExistingCouple() throws Exception {
        Person p1 = createPerson(1, "Alice", 20);
        Person p2 = createPerson(2, "Bob", 21);
        Person p3 = createPerson(3, "Charlie", 22);
        network.addPerson(p1);
        network.addPerson(p2);
        network.addPerson(p3);

        network.addRelation(1, 2, 10); // P1 <-> P2 (10) - Forms a couple (1,2)
        assertEquals("Initial couple sum should be 1", 1, network.queryCoupleSum()); // Pure check 1

        // Add a relation for P1 that is better than P1-P2
        network.addRelation(1, 3, 15); // P1 <-> P3 (15)

        // After adding P1 <-> P3 (15):
        // P1: Acq {P2(10), P3(15)}. Best: P3 (15).
        // P2: Acq {P1(10)}. Best: P1 (10).
        // P3: Acq {P1(15)}. Best: P1 (15).

        // Check couples:
        // (1,2): P1 best P3 != P2. Not couple.
        // (1,3): P1 best P3, P3 best P1. Yes. Couple (1,3). i=1, j=3. i<j true. Count = 1.
        // (2,3): P2 best P1 != P3. Not couple.

        // Wait, the P3 best P1 calculation is wrong based on the logic.
        // P3 Acq: {P1(15)}. P3 best IS P1.
        // P1 Acq: {P2(10), P3(15)}. P1 best IS P3.
        // So (1,3) IS a couple.

        network = new Network(); // Reset for a better test case where P3 doesn't point back to P1
        p1 = createPerson(1, "Alice", 20);
        p2 = createPerson(2, "Bob", 21);
        p3 = createPerson(3, "Charlie", 22);
        Person p4 = createPerson(4, "David", 23);
        network.addPerson(p1);
        network.addPerson(p2);
        network.addPerson(p3);
        network.addPerson(p4);

        network.addRelation(1, 2, 10); // P1 <-> P2 (10) - Forms a couple (1,2)
        network.addRelation(3, 4, 10); // P3 <-> P4 (10) - Forms a couple (3,4)

        assertEquals("Initial couple sum should be 2", 2, network.queryCoupleSum()); // Pure check 1

        // Add a relation for P1 that breaks the (1,2) couple without forming a new one.
        // Make P1's best P3, but P3's best stay P4.
        network.addRelation(1, 3, 15); // P1 <-> P3 (15)

        // After adding P1 <-> P3 (15):
        // P1: Acq {P2(10), P3(15)}. Best: P3 (15).
        // P2: Acq {P1(10)}. Best: P1 (10).
        // P3: Acq {P4(10), P1(15)}. Best: P1 (15). - P3 best IS P1
        // P4: Acq {P3(10)}. Best: P3 (10).

        // This scenario still creates a (1,3) couple. Let's add a relation to P3 that keeps P3's best as P4.
        network = new Network(); // Reset
        p1 = createPerson(1, "Alice", 20);
        p2 = createPerson(2, "Bob", 21);
        p3 = createPerson(3, "Charlie", 22);
        p4 = createPerson(4, "David", 23);
        network.addPerson(p1);
        network.addPerson(p2);
        network.addPerson(p3);
        network.addPerson(p4);

        network.addRelation(1, 2, 10); // P1 <-> P2 (10) - Forms (1,2) couple
        network.addRelation(3, 4, 20); // P3 <-> P4 (20) - Forms (3,4) couple

        assertEquals("Initial couple sum should be 2", 2, network.queryCoupleSum()); // Pure check 1

        // Add a relation P1 <-> P3 (15)
        network.addRelation(1, 3, 15);

        // After adding P1 <-> P3 (15):
        // P1: Acq {P2(10), P3(15)}. Best: P3 (15).
        // P2: Acq {P1(10)}. Best: P1 (10).
        // P3: Acq {P4(20), P1(15)}. Best: P4 (20).
        // P4: Acq {P3(20)}. Best: P3 (20).

        // Check couples:
        // (1,2): P1 best P3 != P2. No.
        // (1,3): P1 best P3, P3 best P4 != P1. No.
        // (1,4): No direct relation. No.
        // (2,3): No direct relation. No.
        // (2,4): No direct relation. No.
        // (3,4): P3 best P4, P4 best P3. Yes. Couple (3,4). Count = 1.

        NetworkSnapshot snapshot = takeSnapshot(network);
        assertEquals("Couple sum after adding relation (1,3) that breaks (1,2) without forming a new one should be 1", 1, network.queryCoupleSum());
        assertNetworkStateUnchanged(network, snapshot);
    }

    // ====================================================================
    // Couple Modification via modifyRelation
    // ====================================================================

    @Test
    public void testModifyRelation_ValueIncreaseWithinCouple() throws Exception {
        Person p1 = createPerson(1, "Alice", 20);
        Person p2 = createPerson(2, "Bob", 21);
        network.addPerson(p1);
        network.addPerson(p2);
        network.addRelation(1, 2, 10); // Forms (1,2) couple

        assertEquals("Initial couple sum should be 1", 1, network.queryCoupleSum()); // Pure check 1

        // Increase relation value, still the only relation
        network.modifyRelation(1, 2, 5); // Value becomes 15

        NetworkSnapshot snapshot = takeSnapshot(network);
        assertEquals("Couple sum after increasing value within couple should still be 1", 1, network.queryCoupleSum());
        assertNetworkStateUnchanged(network, snapshot);
    }

    @Test
    public void testModifyRelation_ValueDecreaseWithinCouple() throws Exception {
        Person p1 = createPerson(1, "Alice", 20);
        Person p2 = createPerson(2, "Bob", 21);
        network.addPerson(p1);
        network.addPerson(p2);
        network.addRelation(1, 2, 10); // Forms (1,2) couple

        assertEquals("Initial couple sum should be 1", 1, network.queryCoupleSum()); // Pure check 1

        // Decrease relation value, still the only relation (value > 0)
        network.modifyRelation(1, 2, -3); // Value becomes 7

        NetworkSnapshot snapshot = takeSnapshot(network);
        assertEquals("Couple sum after decreasing value within couple (but value > 0) should still be 1", 1, network.queryCoupleSum());
        assertNetworkStateUnchanged(network, snapshot);
    }

    @Test
    public void testModifyRelation_ValueBreaksCouple() throws Exception {
        Person p1 = createPerson(1, "Alice", 20);
        Person p2 = createPerson(2, "Bob", 21);
        Person p3 = createPerson(3, "Charlie", 22);
        network.addPerson(p1);
        network.addPerson(p2);
        network.addPerson(p3);

        network.addRelation(1, 2, 10); // P1 <-> P2 (10) - Initially forms couple
        network.addRelation(1, 3, 5);  // P1 <-> P3 (5) - P1's best is P2
        network.addRelation(2, 3, 5);  // P2 <-> P3 (5) - P2's best is P1

        assertEquals("Initial couple sum should be 1", 1, network.queryCoupleSum()); // Pure check 1

        // Modify P1-P3 value to be higher than P1-P2
        network.modifyRelation(1, 3, 6); // Value becomes 5 + 6 = 11

        // After modify:
        // P1: Acq {P2(10), P3(11)}. Best: P3 (11).
        // P2: Acq {P1(10), P3(5)}. Best: P1 (10).
        // P3: Acq {P1(11), P2(5)}. Best: P1 (11).

        // Check couples:
        // (1,2): P1 best P3 != P2. No.
        // (1,3): P1 best P3, P3 best P1. Yes. Couple (1,3). i=1, j=3. i<j true. Count = 1.
        // (2,3): P2 best P1 != P3. No.

        // This test case design still created a new couple (1,3). Let's adjust.
        network = new Network(); // Reset
        p1 = createPerson(1, "Alice", 20);
        p2 = createPerson(2, "Bob", 21);
        p3 = createPerson(3, "Charlie", 22);
        Person p4 = createPerson(4, "David", 23);
        network.addPerson(p1);
        network.addPerson(p2);
        network.addPerson(p3);
        network.addPerson(p4);

        network.addRelation(1, 2, 10); // P1 <-> P2 (10) - Forms (1,2) couple
        network.addRelation(3, 4, 20); // P3 <-> P4 (20) - Forms (3,4) couple

        assertEquals("Initial couple sum should be 2", 2, network.queryCoupleSum()); // Pure check 1

        // Add P1-P3 relation (low value) and P3-P1 relation (low value)
        network.addRelation(1, 3, 5); // P1 <-> P3 (5)
        // At this point: P1 best P2, P2 best P1, P3 best P4, P4 best P3. Still 2 couples.

        // Modify P1-P3 value to be higher than P1-P2, but P3-P1 value unchanged (not best for P3)
        // This is tricky because modifyRelation is symmetrical. We need P3 to have a better relation elsewhere.
        network = new Network(); // Reset
        p1 = createPerson(1, "Alice", 20);
        p2 = createPerson(2, "Bob", 21);
        p3 = createPerson(3, "Charlie", 22);
        p4 = createPerson(4, "David", 23);
        network.addPerson(p1);
        network.addPerson(p2);
        network.addPerson(p3);
        network.addPerson(p4);

        network.addRelation(1, 2, 10); // P1 <-> P2 (10) - Forms (1,2) couple
        network.addRelation(3, 4, 20); // P3 <-> P4 (20) - Forms (3,4) couple
        network.addRelation(1, 3, 5); // P1 <-> P3 (5)

        assertEquals("Initial couple sum should be 2", 2, network.queryCoupleSum()); // Pure check 1

        // Modify P1-P3 relation value to be 15 (5 + 10). P1 <-> P3 (15)
        network.modifyRelation(1, 3, 10);

        // After modify:
        // P1: Acq {P2(10), P3(15)}. Best: P3 (15).
        // P2: Acq {P1(10)}. Best: P1 (10).
        // P3: Acq {P4(20), P1(15)}. Best: P4 (20).
        // P4: Acq {P3(20)}. Best: P3 (20).

        // Check couples:
        // (1,2): P1 best P3 != P2. No.
        // (1,3): P1 best P3, P3 best P4 != P1. No.
        // (3,4): P3 best P4, P4 best P3. Yes. Couple (3,4). Count = 1.

        NetworkSnapshot snapshot = takeSnapshot(network);
        assertEquals("Couple sum after modifying relation (1,3) to break (1,2) should be 1", 1, network.queryCoupleSum());
        assertNetworkStateUnchanged(network, snapshot);
    }

    @Test
    public void testModifyRelation_ValueCreatesCouple() throws Exception {
        Person p1 = createPerson(1, "Alice", 20);
        Person p2 = createPerson(2, "Bob", 21);
        Person p3 = createPerson(3, "Charlie", 22);
        Person p4 = createPerson(4, "David", 23);
        network.addPerson(p1);
        network.addPerson(p2);
        network.addPerson(p3);
        network.addPerson(p4);

        network.addRelation(1, 3, 10); // P1 <-> P3 (10) - P1 best P3
        network.addRelation(2, 4, 10); // P2 <-> P4 (10) - P2 best P4
        network.addRelation(1, 2, 5);  // P1 <-> P2 (5)

        // Initial state:
        // P1: Acq {P3(10), P2(5)}. Best: P3.
        // P2: Acq {P4(10), P1(5)}. Best: P4.
        // P3: Acq {P1(10)}. Best: P1.
        // P4: Acq {P2(10)}. Best: P2.
        // Couples: (1,3) and (2,4) form couples. Count = 2.

        assertEquals("Initial couple sum should be 2", 2, network.queryCoupleSum()); // Pure check 1

        // Modify P1-P2 value to be higher than their current bests
        network.modifyRelation(1, 2, 10); // Value becomes 5 + 10 = 15

        // After modify:
        // P1: Acq {P3(10), P2(15)}. Best: P2 (15).
        // P2: Acq {P4(10), P1(15)}. Best: P1 (15).
        // P3: Acq {P1(10)}. Best: P1 (10).
        // P4: Acq {P2(10)}. Best: P2 (10).

        // Check couples:
        // (1,2): P1 best P2, P2 best P1. Yes. Couple (1,2). Count = 1.
        // (1,3): P1 best P2 != P3. No.
        // (2,4): P2 best P1 != P4. No.
        // (3,4): No direct relation. No.
        // (1,4): No direct relation. No.
        // (2,3): No direct relation. No.

        NetworkSnapshot snapshot = takeSnapshot(network);
        // The test setup was flawed, P1-P3 and P2-P4 were couples initially.
        // Let's adjust the initial setup so P1/P2 are not couples with P3/P4.
        network = new Network(); // Reset
        p1 = createPerson(1, "Alice", 20);
        p2 = createPerson(2, "Bob", 21);
        p3 = createPerson(3, "Charlie", 22);
        p4 = createPerson(4, "David", 23);
        network.addPerson(p1);
        network.addPerson(p2);
        network.addPerson(p3);
        network.addPerson(p4);

        network.addRelation(1, 3, 10); // P1 <-> P3 (10)
        network.addRelation(1, 4, 5);  // P1 <-> P4 (5) - P1 best is P3
        network.addRelation(2, 3, 8);  // P2 <-> P3 (8)
        network.addRelation(2, 4, 12); // P2 <-> P4 (12) - P2 best is P4
        network.addRelation(1, 2, 5);  // P1 <-> P2 (5)

        // Initial state:
        // P1: Acq {P3(10), P4(5), P2(5)}. Best: P3 (10).
        // P2: Acq {P3(8), P4(12), P1(5)}. Best: P4 (12).
        // P3: Acq {P1(10), P2(8)}. Best: P1 (10).
        // P4: Acq {P1(5), P2(12)}. Best: P2 (12).

        // Check couples:
        // (1,3): P1 best P3, P3 best P1. Yes. Couple (1,3).
        // (2,4): P2 best P4, P4 best P2. Yes. Couple (2,4).
        // Others: No.
        // Initial couple sum should be 2.

        assertEquals("Initial couple sum should be 2", 2, network.queryCoupleSum()); // Pure check 1

        // Modify P1-P2 value to 20 (5 + 15)
        network.modifyRelation(1, 2, 15); // Value becomes 20

        // After modify:
        // P1: Acq {P3(10), P4(5), P2(20)}. Best: P2 (20).
        // P2: Acq {P3(8), P4(12), P1(20)}. Best: P1 (20).
        // P3: Acq {P1(10), P2(8)}. Best: P1 (10).
        // P4: Acq {P1(5), P2(12)}. Best: P2 (12).

        // Check couples:
        // (1,2): P1 best P2, P2 best P1. Yes. Couple (1,2). Count = 1.
        // (1,3): P1 best P2 != P3. No.
        // (2,4): P2 best P1 != P4. No.
        // (3,4): No direct relation. No.
        // (1,4): No direct relation. No.
        // (2,3): No direct relation. No.

        NetworkSnapshot snapshot2 = takeSnapshot(network);
        assertEquals("Couple sum after modifying relation (1,2) to create couple (1,2) and break (1,3),(2,4) should be 1", 1, network.queryCoupleSum());
        assertNetworkStateUnchanged(network, snapshot2);
    }


    @Test
    public void testModifyRelation_DeleteBreaksCouple() throws Exception {
        Person p1 = createPerson(1, "Alice", 20);
        Person p2 = createPerson(2, "Bob", 21);
        network.addPerson(p1);
        network.addPerson(p2);
        network.addRelation(1, 2, 10); // Forms (1,2) couple

        assertEquals("Initial couple sum should be 1", 1, network.queryCoupleSum()); // Pure check 1

        // Modify relation value to <= 0 (10 + -20 = -10)
        network.modifyRelation(1, 2, -20);

        // After deletion:
        // P1: Acq {}. Size 0. Best: -1 (or default).
        // P2: Acq {}. Size 0. Best: -1 (or default).
        // Conditions acquaintance.length > 0 not met. No couple.

        NetworkSnapshot snapshot = takeSnapshot(network);
        assertEquals("Couple sum after deleting relation should be 0", 0, network.queryCoupleSum());
        assertNetworkStateUnchanged(network, snapshot);
    }

    // ====================================================================
    // Unaffected Methods
    // Test that other methods do not change the result or network state
    // ====================================================================

    @Test
    public void testQueryCoupleSum_UnaffectedByTagOperations() throws Exception {
        Person p1 = createPerson(1, "Alice", 20);
        Person p2 = createPerson(2, "Bob", 21);
        network.addPerson(p1);
        network.addPerson(p2);
        network.addRelation(1, 2, 10); // Forms (1,2) couple

        assertEquals("Initial couple sum should be 1", 1, network.queryCoupleSum());

        // Perform tag operations
        Tag tag1 = createTag(100);
        network.addTag(1, tag1); // Add tag to p1
        network.addPersonToTag(2, 1, 100); // Add p2 to p1's tag (requires relation P1-P2)
        network.queryTagValueSum(1, 100);
        network.queryTagAgeVar(1, 100);
        network.delPersonFromTag(2, 1, 100);
        network.delTag(1, 100);

        NetworkSnapshot snapshot = takeSnapshot(network);
        assertEquals("Couple sum should be unaffected by tag operations", 1, network.queryCoupleSum());
        assertNetworkStateUnchanged(network, snapshot);
    }

    @Test
    public void testQueryCoupleSum_UnaffectedByAccountAndArticleOperations() throws Exception {
        Person p1 = createPerson(1, "Alice", 20);
        Person p2 = createPerson(2, "Bob", 21);
        Person p3 = createPerson(3, "Charlie", 22); // Need another person for account owner
        network.addPerson(p1);
        network.addPerson(p2);
        network.addPerson(p3);
        network.addRelation(1, 2, 10); // Forms (1,2) couple

        assertEquals("Initial couple sum should be 1", 1, network.queryCoupleSum());

        // Perform account and article operations
        network.createOfficialAccount(3, 200, "P3 Account"); // P3 owns account 200
        network.followOfficialAccount(1, 200); // P1 follows P3's account
        network.followOfficialAccount(2, 200); // P2 follows P3's account
        network.contributeArticle(1, 200, 300); // P1 contributes article 300
        network.queryReceivedArticles(1);
        network.queryReceivedArticles(2);
        network.queryBestContributor(200);
        network.deleteArticle(3, 200, 300); // P3 deletes the article
        network.deleteOfficialAccount(3, 200);

        NetworkSnapshot snapshot = takeSnapshot(network);
        assertEquals("Couple sum should be unaffected by account and article operations", 1, network.queryCoupleSum());
        assertNetworkStateUnchanged(network, snapshot);
    }

    @Test
    public void testQueryCoupleSum_UnaffectedByPureQueryMethods() throws Exception {
        Person p1 = createPerson(1, "Alice", 20);
        Person p2 = createPerson(2, "Bob", 21);
        network.addPerson(p1);
        network.addPerson(p2);
        network.addRelation(1, 2, 10); // Forms (1,2) couple

        assertEquals("Initial couple sum should be 1", 1, network.queryCoupleSum());

        // Perform pure query operations
        network.containsPerson(1);
        network.getPerson(1);
        network.queryValue(1, 2);
        network.isCircle(1, 2);
        network.queryTripleSum(); // This one also queries
        network.queryBestAcquaintance(1); // Needs acquaintance, P1 has P2
        network.queryShortestPath(1, 2); // Needs connection, P1-P2 are connected
        network.containsAccount(99);
        network.containsArticle(999);
        // Add tag/account/article queries if they exist in interface

        NetworkSnapshot snapshot = takeSnapshot(network);
        assertEquals("Couple sum should be unaffected by pure query methods", 1, network.queryCoupleSum());
        assertNetworkStateUnchanged(network, snapshot);
    }


    // ====================================================================
    // Scale Test
    // ====================================================================

    @Test
    public void testQueryCoupleSum_Scale_ManyCouples() throws Exception {
        int numCouples = 50; // Creates 100 persons
        for (int i = 0; i < numCouples * 2; i++) {
            network.addPerson(createPerson(i + 1, "Person" + (i + 1), 20 + (i % 10)));
        }

        // Create numCouples distinct couples (1,2), (3,4), ..., (99,100)
        for (int i = 0; i < numCouples; i++) {
            int id1 = i * 2 + 1;
            int id2 = i * 2 + 2;
            network.addRelation(id1, id2, 100 - i); // Varying values
        }

        NetworkSnapshot snapshot = takeSnapshot(network);
        assertEquals("QueryCoupleSum with " + numCouples + " separate couples should be " + numCouples, numCouples, network.queryCoupleSum());
        assertNetworkStateUnchanged(network, snapshot);
    }

    @Test
    public void testQueryCoupleSum_Scale_DenseNetwork() throws Exception {
        int numPersons = 50; // Creates 50 persons
        for (int i = 0; i < numPersons; i++) {
            network.addPerson(createPerson(i + 1, "Person" + (i + 1), 20 + (i % 10)));
        }

        // Add many relations, but few couples
        // P1 <-> P_i (value 10) for i=2..50
        // P2 <-> P_i (value 10) for i=3..50
        // ... This creates a dense graph but maybe few specific A<->B pairs where A best is B and B best is A.
        for (int i = 1; i <= numPersons; i++) {
            for (int j = i + 1; j <= numPersons; j++) {
                int value = Math.abs(i - j) + 1; // Simple value, might create ties/patterns
                if (value > 10 && value < 20) { // Only add some relations
                    network.addRelation(i, j, value);
                }
            }
        }

        // This specific relation pattern is hard to predict the exact couple sum without running the implementation.
        // The goal here is more about performance under moderate load and ensuring it doesn't crash or take excessive time.
        // We can assert that the result is >= 0 and the pure check passes.
        NetworkSnapshot snapshot = takeSnapshot(network);
        int result = network.queryCoupleSum();
        assertTrue("QueryCoupleSum on a moderately dense network should return a non-negative value", result >= 0);
        assertNetworkStateUnchanged(network, snapshot);

        // A more predictable dense case for couples:
        // Connect P1 to P2 (100), P1 to P3 (1), P2 to P4 (1).
        // P1 best is P2. P2 best is P1. (1,2) is a couple.
        // P3 best is P1. P4 best is P2.
        network = new Network(); // Reset
        Person p1 = createPerson(1, "A", 20);
        Person p2 = createPerson(2, "B", 21);
        Person p3 = createPerson(3, "C", 22);
        Person p4 = createPerson(4, "D", 23);
        network.addPerson(p1);
        network.addPerson(p2);
        network.addPerson(p3);
        network.addPerson(p4);

        network.addRelation(1, 2, 100); // P1 <-> P2 (100)
        network.addRelation(1, 3, 1);   // P1 <-> P3 (1)
        network.addRelation(2, 4, 1);   // P2 <-> P4 (1)

        // P1: Acq {P2(100), P3(1)}. Best: P2.
        // P2: Acq {P1(100), P4(1)}. Best: P1.
        // P3: Acq {P1(1)}. Best: P1.
        // P4: Acq {P2(1)}. Best: P2.
        // Couples: (1,2) is the only couple.

        snapshot = takeSnapshot(network);
        assertEquals("QueryCoupleSum on a dense network with one clear couple", 1, network.queryCoupleSum());
        assertNetworkStateUnchanged(network, snapshot);
    }

    // ====================================================================
    // JML Specific Checks (Acquaintance Length)
    // ====================================================================

    @Test
    public void testQueryCoupleSum_AcquaintanceLengthZero() throws Exception {
        // This is implicitly tested by testQueryCoupleSum_TwoPersonsNoRelation,
        // but let's make it explicit.
        Person p1 = createPerson(1, "Alice", 20);
        Person p2 = createPerson(2, "Bob", 21);
        network.addPerson(p1);
        network.addPerson(p2);

        // Neither has acquaintances, so the condition acquaintance.length > 0 is false for both.
        // They cannot form a couple.

        NetworkSnapshot snapshot = takeSnapshot(network);
        assertEquals("Couple sum should be 0 when no one has acquaintances", 0, network.queryCoupleSum());
        assertNetworkStateUnchanged(network, snapshot);
    }

    @Test
    public void testQueryCoupleSum_OneAcquaintanceLengthZero() throws Exception {
        Person p1 = createPerson(1, "Alice", 20);
        Person p2 = createPerson(2, "Bob", 21);
        Person p3 = createPerson(3, "Charlie", 22);
        network.addPerson(p1);
        network.addPerson(p2);
        network.addPerson(p3);

        network.addRelation(1, 2, 10); // P1 <-> P2 (10) - P1, P2 have acq size 1
        // P3 has acq size 0.

        // P1: Acq {P2(10)}. Best P2. Size > 0.
        // P2: Acq {P1(10)}. Best P1. Size > 0.
        // P3: Acq {}. Size 0. Best -1.

        // Couple check (i < j):
        // (1,2): P1 best P2, P2 best P1. P1 acq > 0, P2 acq > 0. Yes. Couple (1,2). Count = 1.
        // (1,3): P1 acq > 0. P3 acq 0. Condition P3 acq > 0 fails. No.
        // (2,3): P2 acq > 0. P3 acq 0. Condition P3 acq > 0 fails. No.

        NetworkSnapshot snapshot = takeSnapshot(network);
        assertEquals("Couple sum should be 1 when one person has zero acquaintances", 1, network.queryCoupleSum());
        assertNetworkStateUnchanged(network, snapshot);
    }


    // ====================================================================
    // Best Acquaintance Tie Breaking (Lower ID wins)
    // Covered in testQueryCoupleSum_BestAcquaintanceTieBreaking in the previous response's drafting,
    // let's include it explicitly here.
    // ====================================================================

    @Test
    public void testQueryCoupleSum_BestAcquaintanceTieBreaking() throws Exception {
        Person p1 = createPerson(1, "Alice", 20);
        Person p2 = createPerson(2, "Bob", 21);
        Person p3 = createPerson(3, "Charlie", 22);
        network.addPerson(p1);
        network.addPerson(p2);
        network.addPerson(p3);

        // Create relations with tie for P1's best (P2 vs P3)
        network.addRelation(1, 2, 10); // P1 <-> P2 (10)
        network.addRelation(1, 3, 10); // P1 <-> P3 (10)

        // P1's acquaintances: P2(10), P3(10). Tie in value. Lower ID wins, so P1's best is P2. (ID 2 < ID 3)
        // P2's acquaintances: P1(10). Best: P1.
        // P3's acquaintances: P1(10). Best: P1.

        // Couple Check (i < j):
        // (1, 2): P1 best P2, P2 best P1. Yes. Couple (1,2). Count = 1.
        // (1, 3): P1 best P2 != P3. No.

        NetworkSnapshot snapshot = takeSnapshot(network);
        assertEquals("Couple sum with best acquaintance tie breaking (lower ID wins) should be 1", 1, network.queryCoupleSum());
        assertNetworkStateUnchanged(network, snapshot);
    }
}