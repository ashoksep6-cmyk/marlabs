from app.extraction import extract_fields, process

def test_ambiguous_amount():
    f,e,i=extract_fields("Reference: CERT-303\nCertification reimbursement request. The invoice says INR 22000. My reimbursement form says INR 28000.")
    assert f["benefit"]=="certification" and f["amount"] is None
    assert any("ambiguous" in x for x in i)

def test_request_07_missing_amount():
    f,e,o,i=process("request-07.txt",b"Reference: TRAIN-707\nI have already booked external training and now want reimbursement. I have not obtained manager approval.","2026-09-21","Atlas","employee")
    assert f["benefit"]=="training" and f["amount"] is None
    assert any("missing" in x.lower() for x in i)
