package api;

public class GitHubContributor implements MarkdownConvertiable {
    private String name;
    private int commitsCount;
    private String profileUrl;

    public GitHubContributor(String name, int commitsCount, String profileUrl) {
        this.name = name;
        this.commitsCount = commitsCount;
        this.profileUrl = profileUrl;
    }

    public int getCommitsCount() {
        return commitsCount;
    }
    public void setCommitsCount(int commitsCount) {
        this.commitsCount = commitsCount;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getProfileUrl() {
        return profileUrl;
    }
    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    @Override
    public String convertToMarkdown() {
        StringBuilder mdResult = new StringBuilder();

        mdResult.append("*").append(name).append("* [").append(profileUrl).append("]");
        mdResult.append(". Contributions count: ").append(commitsCount);

        return mdResult.toString();
    }
}
