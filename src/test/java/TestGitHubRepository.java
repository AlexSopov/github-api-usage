import api.GitHubContributor;
import api.GitHubRepository;
import org.junit.Assert;
import org.junit.Test;

public class TestGitHubRepository {
    @Test
    public void testGitHubContributorProperties() {
        GitHubRepository repository = new GitHubRepository("name", "description", "language", "url");

        Assert.assertEquals(repository.getName(), "name");
        Assert.assertEquals(repository.getDescription(), "description");
        Assert.assertEquals(repository.getLanguage(), "language");
        Assert.assertEquals(repository.getUrl(), "url");

        repository.setName("name1");
        repository.setDescription("description1");
        repository.setLanguage("language1");
        repository.setUrl("url1");

        Assert.assertEquals(repository.getName(), "name1");
        Assert.assertEquals(repository.getDescription(), "description1");
        Assert.assertEquals(repository.getLanguage(), "language1");
        Assert.assertEquals(repository.getUrl(), "url1");
    }
}
