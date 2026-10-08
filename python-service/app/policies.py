import json
from datetime import date
from pathlib import Path

POLICY_PATH = Path(__file__).resolve().parents[2] / "data/policies/policies.json"

def load_policies():
    with open(POLICY_PATH, encoding="utf-8") as f:
        return json.load(f)

def eligible(tenant: str, role: str, as_of: str):
    d=date.fromisoformat(as_of); rows=[]
    for p in load_policies():
        if p["tenant"] != tenant or p["permitted_role"] != role or p["approval_state"] != "Approved": continue
        if date.fromisoformat(p["effective_from"]) <= d < date.fromisoformat(p["effective_to"]): rows.append(p)
    return rows

def benefit_key(text: str):
    t=text.lower()
    if "certification" in t: return "certification"
    if "home-office" in t or "home office" in t or "desk" in t or "chair" in t: return "home-office"
    if "travel" in t or "rail" in t: return "travel"
    if "training" in t: return "training"
    if "wellness" in t or "gym" in t: return "wellness"
    return None

def relevant(rows, benefit):
    if not benefit:
        return []
    return [p for p in rows if benefit_key(p["text"]) == benefit]

def answer(question: str, as_of: str, tenant: str, role: str):
    hits=relevant(eligible(tenant,role,as_of), benefit_key(question))
    if not hits: return {"status":"INSUFFICIENT_EVIDENCE","answer":None,"citations":[]}
    citations=[{"chunk_id":p["id"],"quote":p["text"]} for p in hits]
    if len({p["text"] for p in hits}) > 1: return {"status":"CONFLICT","answer":None,"citations":citations}
    return {"status":"ANSWERED","answer":hits[0]["text"],"citations":citations}
