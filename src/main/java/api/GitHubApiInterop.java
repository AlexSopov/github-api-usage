package api;

import javafx.util.Pair;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitHubApiInterop {

    private final String GITHUB_OAUTH_TOKEN;

    public GitHubApiInterop() {
        GITHUB_OAUTH_TOKEN = System.getenv("GITHUB_OAUTH_TOKEN");
    }

    /**
     * Finds most starred repositories.
     * General algorithm of working: <br>
     * <ol>
     *     <li>
     *         Get list of repositories, with condition that each repository has at least one star.
     *         Sort list of repositories by stars count descending.
     *     </li>
     *     <li>
     *         For top 10 repositories get list of top 5 contributors, by calling api with argument that was given in
     *         key "contributors_url" in each repository JSON item.
     *     </li>
     * </ol>
     * @return List of 10 most starred repositories.
     */
    public List<GitHubRepository> getMostStarredRepositories() throws URISyntaxException, IOException {
        URI requestUri = getBaseUriToGitHubApi()
                .setPath("search/repositories")
                .setParameter("q", "stars:>0")
                .setParameter("sort", "stars")
                .setParameter("order", "desc")
                .build();

        JSONObject responseBody = new JSONObject(getResponse(requestUri).getValue());
        JSONArray repositoriesJsonArray = responseBody.getJSONArray("items");

        List<GitHubRepository> mostStarredRepositories = new ArrayList<>(10);
        JSONObject currentRepositoryJson;
        GitHubRepository currentGitHubRepository;
        for (int i = 0; i < 10 && i < repositoriesJsonArray.length(); i++) {
            currentRepositoryJson = repositoriesJsonArray.getJSONObject(i);

            currentGitHubRepository = new GitHubRepository(currentRepositoryJson);
            currentGitHubRepository.setRepositoryContributors(getTopFiveContributorsOfRepositoryTotally(currentRepositoryJson.getString("contributors_url")));

            mostStarredRepositories.add(currentGitHubRepository);
        }
        return mostStarredRepositories;
    }

    /**
     * Finds 10 most commited repositories during specified period.
     * General algorithm of working: <br>
     * <ol>
     *     <li>
     *       Prepare list of 1000* most starred repositories, sorted by stars count descending.
     *     </li>
     *     <li>
     *         Get commits count to prepared list of repositories during specified period. Sort list by total commits.
     *         Commits count is created by calling search api with qualifier commits and parameters committer-date and repo. <br>
     *         E.g. https://api.github.com/search/commits?q=committer-date:2017-11-25..2017-11-26+repo:freeCodeCamp/freeCodeCamp
     *     </li>
     *     <li>
     *         Iterate through 10 most commited repositories and get list of commits for each. Prepare list of committer to repository
     *         and associate commits count to repository during specified period with them. Sort commiters by commits count
     *     </li>
     *     *Note: because of oauth authorization and search api limitations, each query will be send each 3 seconds,
     *     and only 1000 repositories will be searched.
     * </ol>
     * @param fromDate The beginning of period to search.
     * @param toDate The end of period to search.
     * @return List which contains 10 most commited repositories during specified period.
     */
    public List<GitHubRepository> getTopCommitedRepositoriesInPeriod(Calendar fromDate, Calendar toDate) throws URISyntaxException, IOException {
        SimpleDateFormat iso8601DateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String iso8601DateFormatFromDate = iso8601DateFormat.format(fromDate.getTime());
        String iso8601DateFormatToDate = iso8601DateFormat.format(toDate.getTime());

        List<GitHubRepository> topCommitedRepositories = getTopRepositories(10);
        sortRepositoriesByCommitsCountInPeriod(topCommitedRepositories, iso8601DateFormatFromDate, iso8601DateFormatToDate);
        topCommitedRepositories = topCommitedRepositories.subList(0, Math.min(topCommitedRepositories.size(), 10));
        initializeTopFiveContributorsOfRepositoryInPeriod(topCommitedRepositories, iso8601DateFormatFromDate, iso8601DateFormatToDate);

        return topCommitedRepositories;
    }

    /**
     * Executes specified request and reads response to used structures.
     * @param requestUri Request to be executed.
     * @return Pair: data from Link header and response body, converted to String
     * @throws ClientProtocolException if response code was not in range [200, 300)
     */
    private Pair<String, String> getResponse(URI requestUri) throws IOException {

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet getRequest = new HttpGet(requestUri);
            getRequest.setHeader("Accept", "application/vnd.github.v3+json");
            getRequest.setHeader("Accept", "application/vnd.github.cloak-preview");
            getRequest.setHeader("Authorization", "token " + GITHUB_OAUTH_TOKEN);

            System.out.println("Executing request " + getRequest.getRequestLine());

            ResponseHandler<Pair<String, String>> responseHandler = response -> {
                int status = response.getStatusLine().getStatusCode();

                if (status >= 200 && status < 300) {
                    HttpEntity responseBodyEntity = response.getEntity();
                    String responseBody = responseBodyEntity != null ? EntityUtils.toString(responseBodyEntity) : null;
                    Header responseHeader = response.getFirstHeader("Link");
                    String responseHeaderValue = responseHeader != null ? responseHeader.getValue() : null;

                    return new Pair<>(responseHeaderValue, responseBody);
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            };

            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            return httpclient.execute(getRequest, responseHandler);
        }
    }

    private List<GitHubRepository> getTopRepositories(int maxRepositories) throws URISyntaxException, IOException {
        URI requestUri = getBaseUriToGitHubApi()
                .setPath("search/repositories")
                .setParameter("q", "stars:>0")
                //.setParameter("q", String.format("pushed:%s..%s", fromDate, toDate))
                .setParameter("sort", "stars")
                .setParameter("order", "desc")
                .build();

        List<GitHubRepository> commitedRepositoriesInPeriod = new ArrayList<>();

        Pair<String, String> currentResponse;
        JSONObject currentJsonObject;
        JSONArray currentRepositories;

        while (requestUri != null && commitedRepositoriesInPeriod.size() < maxRepositories) {
            currentResponse = getResponse(requestUri);

            currentJsonObject = new JSONObject(currentResponse.getValue());
            currentRepositories = currentJsonObject.getJSONArray("items");
            currentRepositories.forEach(repositoryJson -> commitedRepositoriesInPeriod.add(new GitHubRepository((JSONObject)repositoryJson)));

            requestUri = matchNextResponsePage(currentResponse.getKey());
        }

        return commitedRepositoriesInPeriod;
    }
    private void sortRepositoriesByCommitsCountInPeriod(List<GitHubRepository> gitHubRepositories, String fromDate, String toDate) throws URISyntaxException, IOException {
        String repositoryCommitsInPeriodRequestParameter = String.format("committer-date:%s..%s repo:", fromDate, toDate) + "%s";

        URIBuilder baseUriForCommitsInPeriod = getBaseUriToGitHubApi().setPath("search/commits");
        URI repositoryCommitsInPeriodRequest;
        JSONObject currentRepositoryCommitsJson;

        for (GitHubRepository gitHubRepository : gitHubRepositories) {
            repositoryCommitsInPeriodRequest = baseUriForCommitsInPeriod
                    .setParameter("q", String.format(repositoryCommitsInPeriodRequestParameter, gitHubRepository.getName()))
                    .build();

            currentRepositoryCommitsJson = new JSONObject(getResponse(repositoryCommitsInPeriodRequest).getValue());
            gitHubRepository.setTotalCommitsCount(currentRepositoryCommitsJson.getInt("total_count"));
        }

        gitHubRepositories.sort(Comparator.comparing(GitHubRepository::getTotalCommitsCount).reversed());
    }
    private void initializeTopFiveContributorsOfRepositoryInPeriod(List<GitHubRepository> gitHubRepositories, String fromDate, String toDate) throws URISyntaxException, IOException {
        String repositoryCommitsInPeriodRequestParameter = String.format("committer-date:%s..%s repo:", fromDate, toDate) + "%s";

        URIBuilder baseUriForCommitsInPeriod = getBaseUriToGitHubApi().setPath("search/commits");
        URI repositoryCommitsInPeriodRequest;
        Pair<String, String> currentResponse;
        JSONObject currentCommitJsonObject;

        for (GitHubRepository gitHubRepository : gitHubRepositories) {
            repositoryCommitsInPeriodRequest = baseUriForCommitsInPeriod
                    .setParameter("q", String.format(repositoryCommitsInPeriodRequestParameter, gitHubRepository.getName()))
                    .build();

            while (repositoryCommitsInPeriodRequest != null) {
                currentResponse = getResponse(repositoryCommitsInPeriodRequest);
                currentCommitJsonObject = new JSONObject(currentResponse.getValue());

                for (Object commit : currentCommitJsonObject.getJSONArray("items")) {
                    currentCommitJsonObject = (JSONObject)commit;

                    if (currentCommitJsonObject.isNull("committer")) {
                        continue;
                    }
                    currentCommitJsonObject = currentCommitJsonObject.getJSONObject("committer");
                    gitHubRepository.setContributorCommit(currentCommitJsonObject);
                }

                gitHubRepository.getRepositoryContributors().sort(Comparator.comparing(GitHubContributor::getCommitsCount).reversed());
                repositoryCommitsInPeriodRequest = matchNextResponsePage(currentResponse.getKey());
            }
        }
    }
    private List<GitHubContributor> getTopFiveContributorsOfRepositoryTotally(String contributorsUrlString) throws URISyntaxException, IOException {
        List<GitHubContributor> contributorsOfRepository = new ArrayList<>();

        JSONArray contributorsJsonArray = new JSONArray(getResponse(new URI(contributorsUrlString)).getValue());
        JSONObject currentContributorJson;
        GitHubContributor currentGitHubContributor;

        for (int i = 0; i < 5 && i < contributorsJsonArray.length(); i++) {
            currentContributorJson = contributorsJsonArray.getJSONObject(i);
            currentGitHubContributor = new GitHubContributor(currentContributorJson);
            currentGitHubContributor.setCommitsCount(currentContributorJson.getInt("contributions"));
            contributorsOfRepository.add(currentGitHubContributor);
        }

        return contributorsOfRepository;
    }

    private URIBuilder getBaseUriToGitHubApi() throws URISyntaxException {
        return new URIBuilder()
                .setScheme("https")
                .setHost("api.github.com");
    }
    private URI matchNextResponsePage(String linkHeader) throws URISyntaxException {
        if (linkHeader == null || linkHeader.isEmpty()) {
            return null;
        }

        Pattern headerNextResponsePagePattern = Pattern.compile("<(.*)>; rel=\"next\"");
        Matcher headerNextResponsePatternMatcher = headerNextResponsePagePattern.matcher(linkHeader);

        if (headerNextResponsePatternMatcher.find()) {
            return new URI(headerNextResponsePatternMatcher.group(1));
        }
        else {
            return null;
        }
    }
}
