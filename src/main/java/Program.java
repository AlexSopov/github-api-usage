import api.GitHubRepository;
import api.GitHubApiInterop;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class Program {
    public static void main(String[] args) {
        try {

            List<GitHubRepository> reps =  new GitHubApiInterop().getTopCommitedRepositoriesInPeriod(
                    new GregorianCalendar(2017, Calendar.SEPTEMBER, 11),
                    new GregorianCalendar(2017, Calendar.SEPTEMBER, 17));

            String result = "";

            for (int i = 0; i < reps.size(); i++) {
                GitHubRepository currentt = reps.get(i);


                result += "**_" + (i + 1) + "." + " " + currentt.getName() + "_**<br>";
                result += "**Repository URL:** " + currentt.getUrl() + "<br>";
                result += "**Description:** " + currentt.getDescription() + "<br>";
                result += "**Language:** " + currentt.getLanguage() + "<br>";
                result += "Top contributors: <br> \n";

                for (int j = 0; j < 5 && j < currentt.getRepositoryContributors().size(); j++) {
                    result += "* " + currentt.getRepositoryContributors().get(j).convertToMarkdown() + "\n";
                }

                result += "<br> \n\n\r";
            }

            System.out.println(result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
