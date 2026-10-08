import base64, re
from io import BytesIO
from pypdf import PdfReader
from .policies import answer, benefit_key

REFERENCE_RE=re.compile(r"Reference:\s*([A-Za-z0-9-]+)",re.I)
MONEY_RE=re.compile(r"(?:INR|Rs\.?|₹)\s*([0-9]+(?:\.[0-9]+)?)",re.I)

def extract_text(filename: str, raw: bytes):
    if not raw: raise ValueError("EMPTY_FILE")
    if filename.lower().endswith(".txt"):
        return raw.decode("utf-8")
    if filename.lower().endswith(".pdf"):
        reader=PdfReader(BytesIO(raw)); text="\n".join((p.extract_text() or "") for p in reader.pages)
        if not text.strip(): raise ValueError("UNREADABLE_FILE")
        return text
    raise ValueError("UNREADABLE_FILE")

def extract_fields(text: str):
    benefit=benefit_key(text)
    amounts=MONEY_RE.findall(text)
    reference=(REFERENCE_RE.search(text).group(1) if REFERENCE_RE.search(text) else None)
    currency="INR" if amounts else None
    amount=float(amounts[0]) if len(amounts)==1 else None
    issues=[]
    if not benefit: issues.append("Benefit could not be identified")
    if len(amounts)>1: issues.append("Amount is ambiguous because multiple amounts are present")
    if amount is None: issues.append("Amount is missing or unresolved")
    if not currency: issues.append("Currency is missing or unresolved")
    if not reference: issues.append("Reference is missing")
    evidence={"benefit":[],"amount":[],"currency":[],"reference":[]}
    for line in text.splitlines():
        s=line.strip()
        if not s: continue
        low=s.lower()
        if benefit and benefit_key(s)==benefit: evidence["benefit"].append(s)
        if any(a in s for a in amounts): evidence["amount"].append(s)
        if "inr" in low or "₹" in s: evidence["currency"].append(s)
        if reference and reference in s: evidence["reference"].append(s)
    return {"benefit":benefit,"amount":amount,"currency":currency,"reference":reference}, evidence, issues

def process(filename, raw, as_of, tenant, role):
    text=extract_text(filename,raw); fields,evidence,issues=extract_fields(text)
    issues.insert(0,"Human review is always required; the system does not approve or pay claims")
    outcome=None
    if fields["benefit"]:
        outcome=answer(fields["benefit"],as_of,tenant,role)
        if outcome["status"] == "INSUFFICIENT_EVIDENCE": issues.append("No eligible approved policy evidence was found for the requested benefit")
        elif outcome["status"] == "CONFLICT": issues.append("Multiple eligible approved policies disagree; no precedence rule is supplied")
        # Request text is deliberately never passed into policy corpus and never changes caller context.
        if fields["amount"] is None: issues.append("Payable amount cannot be determined")
    return fields,evidence,outcome,issues
