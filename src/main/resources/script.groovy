import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.jcabi.github.Coordinates
import com.jcabi.github.Github
import com.jcabi.github.Pull
import se.elipsion.tutter.modules.github.Actions
import se.elipsion.tutter.modules.github.Util
import spark.Request

Github ghe = Util.gitHubEnterprise(gitHubEndPoint, accessToken)
String event = ((Request) request).headers("X-GitHub-Event")
JsonNode body = new ObjectMapper().readTree(((Request) request).body())
String repo = body.get("repository").get("full_name").asText()
switch(event){
    case "status":
        if("success" != body.get("state").asText()) {
            println "Merge state not clean"
            return 200
        }
        String sha = body.get("commit").get("sha").asText();
        Pull pull = Util.findPullRequest(ghe.repos().get(Coordinates.Simple(repo)), sha)
        print Actions.saneReview(ghe, pull)
        return 200;
    case "issue_comment":
        int number = body.get("issue").get("number").asInt()
        print Actions.saneReview(ghe, repo, number);
        return 200;
    default:
        println("No action configured for: "+event);
        return 200;
}
