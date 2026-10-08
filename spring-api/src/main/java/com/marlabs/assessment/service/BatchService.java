package com.marlabs.assessment.service;

import com.marlabs.assessment.client.PythonClient;
import com.marlabs.assessment.config.CallerProperties;
import com.marlabs.assessment.model.ApiModels;
import com.marlabs.assessment.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.security.MessageDigest;
import java.util.*;

@Service
public class BatchService {
  private final PythonClient python; private final ValidationService validation;
  public BatchService(PythonClient python,ValidationService validation){this.python=python;this.validation=validation;}
  public ApiModels.BatchResponse process(ApiModels.Manifest m, Map<String,MultipartFile> files, CallerProperties.Caller caller){
    validation.manifest(m,files);
    Map<String,String> hashOwner=new HashMap<>(); List<ApiModels.BatchResult> out=new ArrayList<>();
    for(var d:m.documents()){
      MultipartFile f=files.get(d.filename());
      try {
        byte[] bytes=f.getBytes();
        if(bytes.length==0) throw new ApiException("EMPTY_FILE","File is empty",422);
        String hash=hex(MessageDigest.getInstance("SHA-256").digest(bytes));
        String dup=hashOwner.putIfAbsent(hash,d.document_id());
        var r=python.extract(d.document_id(),d.filename(),bytes,m.as_of(),caller);
        out.add(new ApiModels.BatchResult(d.document_id(),"COMPLETED",r.extracted(),r.field_evidence(),r.policy_outcome(),true,r.review_issues(),dup,null));
      } catch(ApiException e){ out.add(failed(d.document_id(),e)); }
      catch(Exception e){ out.add(failed(d.document_id(),new ApiException("UNREADABLE_FILE","File could not be processed",422))); }
    }
    long completed=out.stream().filter(x->x.processing_status().equals("COMPLETED")).count();
    return new ApiModels.BatchResponse(m.batch_id(),new ApiModels.Summary(out.size(),(int)completed,out.size()-(int)completed),out);
  }
  private ApiModels.BatchResult failed(String id,ApiException e){return new ApiModels.BatchResult(id,"FAILED",null,Map.of(),null,true,List.of("Processing failed: "+e.getCode()),null,new ApiModels.ErrorBody(e.code(),e.getMessage()));}
  private String hex(byte[] b){var sb=new StringBuilder();for(byte x:b)sb.append(String.format("%02x",x));return sb.toString();}
}
