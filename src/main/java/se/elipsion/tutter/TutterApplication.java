package se.elipsion.tutter;

import bsh.EvalError;
import bsh.Interpreter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.Map;

import spark.Request;
import spark.Response;

import static spark.Spark.get;

public class TutterApplication {

  public static void main(final String[] args) throws IOException {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    TutterConfiguration configuration;
    configuration = mapper.readValue(new File(args[0]), TutterConfiguration.class);
    for(Map.Entry<String, EndpointConfiguration> endpoint : configuration.getEndpoints().entrySet()) {
      String script = getScript(endpoint.getValue());
      get(endpoint.getKey(), (req, resp) -> handleRequest(req, resp, script, endpoint.getValue()));
    }
  }

  private static String handleRequest(Request req, Response resp, String script,
                                      EndpointConfiguration config) {
    Interpreter interpreter = new Interpreter();
    OutputStream os = new ByteArrayOutputStream();
    interpreter.setOut(new PrintStream(os));
    try {
      for(Map.Entry<String, Object> entry : config.parameters.entrySet()) {
        interpreter.set(entry.getKey(), entry.getValue());
      }
      interpreter.eval(script);
    } catch (EvalError evalError) {
      // Wut, wut?
      evalError.printStackTrace();
    }
    return os.toString();
  }

  private static String getScript(EndpointConfiguration config) throws IOException {
    if(config.script != null){
      return config.script;
    }
    if(config.scriptPath != null) {
      return Files.readAllBytes(new File(config.scriptPath).toPath()).toString();
    }
    throw new RuntimeException("No action specified");
  }
}
