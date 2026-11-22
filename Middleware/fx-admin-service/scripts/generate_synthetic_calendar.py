#!/usr/bin/env python3
"""
Generates SYNTHETIC economic calendar seed data for local and agentic-AI testing.

These are NOT published central-bank or BLS dates. Each label is prefixed [SYNTHETIC].
Rules used:
  NFP   first Friday of each month, 13:30-16:30 UTC
  FOMC  eight meetings a year on fixed week-of-year slots, 18:00-22:00 UTC
  ECB   eight meetings a year on fixed week-of-year slots, 12:45-16:45 UTC
Range: 20 years back and 5 years forward from the generation date.

FOMC dates are NOT synthetic where a source exists:
  2021-2027  Federal Reserve FOMC calendar, https://www.federalreserve.gov/monetarypolicy/fomccalendars.htm
             (decision day = last day of each meeting). Entries after the last update are marked tentative.
  2006-2020  Hugging Face dataset vtasca/fomc-statements-minutes (communications.csv), a scrape of the
             Federal Reserve site; meeting day = Date of the Statement / Scheduled Meeting rows.
Any FOMC date with no source falls back to a [SYNTHETIC] label.

Usage:  python3 generate_synthetic_calendar.py <output.json> [--today YYYY-MM-DD] [--fomc-csv communications.csv]
"""
import argparse
import datetime as dt
import json
from dateutil.relativedelta import relativedelta  # optional; falls back below

# Federal Reserve calendar, decision day of each regularly scheduled FOMC meeting (2021-2027).
FED_FOMC = [
    "2021-01-27", "2021-03-17", "2021-04-28", "2021-06-16", "2021-07-28", "2021-09-22", "2021-11-03", "2021-12-15",
    "2022-01-26", "2022-03-16", "2022-05-04", "2022-06-15", "2022-07-27", "2022-09-21", "2022-11-02", "2022-12-14",
    "2023-02-01", "2023-03-22", "2023-05-03", "2023-06-14", "2023-07-26", "2023-09-20", "2023-11-01", "2023-12-13",
    "2024-01-31", "2024-03-20", "2024-05-01", "2024-06-12", "2024-07-31", "2024-09-18", "2024-11-07", "2024-12-18",
    "2025-01-29", "2025-03-19", "2025-05-07", "2025-06-18", "2025-07-30", "2025-09-17", "2025-10-29", "2025-12-10",
    "2026-01-28", "2026-03-18", "2026-04-29", "2026-06-17", "2026-07-29", "2026-09-16", "2026-10-28", "2026-12-09",
    "2027-01-27", "2027-03-17", "2027-04-28", "2027-06-09", "2027-07-28", "2027-09-15", "2027-10-27", "2027-11-17",
]
FED_TENTATIVE_FROM = "2027-01-01"  # the Fed page says each 2027 date is tentative until confirmed

FOMC_WEEKS = [4, 11, 17, 24, 30, 37, 43, 50]
ECB_WEEKS = [5, 11, 15, 23, 27, 36, 41, 49]


def first_friday(year: int, month: int) -> dt.date:
    d = dt.date(year, month, 1)
    while d.weekday() != 4:
        d += dt.timedelta(days=1)
    return d


def week_wed(year: int, week: int) -> dt.date:
    return dt.date.fromisocalendar(year, week, 3)


def window(day: dt.date, start_h: int, start_m: int, end_h: int, end_m: int):
    s = dt.datetime(day.year, day.month, day.day, start_h, start_m, tzinfo=dt.timezone.utc)
    e = dt.datetime(day.year, day.month, day.day, end_h, end_m, tzinfo=dt.timezone.utc)
    return s, e


def csv_fomc_dates(path):
    import csv
    days = set()
    with open(path, encoding="utf-8") as f:
        for r in csv.DictReader(f):
            if r.get("Type") in ("Statement", "Scheduled Meeting"):
                days.add(dt.date.fromisoformat(r["Date"]))
    return sorted(days)


def build(today: dt.date, fomc_csv=None):
    start_year = today.year - 20
    end_year = today.year + 5
    fomc_days = {dt.date.fromisoformat(d) for d in FED_FOMC}
    if fomc_csv:
        fomc_days |= {d for d in csv_fomc_dates(fomc_csv) if d.year < 2021}
    rows = []
    for year in range(start_year, end_year + 1):
        for month in range(1, 13):
            d = first_friday(year, month)
            s, e = window(d, 13, 30, 16, 30)
            rows.append(("NFP", 1.40, s, e, f"[SYNTHETIC] US Non-Farm Payrolls {d:%b %Y}"))
        real = sorted(d for d in fomc_days if d.year == year)
        if real:
            for d in real:
                s, e = window(d, 18, 0, 22, 0)
                tag = "[TENTATIVE] " if d.isoformat() >= FED_TENTATIVE_FROM else ""
                rows.append(("FOMC", 1.35, s, e, f"{tag}FOMC rate decision {d:%b %d, %Y}"))
        else:
            for wk in FOMC_WEEKS:
                d = week_wed(year, wk)
                s, e = window(d, 18, 0, 22, 0)
                rows.append(("FOMC", 1.35, s, e, f"[SYNTHETIC] FOMC rate decision {d:%b %Y}"))
        for wk in ECB_WEEKS:
            d = week_wed(year, wk)
            s, e = window(d, 12, 45, 16, 45)
            rows.append(("ECB", 1.30, s, e, f"[SYNTHETIC] ECB monetary policy {d:%b %Y}"))
    rows.sort(key=lambda r: r[2])
    out = []
    for src, sev, s, e, label in rows:
        out.append({
            "source": src,
            "severity": sev,
            "start": s.strftime("%Y-%m-%dT%H:%M:%SZ"),
            "end": e.strftime("%Y-%m-%dT%H:%M:%SZ"),
            "label": label,
        })
    return out


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("output")
    ap.add_argument("--today", default="2026-11-22")
    ap.add_argument("--fomc-csv", default=None)
    args = ap.parse_args()
    today = dt.date.fromisoformat(args.today)
    data = build(today, args.fomc_csv)
    with open(args.output, "w") as f:
        json.dump(data, f, indent=2)
        f.write("\n")
    print(f"wrote {len(data)} synthetic events to {args.output}")
