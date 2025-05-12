import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.OfficialAccountInterface;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OfficialAccount implements OfficialAccountInterface {

    private final int ownerId;
    private final int id;
    private final String name;

    private final Map<Integer, PersonInterface> followersMap;
    private final Map<Integer, Integer> followerContributions;
    private final Set<Integer> articles;

    private int bestContributorId;
    private int maxContribution;

    public OfficialAccount(int ownerId, int id, String name) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.followersMap = new HashMap<>();
        this.followerContributions = new HashMap<>();
        this.articles = new HashSet<>();
        this.bestContributorId = Integer.MAX_VALUE;
        this.maxContribution = -1;
    }

    @Override
    public int getOwnerId() {
        return ownerId;
    }

    @Override
    public void addFollower(PersonInterface person) {
        int personId = person.getId();

        followersMap.put(personId, person);
        followerContributions.put(personId, 0);

        // update bestContribute
        int newContrib = 0;
        if (newContrib > maxContribution) {
            maxContribution = newContrib;
            bestContributorId = personId;
        } else if (newContrib == maxContribution) {
            bestContributorId = Math.min(bestContributorId, personId);
        }
    }

    @Override
    public boolean containsFollower(PersonInterface person) {
        return followersMap.containsKey(person.getId());
    }

    @Override
    public void addArticle(PersonInterface person, int id) {
        articles.add(id);
    }

    @Override
    public boolean containsArticle(int id) {
        return articles.contains(id);
    }

    @Override
    public void removeArticle(int id) {
        articles.remove(id);
    }

    @Override
    public int getBestContributor() {

        if (followersMap.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        return bestContributorId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OfficialAccount that = (OfficialAccount) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    // --- Helper methods for contribution management called by Network ---
    public void incrementContributor(int personId) {
        int oldContrib = followerContributions.getOrDefault(personId, 0);
        int newContrib = oldContrib + 1;
        followerContributions.put(personId, newContrib);
        updateBestContributor(personId, newContrib, oldContrib);
    }

    public void decrementContributor(int personId) {
        int oldContrib = followerContributions.getOrDefault(personId, 0);
        if (oldContrib > 0) {
            int newContrib = oldContrib - 1;
            followerContributions.put(personId, newContrib);
            updateBestContributor(personId, newContrib, oldContrib);
        } else {
            // doesn't change if the contribution is already 0
        }
    }

    private void updateBestContributor(int personId, int newContrib, int oldContrib) {
        if (newContrib > oldContrib) {

            if (newContrib > maxContribution) {
                maxContribution = newContrib;
                bestContributorId = personId;
            } else if (newContrib == maxContribution) {
                bestContributorId = Math.min(bestContributorId, personId);
            }
        } else if (newContrib < oldContrib) {
            if (personId == bestContributorId || oldContrib == maxContribution) {
                recalculateBestContributor(); // O(NumFollowers)
            }
        }
        // If newContrib == oldContrib, nothing changes.
    }

    private void recalculateBestContributor() {
        if (followersMap.isEmpty()) {
            bestContributorId = Integer.MAX_VALUE;
            maxContribution = -1;
            return;
        }
        int currentbestid = Integer.MAX_VALUE;
        int currentMaxValue = -1;

        for (Map.Entry<Integer, Integer> entry : followerContributions.entrySet()) {
            int fid = entry.getKey();
            int fcontrib = entry.getValue();

            if (fcontrib > currentMaxValue) {
                currentMaxValue = fcontrib;
                currentbestid = fid;
            } else if (fcontrib == currentMaxValue) {

                currentbestid = Math.min(currentbestid, fid);
            }
        }
        this.bestContributorId = currentbestid;
        this.maxContribution = currentMaxValue;
    }
    // --- End helper methods ---

    public Map<Integer, PersonInterface> getFollowersMap() {
        return followersMap;
    }
}