package com.marlabs.assessment.client;

import com.marlabs.assessment.config.CallerProperties;
import com.marlabs.assessment.exception.ApiException;
import com.marlabs.assessment.model.ApiModels;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import java.util.Base64;
import java.util.concurrent.TimeoutException;

@Component
public class PythonClient {
  private final RestClient client; private final String baseUrl;
  public PythonClient(RestClient client,@Value("${assessment.python-service-url}") String baseUrl){this.client=client;this.baseUrl=baseUrl;}
  public ApiModels.PythonAnswerResponse answer(ApiModels.AnswerRequest r, CallerProperties.Caller c){
    try { return client.post().uri(baseUrl+"/internal/answer").contentType(MediaType.APPLICATION_JSON).body(new ApiModels.PythonAnswerRequest(r.question(),r.as_of(),c.tenant(),c.role())).retrieve().body(ApiModels.PythonAnswerResponse.class); }
    catch(ResourceAccessException e){ throw new ApiException("DEPENDENCY_TIMEOUT","Policy provider timed out or was unavailable",502); }
    catch(Exception e){ throw new ApiException("MALFORMED_PROVIDER_RESPONSE","Policy provider returned malformed output",502); }
  }
  public ApiModels.PythonExtractResponse extract(String id,String filename,byte[] bytes,String asOf,CallerProperties.Caller c){
    try {
      var req=new ApiModels.PythonExtractRequest(id,filename,Base64.getEncoder().encodeToString(bytes),asOf,c.tenant(),c.role());
      return client.post().uri(baseUrl+"/internal/extract").contentType(MediaType.APPLICATION_JSON).body(req).retrieve().body(ApiModels.PythonExtractResponse.class);
    } catch(ResourceAccessException e){ throw new ApiException("DEPENDENCY_UNAVAILABLE","Document provider was unavailable",502); }
      catch(Exception e){ throw new ApiException("MALFORMED_PROVIDER_RESPONSE","Document provider returned malformed output",502); }
  }
}
