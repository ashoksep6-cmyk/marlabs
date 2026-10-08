import base64
from fastapi import FastAPI, HTTPException
from .models import AnswerRequest, AnswerResponse, ExtractRequest, ExtractResponse
from .policies import answer
from .extraction import process
app=FastAPI(title="Marlabs Policy Provider", version="1.0.0")

@app.get("/health")
def health(): return {"status":"UP"}

@app.post("/internal/answer",response_model=AnswerResponse)
def internal_answer(req: AnswerRequest):
    try: return answer(req.question,req.as_of,req.tenant,req.role)
    except ValueError: raise HTTPException(400,"INVALID_DATE")

@app.post("/internal/extract",response_model=ExtractResponse)
def internal_extract(req: ExtractRequest):
    try:
        raw=base64.b64decode(req.content_base64,validate=True)
        fields,evidence,outcome,issues=process(req.filename,raw,req.as_of,req.tenant,req.role)
        return {"extracted":fields,"field_evidence":evidence,"policy_outcome":outcome,"review_issues":issues}
    except ValueError as e:
        code=str(e)
        status=422 if code in {"EMPTY_FILE","UNREADABLE_FILE"} else 400
        raise HTTPException(status,code)
