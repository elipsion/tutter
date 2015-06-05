package se.elipsion.tutter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.SparkBase;

public class TutterApplication extends SparkBase {

  public static void main(final String[] args) throws IOException

  {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    TutterConfiguration configuration;
    String configPath = args[0];
    configuration = mapper.readValue(new File(configPath), TutterConfiguration.class);
    for(Map.Entry<String, EndpointConfiguration> endpoint : configuration.getEndpoints().entrySet()) {
      Route route = null;
      if (null != endpoint.getValue().script) {
        route = (req, resp) -> handleRequest(req, resp,
                                             endpoint.getValue().script,
                                             endpoint.getValue(), true);
      }
      if (null != endpoint.getValue().scriptPath) {
        route = (req, resp) -> handleRequest(req, resp,
                                             endpoint.getValue().scriptPath,
                                             endpoint.getValue(), false);
      }

      for (String method : endpoint.getValue().methods)
        addRoute(method.toLowerCase(), wrap(endpoint.getKey(), route));
    }
  }

  private static String handleRequest(Request req, Response resp, String script, EndpointConfiguration config, boolean inline) {
    Binding binding = getBindings(req, resp, config);
    Object ret;
    try {
      if (inline)
        ret = handleRequestInline(script, binding);
      else
        ret = handleRequestFile(script, binding);
    } catch (Exception e){
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      resp.status(501);
      return sw.toString();
    }
    if(null != ret && Integer.class == ret.getClass())
      resp.status((Integer) ret);
    return binding.getProperty("outBuffer").toString();
  }
  private static Object handleRequestInline(String script, Binding binding) {
    Script interpreter = new GroovyShell().parse(script);
    interpreter.setBinding(binding);
    return interpreter.run();
  }
  private static Object handleRequestFile(String URI, Binding binding)
      throws MalformedURLException, ResourceException, ScriptException {
    URL path = new File(java.net.URI.create(URI)).getParentFile().toURI().toURL();
    String scriptName = new File(java.net.URI.create(URI)).getName();
    GroovyScriptEngine interpreter = new GroovyScriptEngine(new URL[]{path});
    return interpreter.run(scriptName, binding);
  }

  private static Binding getBindings(Request req, Response resp, EndpointConfiguration config) {
    Binding b = new Binding();
    OutputStream os = new ByteArrayOutputStream();
    PrintStream pw = new PrintStream(os);
    for(Map.Entry<String, Object> entry : config.parameters.entrySet()) {
      b.setVariable(entry.getKey(), entry.getValue());
    }
    b.setVariable("request", req);
    b.setVariable("response", resp);
    b.setProperty("out", pw);
    b.setProperty("outBuffer", os);
    return b;
  }

}
