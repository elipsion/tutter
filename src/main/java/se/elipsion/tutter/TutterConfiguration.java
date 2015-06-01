package se.elipsion.tutter;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;


public class TutterConfiguration {

  @JsonProperty
  private Map<String,EndpointConfiguration> endpoints;

  public Map<String, EndpointConfiguration> getEndpoints() {
    return endpoints;
  }
}
