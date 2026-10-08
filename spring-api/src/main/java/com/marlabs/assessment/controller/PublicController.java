package com.marlabs.assessment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marlabs.assessment.model.ApiModels;
import com.marlabs.assessment.service.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@RestController
public class PublicController {
  private final CallerService callers; private final ValidationService validation; private final com.marlabs.assessment.client.PythonClient python; private final BatchService batch; private final ObjectMapper mapper;
  public PublicController(CallerService callers,ValidationService validation,com.marlabs.assessment.client.PythonClient python,BatchService batch,ObjectMapper mapper){this.callers=callers;this.validation=validation;this.python=python;this.batch=batch;this.mapper=mapper;}
  @PostMapping("/answer")
  public ApiModels.AnswerResponse answer(@RequestHeader(value="X-Caller-Id",required=false) String id,@RequestBody ApiModels.AnswerRequest req){
    validation.question(req); return python.answer(req,callers.require(id));
  }
  @PostMapping(value="/batches",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiModels.BatchResponse batch(@RequestHeader(value="X-Caller-Id",required=false) String id,@RequestPart("metadata") String metadata,@RequestPart("files") MultipartFile[] files) throws Exception {
    var caller=callers.require(id); ApiModels.Manifest m=mapper.readValue(metadata,ApiModels.Manifest.class);
    Map<String,MultipartFile> byName=new LinkedHashMap<>(); for(var f:files){ if(f.getOriginalFilename()!=null) byName.put(f.getOriginalFilename(),f); }
    return batch.process(m,byName,caller);
  }
}
