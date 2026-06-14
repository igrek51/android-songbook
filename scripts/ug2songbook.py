#!/usr/bin/env python3
"""
Ultimate Guitar -> Songbook converter.

Usage:
  python ug2songbook.py <ultimate-guitar-tab-url>

Extracts chords from a UG tab page, converts to Songbook inline format,
and prints to stdout.

Requires: cloudscraper
  uv pip install cloudscraper
"""

from __future__ import annotations

import html as html_mod
import json
import re
import sys
import time

try:
    import cloudscraper
except ImportError:
    print(
        "Missing dependency: cloudscraper\n"
        "Install with: uv pip install cloudscraper\n",
        file=sys.stderr,
    )
    sys.exit(1)


# ---------------------------------------------------------------------------
# 1. Fetch & parse
# ---------------------------------------------------------------------------

SCRAPER = cloudscraper.create_scraper(
    browser={"browser": "chrome", "platform": "windows", "desktop": True},
)


def fetch_ug_tab(url: str, max_retries: int = 3) -> str:
    """Fetch a UG tab page and return the raw wiki_tab content."""
    for attempt in range(1, max_retries + 1):
        resp = SCRAPER.get(url)
        if resp.status_code == 200:
            break
        if attempt < max_retries:
            wait = attempt * 3
            print(f"HTTP {resp.status_code}, retrying in {wait}s...", file=sys.stderr)
            time.sleep(wait)
    else:
        raise RuntimeError(f"HTTP {resp.status_code} from {url}")

    match = re.search(
        r'<div[^>]*class="js-store"[^>]*data-content="([^"]+)"', resp.text
    )
    if not match:
        raise RuntimeError("No js-store found -- page structure may have changed")

    raw = match.group(1)
    raw = html_mod.unescape(raw)
    raw = html_mod.unescape(raw)  # double-encoded entities are common on UG
    data = json.loads(raw)

    tab_view = (
        data.get("store", {}).get("page", {}).get("data", {}).get("tab_view", {})
    )
    content = tab_view.get("wiki_tab", {}).get("content")
    if content is None:
        raise RuntimeError(
            "No wiki_tab.content found (may be a Pro tab, album page, or guitar pro)"
        )
    return content


# ---------------------------------------------------------------------------
# 2. Column-aligned merge (chord-above-lyrics)
# ---------------------------------------------------------------------------

def _rendered_column(raw_pos: int, text: str) -> int:
    """Return the visual column of ``raw_pos`` in ``text`` after stripping [ch] and [/ch] tags."""
    before = text[:raw_pos]
    opens = before.count("[ch]")
    closes = before.count("[/ch]")
    return raw_pos - 4 * opens - 5 * closes


def merge_chords(chord_line: str, lyric_line: str) -> str:
    """
    Given a UG chord line (with [ch]…[/ch] tags) and the lyric line below it,
    insert the chords into the lyric at their column positions (right-to-left).

    The [ch] and [/ch] tags are invisible in the rendered view, so the column
    used for alignment is the position of the chord name minus the width of
    every tag that precedes it.

    Chords that fall past the end of the lyric line are grouped at the end.
    """
    matches = list(re.finditer(r'\[ch\](.*?)\[/ch\]', chord_line))
    if not matches:
        return lyric_line

    chords = [(_rendered_column(m.start(), chord_line), m.group(1).strip()) for m in matches]

    lyric = lyric_line.rstrip()
    lyric_len = len(lyric)
    inline = [(c, n) for c, n in chords if c < lyric_len]
    eol = [(c, n) for c, n in chords if c >= lyric_len]

    chars = list(lyric)
    for col, name in sorted(inline, key=lambda x: -x[0]):
        chars.insert(col, f"[{name}]")

    eol_names = [name for _, name in eol]
    if eol_names:
        result = "".join(chars).rstrip()
        result += "  " + " ".join(f"[{c}]" for c in eol_names)
        return result

    result = "".join(chars)

    # Post-processing: if a chord is immediately followed by a space
    # ("bez[G] gumy"), move it after the space ("bez [G]gumy").
    # A chord belongs to the word that follows it, not the one before.
    result = re.sub(r'\[(\w+)\] ', r' [\1]', result)
    return result


def process_tab_block(inner: str) -> str:
    """Process content inside [tab]…[/tab]: pairs of chord-line + lyric-line."""
    lines = inner.splitlines()
    chord_lines = []
    lyric_lines = []
    for line in lines:
        if re.search(r'\[ch\].*?\[/ch\]', line):
            chord_lines.append(line.rstrip("\r"))
        else:
            lyric_lines.append(line.rstrip("\r"))

    out: list[str] = []
    for cl, ll in zip(chord_lines, lyric_lines):
        out.append(merge_chords(cl, ll))
    if len(lyric_lines) > len(chord_lines):
        out.extend(lyric_lines[len(chord_lines) :])
    return "\n".join(out)


# ---------------------------------------------------------------------------
# 3. Cleanup helpers
# ---------------------------------------------------------------------------

TAB_BLOCK_RE = re.compile(r'\[tab\](.*?)\[/tab\]', re.DOTALL)

SECTION_HEADER_RE = re.compile(
    r"\[(?:"
    r"Verse\s*\d*|Chorus|Bridge|Intro|Outro|Solo\s*\d*|Instrumental|"
    r"Interlude|Refrain|Pre-Chorus|Pre\s*Chorus|Hook|Break|Riff|Notation|"
    r"Part\s*\d*|Middle\s*\d*|Ending|Coda|"
    r"Guitar\s*\d*|Drums\s*\d*|Bass\s*\d*|Keyboard\s*\d*|Piano\s*\d*|"
    r"Lead|Rhythm|Fill\s*\d*|N\.C\.|Intro\s*Riff|Main\s*Riff|"
    r"Zwrotka\s*\d*|Refren|Bridge"
    r")\]\s*",
    re.IGNORECASE,
)

TAB_LINE_RE = re.compile(
    r"^\s*[eBGDAbE]{1,2}\|"        # string-based tablature: e|--, B|--, G|--, D|--, A|--, E|--
)

META_LINE_RE = re.compile(
    r"^\s*(?:"
    r"(?:Written|Tabbed|Arranged|Performed|Transcribed|Copyright|All Rights).*|"
    r"(?:From the|From \"|Album).*|"
    r"(?:Tuning|Capo|Tempo|Key|Time Signature).*|"
    r"(?:Standard tuning|Tablature).*|"
    r"(?:CHORDS USED|Note:).*|"
    r"[ \t]*\d+\.\s+\w+.*(?:\(.*\)|–|by\s+).*"   # track listings
    r").*$",
    re.MULTILINE,
)

STRAY_LINE_RE = re.compile(
    r"^\s*-{3,}\s*$|"
    r"^\s*(?:see below|that'?s right|listen to|rock on|both solos?|"
    r"below is|above is|from the album|tabbed by|"
    r"note(:| about| that)|the songs for)\s",
    re.IGNORECASE,
)


LEGEND_TECH_RE = re.compile(r"^\s*\|?\s*(x|h|p|r|b|br|/|\\)\s{3,}", re.IGNORECASE)


# ---------------------------------------------------------------------------
# 4. Main conversion pipeline
# ---------------------------------------------------------------------------

def ug_to_songbook(raw: str) -> str:
    # Normalise line endings
    text = raw.replace("\r\n", "\n").replace("\r", "\n")

    # Step A: process [tab] blocks (chord-above-lyrics → inline)
    text = TAB_BLOCK_RE.sub(lambda m: process_tab_block(m.group(1)), text)

    # Step B: leftover [ch]…[/ch] → [Chord]
    text = re.sub(r'\[ch\](.*?)\[/ch\]', r'[\1]', text)

    # Step C: stray tab tags
    text = re.sub(r'\[/?tab\]', "", text)

    # Step D: remove metadata, track-listing, tablature lines
    text = META_LINE_RE.sub("", text)

    # Step E: section headers
    text = SECTION_HEADER_RE.sub("", text)

    # Step F: line-by-line filtering
    cleaned: list[str] = []
    in_legend = False
    for line in text.split("\n"):
        stripped = line.strip()

        # Toggle legend blocks (****)
        if re.match(r"^\s*\*{4,}", line):
            in_legend = not in_legend
            continue
        if in_legend:
            continue

        # Stray comments, dividers
        if STRAY_LINE_RE.match(line):
            continue

        # Tablature lines (e|--, B|--, etc.)
        if TAB_LINE_RE.match(line):
            continue

        # Legend technique lines
        if LEGEND_TECH_RE.match(line):
            continue

        cleaned.append(line)

    text = "\n".join(cleaned)

    # Step G: collapse multiple blank lines
    text = re.sub(r"\n{3,}", "\n\n", text)

    return text.strip()


# ---------------------------------------------------------------------------
# 5. CLI
# ---------------------------------------------------------------------------

def main() -> None:
    raw_mode = False
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    for a in sys.argv[1:]:
        if a == "--raw":
            raw_mode = True

    if len(args) < 1:
        print("Usage: ug2songbook.py [--raw] <ultimate-guitar-tab-url>", file=sys.stderr)
        print(file=sys.stderr)
        print("  uv run python ug2songbook.py '<url>'", file=sys.stderr)
        print("  uv run python ug2songbook.py --raw '<url>'", file=sys.stderr)
        print(file=sys.stderr)
        print("Examples:", file=sys.stderr)
        print(
            "  ug2songbook.py 'https://tabs.ultimate-guitar.com/tab/"
            "the-beatles/let-it-be-chords-60690'",
            file=sys.stderr,
        )
        sys.exit(1)

    url = args[0]

    print(f"Fetching: {url}", file=sys.stderr)
    try:
        raw = fetch_ug_tab(url)
    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)

    if raw_mode:
        print(raw)
    else:
        print(f"Converting ({len(raw)} chars)...", file=sys.stderr)
        result = ug_to_songbook(raw)
        print(result)


if __name__ == "__main__":
    main()
