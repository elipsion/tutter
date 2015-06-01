package se.elipsion.tutter;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;


/**
 * Created by elipsion on 6/1/15.
 */

public class EndpointConfiguration {
  @JsonProperty
  String script;

  @JsonProperty
  String scriptPath;

  @JsonProperty
  HashMap<String, Object> parameters;
}
