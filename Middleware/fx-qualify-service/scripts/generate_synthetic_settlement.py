#!/usr/bin/env python3
"""
SYNTHETIC settlement policy seed for local and agentic-AI testing. Not business-approved values.
G10 pairs: CLS eligible, cutoff 23:55 UTC (SPOT) / 16:00 UTC (TOM), clips 100k to 50m (SPOT) / 25m (TOM).
EM pairs: not CLS eligible (NDF rules), cutoff 15:00 UTC, clips 100k to 10m.
Restricted per the synthetic spec: USDINR and USDBRL.
Usage: python3 generate_synthetic_settlement.py <pairs.json> <output.json>
"""
import json
import sys

EM = {"USDMXN", "USDSGD", "USDHKD", "USDZAR", "USDTRY", "USDCNH", "USDINR", "USDBRL", "USDKRW", "USDPLN"}
RESTRICTED = {"USDINR", "USDBRL"}


def rows(pair_code):
    if pair_code in EM:
        return [
            {"ccyPair": pair_code, "tenor": "SPOT", "clsEligible": False, "cutoffTimeUtc": "15:00:00",
             "minClipUsd": 100000.00, "maxClipUsd": 10000000.00, "restrictedFlag": pair_code in RESTRICTED},
        ]
    return [
        {"ccyPair": pair_code, "tenor": "SPOT", "clsEligible": True, "cutoffTimeUtc": "23:55:00",
         "minClipUsd": 100000.00, "maxClipUsd": 50000000.00, "restrictedFlag": False},
        {"ccyPair": pair_code, "tenor": "TOM", "clsEligible": True, "cutoffTimeUtc": "16:00:00",
         "minClipUsd": 100000.00, "maxClipUsd": 25000000.00, "restrictedFlag": False},
    ]


if __name__ == "__main__":
    with open(sys.argv[1]) as f:
        pairs = json.load(f)
    out = []
    for p in pairs:
        out.extend(rows(p["pairCode"]))
    with open(sys.argv[2], "w") as f:
        json.dump(out, f, indent=2)
        f.write("\n")
    print(f"wrote {len(out)} synthetic settlement rows")
