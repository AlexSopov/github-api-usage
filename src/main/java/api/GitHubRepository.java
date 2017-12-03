package api;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GitHubRepository{
    private String name;
    private String description;
    private String language;
    private String url;
    private int totalCommitsCount;
    private List<GitHubContributor> repositoryContributors;

    public GitHubRepository(String name, String description, String language, String url) {
        this.name = name;
        this.description = description;
        this.language = language;
        this.url = url;
        this.repositoryContributors = new ArrayList<>();
    }
    public GitHubRepository(JSONObject gitHubRepositoryJson) {
        this.name = gitHubRepositoryJson.getString("full_name");
        this.description = gitHubRepositoryJson.isNull("description") ? "Description is not specified" : gitHubRepositoryJson.getString("description");
        this.language = gitHubRepositoryJson.isNull("language") ? "Language is not specified" : gitHubRepositoryJson.getString("language");
        this.url = gitHubRepositoryJson.getString("html_url");
        this.repositoryContributors = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<GitHubContributor> getRepositoryContributors() {
        return repositoryContributors;
    }

    public void setRepositoryContributors(List<GitHubContributor> repositoryContributors) {
        this.repositoryContributors = repositoryContributors;
    }

    public int getTotalCommitsCount() {
        return totalCommitsCount;
    }

    public void setTotalCommitsCount(int totalCommitsCount) {
        this.totalCommitsCount = totalCommitsCount;
    }

    public void setContributorCommit(JSONObject committerJson) {
        String committerUrl = committerJson.getString("html_url");
        GitHubContributor committer = null;

        for (GitHubContributor gitHubContributor : repositoryContributors) {
            if (gitHubContributor.getProfileUrl().equals(committerUrl)) {
                committer = gitHubContributor;
                break;
            }
        }

        if (committer == null) {
            committer = new GitHubContributor(committerJson);
            repositoryContributors.add(committer);
        }

        committer.setCommitsCount(committer.getCommitsCount() + 1);
    }
}
