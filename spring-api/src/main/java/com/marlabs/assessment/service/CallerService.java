package com.marlabs.assessment.service;

import com.marlabs.assessment.config.CallerProperties;
import com.marlabs.assessment.exception.ApiException;
import org.springframework.stereotype.Service;

@Service
public class CallerService {
  private final CallerProperties props;
  public CallerService(CallerProperties props){this.props=props;}
  public CallerProperties.Caller require(String id){
    if(id==null || id.isBlank()) throw new ApiException("INVALID_CALLER","X-Caller-Id is required",400);
    var caller=props.callers().get(id);
    if(caller==null) throw new ApiException("INVALID_CALLER","Unknown caller",401);
    return caller;
  }
}
