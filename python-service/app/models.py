from pydantic import BaseModel
from typing import Optional

class Citation(BaseModel):
    chunk_id: str
    quote: str
class AnswerResponse(BaseModel):
    status: str
    answer: Optional[str] = None
    citations: list[Citation] = []
class Extracted(BaseModel):
    benefit: Optional[str] = None
    amount: Optional[float] = None
    currency: Optional[str] = None
    reference: Optional[str] = None
class AnswerRequest(BaseModel):
    question: str
    as_of: str
    tenant: str
    role: str
class ExtractRequest(BaseModel):
    document_id: str
    filename: str
    content_base64: str
    as_of: str
    tenant: str
    role: str
class ExtractResponse(BaseModel):
    extracted: Extracted
    field_evidence: dict[str,list[str]]
    policy_outcome: Optional[AnswerResponse]
    review_issues: list[str]
