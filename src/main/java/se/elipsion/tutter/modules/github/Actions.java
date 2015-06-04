package se.elipsion.tutter.modules.github;


import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import com.jcabi.github.Pull;

import java.io.IOException;

/**
 * Created by elipsion on 6/3/15.
 */
public class Actions {

  public static String sppuppet(Github github, String repository, int pullRequest)
      throws IOException {
    Pull pull = github.repos().get(new Coordinates.Simple(repository)).pulls().get(pullRequest);
    if (Util.hasMergeCommand(pull))
      if (Util.sumVotes(pull) > 1) {
        pull.merge("Merged by tutter, the almighty");
      } else {

      }

    return "";
  }


}
