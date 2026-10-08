package com.marlabs.assessment.model;

import java.util.List;
import java.util.Map;

public final class ApiModels {
  private ApiModels() {}
  public record AnswerRequest(String question, String as_of) {}
  public record Citation(String chunk_id, String quote) {}
  public record AnswerResponse(String status, String answer, List<Citation> citations) {}
  public record Manifest(String batch_id, String as_of, List<DocumentEntry> documents) {}
  public record DocumentEntry(String document_id, String filename) {}
  public record Extracted(String benefit, Double amount, String currency, String reference) {}
  public record BatchResult(String document_id, String processing_status, Extracted extracted,
                            Map<String,List<String>> field_evidence, AnswerResponse policy_outcome,
                            boolean review_required, List<String> review_issues, String duplicate_of, ErrorBody error) {}
  public record ErrorBody(String code, String message) {}
  public record BatchResponse(String batch_id, Summary summary, List<BatchResult> results) {}
  public record Summary(int total, int completed, int failed) {}
  public record PythonAnswerRequest(String question, String as_of, String tenant, String role) {}
  public record PythonAnswerResponse(String status, String answer, List<Citation> citations) {}
  public record PythonExtractRequest(String document_id, String filename, String content_base64, String as_of, String tenant, String role) {}
  public record PythonExtractResponse(Extracted extracted, Map<String,List<String>> field_evidence, AnswerResponse policy_outcome, List<String> review_issues) {}
}
