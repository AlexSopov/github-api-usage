package api;

import java.util.List;

public class GitHubRepository{
    private String name;
    private String description;
    private String language;
    private String url;
    private List<GitHubContributor> repositoryContributors;

    public GitHubRepository(String name, String description, String language, String url, List<GitHubContributor> repositoryContributors) {

        this.name = name;
        this.description = description;
        this.language = language;
        this.url = url;
        this.repositoryContributors = repositoryContributors;
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

}
