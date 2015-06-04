package se.elipsion.tutter.modules.github;


import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.jcabi.github.Github;
import com.jcabi.github.Pull;
import com.jcabi.github.PullComment;
import com.jcabi.github.RtGithub;
import com.jcabi.http.Request;
import com.jcabi.http.request.ApacheRequest;
import com.jcabi.manifests.Manifests;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

/**
 * Created by elipsion on 6/3/15.
 */
public class Util {

  public static final String[] UP_VOTES = new String[]{"+1", ":+1:"};
  public static final String[] DOWN_VOTES = new String[]{"-1", ":-1:"};
  public static final String[] MERGE_COMMANDS = new String[]{"!merge", "merge", ":shipit:", ":ship:"};
  private static final String USER_AGENT = String.format(
      "jcabi-github %s %s %s",
      Manifests.read("JCabi-Version"),
      Manifests.read("JCabi-Build"),
      Manifests.read("JCabi-Date")
  );

  public static Request gitHubEnterpriseRequest(String url) {
    return new ApacheRequest(url)
        .header(HttpHeaders.USER_AGENT, USER_AGENT)
        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
  }

  public static Github gitHubEnterprise(String URI) {
    return new RtGithub(gitHubEnterpriseRequest(URI));
  }

  public static int sumVotes(Pull pullRequest) throws IOException {
    return sumVotes(pullRequest, UP_VOTES, DOWN_VOTES);
  }

  public static int sumVotes(Pull pullRequest, String[] upVotes, String[] downVotes)
      throws IOException {
    int votes = 0;
    String author = pullRequest.json().getJsonObject("user").getString("login");
    // Loop over comments
    commentCounter:
    for (PullComment dumbComment : validCommandComments(pullRequest)) {
      PullComment.Smart comment = new PullComment.Smart(dumbComment);
      // Count votes
      String reviewer = comment.author();
      String message = comment.body();
      if (reviewer.equals(author)) {
        // We don't allow the author to vote
        continue commentCounter;
      }
      for (String match : upVotes) {
        if (message.startsWith(match)) {
          votes++;
          continue commentCounter;
        }
      }
      for (String match : downVotes) {
        if (message.startsWith(match)) {
          votes--;
          continue commentCounter;
        }
      }
    }
    return votes;
  }

  public static Iterable<PullComment> commentsAfter(Iterable<PullComment> commentIterator,
                                                    String date) throws IOException {
    DateFormat dateFormat = new ISO8601DateFormat();
    try {
      return commentsAfter(commentIterator, dateFormat.parse(date));
    } catch (ParseException e) {
      throw new RuntimeException("Could not parse date field on head commit");
    }
  }
  public static Iterable<PullComment> commentsAfter(Iterable<PullComment> commentIterator,
                                                    Date date) throws IOException{
    DateFormat dateFormat = new ISO8601DateFormat();
    Stream<PullComment> stream = StreamSupport.stream(commentIterator.spliterator(), false);
    return stream.filter(comment -> {
      try {
        return date.before(dateFormat.parse(comment.json().getString("updated_at")));
      } catch (ParseException e) {
        throw new RuntimeException("Could not parse date field on " + comment.number());
      } catch (IOException e) {
        // Why oh why?
        throw new RuntimeException(e);
      }
    }).collect( Collectors.toList());
  }

  public static Iterable<PullComment> validCommandComments(Pull pull) throws IOException {
    String headSHA = pull.json().getJsonObject("head").getString("sha");
    String headTime = pull.repo().commits().get(headSHA).json()
        .getJsonObject("committer").getString("date");
    return commentsAfter(pull.comments().iterate(null), headTime);
  }

  public static boolean hasMergeCommand(Pull pull) throws IOException {
    for (PullComment dumbComment : validCommandComments(pull))
      for(String match : MERGE_COMMANDS)
        if(new PullComment.Smart(dumbComment).body().startsWith(match))
          return true;
    return false;
  }
}
