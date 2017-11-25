import api.GitHubRepository;
import api.Interop;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Program {
    public static void main(String[] args) {
        try {
            List<GitHubRepository> reps =  new Interop().getMostStarredRepositories();
            String result = "";

            for (int i = 0; i < reps.size(); i++) {
                GitHubRepository currentt = reps.get(i);


                result += "**_" + (i + 1) + "." + " " + currentt.getName() + "_**<br>";
                result += "**Repository URL:** " + currentt.getUrl() + "<br>";
                result += "**Description:** " + currentt.getDescription() + "<br>";
                result += "**Language:** " + currentt.getLanguage() + "<br>";
                result += "Top contributors: <br> \n";

                for (int j = 0; j < 5; j++) {
                    result += "* " + currentt.getRepositoryContributors().get(j).convertToMarkdown() + "\n";
                }

                result += "<br> \n\n\r";
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
