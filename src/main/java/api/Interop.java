package api;

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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Interop {

    private final String GITHUB_OAUTH_TOKEN;

    public Interop() {
        GITHUB_OAUTH_TOKEN = System.getenv("GITHUB_OAUTH_TOKEN");
    }

    public List<GitHubRepository> getMostStarredRepositories() throws URISyntaxException, IOException {
        URI requestUri = getBaseUriToGitHubApi()
                .setPath("search/repositories")
                .setParameter("q", "stars:>0")
                .setParameter("sort", "stars")
                .setParameter("order", "desc")
                .build();

        JSONObject responseBody = new JSONObject(getResponseBody(requestUri));
        JSONArray repositories = responseBody.getJSONArray("items");

        List<GitHubRepository> repositoryInfos = new ArrayList<>(5);

        for (int i = 0; i < 10 && i < repositories.length(); i++) {
            JSONObject jsonObject = (JSONObject)repositories.get(i);

            repositoryInfos.add(new GitHubRepository(
                    jsonObject.getString("full_name"),
                    jsonObject.isNull("description") ? "Description is not specified" : jsonObject.getString("description"),
                    jsonObject.isNull("language") ? "Language is not specified" : jsonObject.getString("language"),
                    jsonObject.getString("html_url"),
                    getContributorsOfRepository(jsonObject.getString("contributors_url")
            )));
        }

        return repositoryInfos;
    }

    private String getResponseBody(URI requestUri) throws IOException {

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpget = new HttpGet(requestUri);
            httpget.setHeader("Accept", "application/vnd.github.v3+json");
            httpget.setHeader("Authorization", "token " + GITHUB_OAUTH_TOKEN);

            System.out.println("Executing request " + httpget.getRequestLine());

            ResponseHandler<String> responseHandler = response -> {
                int status = response.getStatusLine().getStatusCode();

                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            };

            return httpclient.execute(httpget, responseHandler);
        }
    }
    private URIBuilder getBaseUriToGitHubApi() throws URISyntaxException {
        return new URIBuilder()
                .setScheme("https")
                .setHost("api.github.com");
    }

    private List<GitHubContributor> getContributorsOfRepository(String contributorsUrlString) throws URISyntaxException, IOException {
        ArrayList<GitHubContributor> contributorsOfRepository = new ArrayList<>();

        JSONArray jsonArray = new JSONArray(getResponseBody(new URI(contributorsUrlString)));
        JSONObject currentJsonObject;
        for (int i = 0; i < jsonArray.length(); i++) {
            currentJsonObject = jsonArray.getJSONObject(i);

            contributorsOfRepository.add(new GitHubContributor(
                    currentJsonObject.getString("login"),
                    currentJsonObject.getInt("contributions"), 
                    currentJsonObject.getString("html_url")
            ));
        }

        contributorsOfRepository.sort(Comparator.comparing(GitHubContributor::getCommitsCount).reversed());
        return contributorsOfRepository;
    }
}
