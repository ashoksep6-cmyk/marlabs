package com.marlabs.assessment.service;

import com.marlabs.assessment.exception.ApiException;
import com.marlabs.assessment.model.ApiModels;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class ValidationService {
  public LocalDate date(String value){
    try { return LocalDate.parse(value); } catch(Exception e){ throw new ApiException("INVALID_DATE","as_of must be YYYY-MM-DD",400); }
  }
  public void question(ApiModels.AnswerRequest r){ if(r==null || r.question()==null || r.question().isBlank()) throw new ApiException("INVALID_QUESTION","question must be non-empty",400); date(r.as_of()); }
  public void manifest(ApiModels.Manifest m, Map<String,org.springframework.web.multipart.MultipartFile> files){
    if(m==null || m.batch_id()==null || m.batch_id().isBlank() || m.documents()==null) throw new ApiException("INVALID_METADATA","Invalid metadata",400);
    date(m.as_of());
    Set<String> ids=new HashSet<>(), names=new HashSet<>();
    for(var d:m.documents()){
      if(d.document_id()==null||d.document_id().isBlank()||d.filename()==null||d.filename().isBlank()||!ids.add(d.document_id())||!names.add(d.filename())) throw new ApiException("DUPLICATE_MANIFEST_ID","Manifest document_id and filename must be unique",400);
    }
    if(files.size()!=m.documents().size()) throw new ApiException("MISSING_FILE_PART","Missing or extra file parts",400);
    for(var d:m.documents()) if(!files.containsKey(d.filename())) throw new ApiException("MISSING_FILE_PART","Missing file part for manifest entry",400);
  }
}
