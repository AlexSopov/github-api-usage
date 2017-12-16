import api.GitHubContributor;
import org.junit.Assert;
import org.junit.Test;

public class TestGitHubContributor {
    @Test
    public void testGitHubContributorProperties() {
        GitHubContributor contributor = new GitHubContributor("test", 100, "test2");

        Assert.assertEquals(contributor.getCommitsCount(), 100);
        Assert.assertEquals(contributor.getProfileUrl(), "test2");
        Assert.assertEquals(contributor.getName(), "test");

        contributor.setCommitsCount(1);
        contributor.setProfileUrl("test3");
        contributor.setName("test4");

        Assert.assertEquals(contributor.getCommitsCount(), 1);
        Assert.assertEquals(contributor.getProfileUrl(), "test3");
        Assert.assertEquals(contributor.getName(), "test4");
    }
}
