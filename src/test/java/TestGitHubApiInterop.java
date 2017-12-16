import api.GitHubApiInterop;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Calendar;

public class TestGitHubApiInterop {

    @Test
    public void testGetMostStarredRepositories() {
        GitHubApiInterop gitHubApiInterop = new GitHubApiInterop();
        try {
            Assert.assertNotNull(gitHubApiInterop.getMostStarredRepositories());
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void testGetTopCommitedRepositoriesInPeriod() {
        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();

        calendar1.set(2017, 11, 16);
        calendar2.set(2017, 11, 16);

        GitHubApiInterop gitHubApiInterop = new GitHubApiInterop();
        try {
            Assert.assertNotNull(gitHubApiInterop.getTopCommitedRepositoriesInPeriod(calendar1, calendar2));
        } catch (Exception e) {
            Assert.fail();
        }
    }
}
