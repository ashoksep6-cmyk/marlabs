package com.marlabs.assessment;
import com.marlabs.assessment.service.ValidationService; import org.junit.jupiter.api.Test; import static org.junit.jupiter.api.Assertions.*;
class ValidationServiceTest { @Test void invalidDate(){assertThrows(RuntimeException.class,()->new ValidationService().date("21-09-2026"));} @Test void invalidQuestion(){assertThrows(RuntimeException.class,()->new ValidationService().question(new com.marlabs.assessment.model.ApiModels.AnswerRequest("","2026-09-21")));} }
