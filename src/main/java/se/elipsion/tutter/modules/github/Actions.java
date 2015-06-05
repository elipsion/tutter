package se.elipsion.tutter.modules.github;


import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import com.jcabi.github.Pull;

import java.io.IOException;

/**
 * Created by elipsion on 6/3/15.
 */
public class Actions {

  public static String saneReview(Github github, String repository, int pullRequest)
      throws IOException {
    Pull pull = github.repos().get(new Coordinates.Simple(repository)).pulls().get(pullRequest);
    return saneReview(github,pull);
  }
  public static String saneReview(Github github, Pull pull)
      throws IOException {
    if (Util.hasMergeCommand(pull)) {
      if (Util.sumVotes(pull) > 1) {
        //pull.merge("Merged by tutter, the almighty");
        return "Merged!";
      } else {
        //new Pull.Smart(pull).issue().comments().post("Not enough votes to merge");
        return "Not enough votes to merge";
      }
    } else {
      return "No merge command submitted; standing by";
    }
  }


}
