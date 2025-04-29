import com.oocourse.spec2.main.PersonInterface;
import com.oocourse.spec2.main.OfficialAccountInterface;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OfficialAccount implements OfficialAccountInterface {

    private final int ownerId;
    private final int id;
    private final String name;

    // Maps personId to PersonInterface object for quick lookup O(1) avg
    private final Map<Integer, PersonInterface> followersMap;

    // Maps personId to their contribution count for quick lookup O(1) avg
    // Keys are the same as followersMap
    private final Map<Integer, Integer> followerContributions;

    // Set of article IDs for quick containment check, add, and remove O(1) avg
    private final Set<Integer> articles;

    // Fields to maintain the best contributor dynamically for O(1) access
    private int bestContributorId;
    private int maxContribution; // Initialize to -1, as contributions are non-negative (>= 0)

    /**
     * Constructor for OfficialAccount.
     *
     * @param id      The ID of the official account.
     * @param name    The name of the official account.
     * @param ownerId The ID of the owner of the official account.
     */
    public OfficialAccount(int id, String name, int ownerId) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.followersMap = new HashMap<>();
        this.followerContributions = new HashMap<>();
        this.articles = new HashSet<>();

        // Initialize dynamic best contributor state
        // JML min over empty set is Integer.MAX_VALUE
        this.bestContributorId = Integer.MAX_VALUE;
        // Contributions are >= 0. -1 ensures any follower with 0 contribution
        // becomes the initial max candidate.
        this.maxContribution = -1;
    }

    /**
     * Gets the owner ID of the official account.
     *
     * @return The owner ID.
     */
    @Override
    public int getOwnerId() {
        return ownerId; // O(1)
    }

    /**
     * Adds a follower to the official account.
     * Requires: person is not already a follower.
     * Ensures: person is now a follower with a contribution count of 0.
     *
     * @param person The person to add as a follower.
     */
    @Override
    public void addFollower(/*@ non_null @*/PersonInterface person) {
        // JML requires !containsFollower(person) is met by caller
        int personId = person.getId();

        followersMap.put(personId, person); // O(1) avg
        followerContributions.put(personId, 0); // O(1) avg

        // Update dynamic best contributor state
        // New follower has 0 contribution.
        int newContrib = 0;
        if (newContrib > maxContribution) {
            // This new follower is the first, or has contribution 0 which is
            // greater than the initial maxContribution of -1.
            maxContribution = newContrib;
            bestContributorId = personId;
        } else if (newContrib == maxContribution) {
            // New follower ties the current max (which must be 0 if we reach here).
            // Update bestContributorId if this person has a smaller ID.
            bestContributorId = Math.min(bestContributorId, personId);
        }
        // If newContrib < maxContribution (i.e., maxContribution > 0),
        // adding a 0-contributor doesn't affect the best.
    }

    /**
     * Checks if a person is a follower of the official account.
     *
     * @param person The person to check.
     * @return True if the person is a follower, false otherwise.
     */
    @Override
    public boolean containsFollower(PersonInterface person) {
        return followersMap.containsKey(person.getId()); // O(1) avg
    }

    /**
     * Adds an article ID to the official account's list of articles
     * and increments the contributor's count.
     * Requires: article ID is not already present.
     * Ensures: article ID is present, and the person's contribution count is incremented.
     * Note: JML implies person must be a follower, checked by caller or surrounding logic.
     *
     * @param person The person who contributed the article (must be a follower).
     * @param id     The ID of the article.
     */
    @Override
    public void addArticle(/*@ non_null @*/PersonInterface person, int id) {
        // JML requires !containsArticle(id) is met by caller
        articles.add(id); // O(1) avg

        int personId = person.getId();

        // Retrieve old contribution before updating
        int oldContrib = followerContributions.getOrDefault(personId, 0); // JML implies person is follower, get will not return null if so.
        int newContrib = oldContrib + 1;

        // Update contribution in the map
        followerContributions.put(personId, newContrib); // O(1) avg

        // Update dynamic best contributor state
        if (newContrib > maxContribution) {
            // Found a new strictly higher maximum contribution
            maxContribution = newContrib;
            bestContributorId = personId; // This person is now the unique best
        } else if (newContrib == maxContribution) {
            // This person now ties the current maximum contribution
            // Update bestContributorId if this person's ID is smaller
            bestContributorId = Math.min(bestContributorId, personId);
        }
        // If newContrib < maxContribution, their increase doesn't affect the max,
        // so the best contributor state remains unchanged.
    }

    /**
     * Checks if an article with the given ID exists in the official account.
     *
     * @param id The article ID to check.
     * @return True if the article exists, false otherwise.
     */
    @Override
    public boolean containsArticle(int id) {
        return articles.contains(id); // O(1) avg
    }

    /**
     * Removes an article with the given ID from the official account.
     * Requires: article ID is present.
     * Ensures: article ID is not present.
     *
     * @param id The article ID to remove.
     */
    @Override
    public void removeArticle(int id) {
        // JML requires containsArticle(id) is met by caller
        articles.remove(id); // O(1) avg

        // According to the provided JML, removing an article does NOT affect
        // the contribution counts. Therefore, it does not affect the best contributor state.
    }

    /**
     * Gets the ID of the follower with the highest contribution count.
     * If multiple followers have the same highest count, returns the minimum ID among them.
     * JML: (\result == (\min int bestId;
     *                         (\exists int i; 0 <= i && i < followers.length; followers[i].id == bestId &&
     *                             (\forall int j; 0 <= j && j < followers.length; contributions[i] >= contributions[j]));
     *                         bestId));
     * Note: Returns Integer.MAX_VALUE if there are no followers, according to JML convention
     * for min over an empty set.
     *
     * @return The ID of the best contributor, or Integer.MAX_VALUE if there are no followers.
     */
    @Override
    public int getBestContributor() {
        // Check if there are any followers. If not, the JML min over an empty set
        // result is Integer.MAX_VALUE.
        if (followersMap.isEmpty()) {
            return Integer.MAX_VALUE; // O(1)
        }
        // Otherwise, return the dynamically maintained best contributor ID.
        return bestContributorId; // O(1)
    }

    // equals and hashCode based on ID are good practice if needed
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OfficialAccount that = (OfficialAccount) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}