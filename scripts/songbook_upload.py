#!/usr/bin/env python3
# /// script
# requires-python = ">=3.13"
# dependencies = [
#     "requests",
# ]
# ///
#
# Usage:
#   uv run scripts/songbook_upload.py <file> [options]
#
# Examples:
#   # Artist and title inferred from filename
#   uv run scripts/songbook_upload.py "Bracia Figo Fagot - Czy Kochalabys Mnie Bardziej.txt"
#
#   # Explicit artist and title
#   uv run scripts/songbook_upload.py file.txt --artist "Bracia Figo Fagot" --title "My Song"
#
#   # Dry-run to preview payload
#   uv run scripts/songbook_upload.py "Artist - Song.txt" --dry
#
#   # Custom server URL and token
#   uv run scripts/songbook_upload.py file.txt --artist "Rock" \
#     --url http://192.168.1.100:8008 --token mytoken
#
#   # Custom language, chords notation and uploader
#   uv run scripts/songbook_upload.py file.txt --artist "Classical" \
#     --language en --notation 1 --author "uploader_username"
#
# Options:
#   file              Path to Songbook .txt file (inline [chords] format)
#   -a, --artist      Artist name (inferred from filename if omitted)
#   --url             Server URL (default: http://localhost:8008)
#   --token           X-Auth-Token value (default: authtoken)
#   -l, --language    Language code (default: pl)
#   -n, --notation    Chords notation: 1=GERMAN, 2=GERMAN_IS, 3=ENGLISH,
#                     4=SOLFEGE (default: 3)
#   --author          Uploader username (optional)
#   -t, --title       Override title (default: parsed from filename)
#   -d, --dry          Print payload without sending
#   -v, --verbose     Verbose output
#
# Filename convention:
#   Artist - Title.txt  -> artist="Artist", title="Title"
#   Title.txt           -> title="Title" (no artist)
#
import argparse
import json
import os
import re
import sqlite3
import sys
import tempfile
from pathlib import Path

import requests


def parse_filename(filename: str):
    stem = Path(filename).stem
    m = re.match(r'^(.+?)[ _]-[ _](.+)$', stem)
    if m:
        artist = m.group(1).strip().replace('_', ' ').title()
        title = m.group(2).strip().replace('_', ' ').title()
        return artist, title
    return None, stem.replace('_', ' ').title()


def read_file(filepath: str) -> str:
    with open(filepath, 'r', encoding='utf-8') as f:
        return f.read()


def build_payload(filepath: str, artist: str, language: str, notation: int,
                  author: str | None, title: str) -> dict:
    content = read_file(filepath)
    payload = {
        'title': title,
        'content': content,
        'categories': [artist],
        'chords_notation': notation,
        'language': language,
        'author': author or '',
        'state': 1,
    }
    return payload


def fetch_categories_via_db(base: str, token: str, verbose: bool) -> list[str] | None:
    for url in [f'{base}/songs_db', f'{base}/api/v5/songs_db']:
        if verbose:
            print(f'  Trying songs DB at: {url}')
        headers = {'X-Auth-Token': token}
        try:
            resp = requests.get(url, headers=headers, timeout=15)
        except requests.RequestException as e:
            if verbose:
                print(f'  Connection failed: {e}')
            continue
        if not resp.ok:
            if verbose:
                print(f'  HTTP {resp.status_code}')
            continue
        try:
            tmp = tempfile.NamedTemporaryFile(delete=False, suffix='.db')
            tmp.write(resp.content)
            tmp.close()
            conn = sqlite3.connect(tmp.name)
            cur = conn.cursor()
            cur.execute("SELECT name FROM songs_category")
            rows = cur.fetchall()
            conn.close()
            os.unlink(tmp.name)
            names = [r[0] for r in rows]
            if verbose:
                print(f'  Found {len(names)} categories in songs DB')
            return names
        except Exception as e:
            if verbose:
                print(f'  Failed to parse songs DB: {e}')
            try:
                os.unlink(tmp.name)
            except Exception:
                pass
            continue
    return None


def check_category_exists(url: str, token: str, artist: str, verbose: bool):
    base = url.rstrip('/')
    candidates = [
        f'{base}/category',
        f'{base}/categories',
        f'{base}/api/v5/category',
        f'{base}/api/v5/categories',
    ]
    names = None
    for endpoint in candidates:
        if verbose:
            print(f'Checking categories at: {endpoint}')
        headers = {'X-Auth-Token': token}
        try:
            resp = requests.get(endpoint, headers=headers, timeout=5)
        except requests.RequestException as e:
            if verbose:
                print(f'  Connection failed: {e}')
            continue
        if resp.status_code == 404:
            continue
        if not resp.ok:
            if verbose:
                print(f'  HTTP {resp.status_code}')
            continue
        try:
            data = resp.json()
        except Exception:
            continue
        if isinstance(data, list):
            names = [c.get('name') if isinstance(c, dict) else str(c) for c in data]
        elif isinstance(data, dict):
            names = list(data.keys())
        if names is not None:
            break
    if names is None:
        if verbose:
            print('No JSON category endpoint found, trying songs database...')
        names = fetch_categories_via_db(base, token, verbose)
    if names is None:
        if verbose:
            print('Could not fetch category list — skipping check.')
        return
    if verbose:
        print(f'Existing categories: {len(names)}')
    if artist in names:
        print(f'Category "{artist}" already exists.')
    else:
        print(f'WARNING: Artist "{artist}" not found in existing categories. '
              f'It will be created automatically on upload.', file=sys.stderr)


def upload_payload(payload: dict, url: str, token: str, dry_run: bool, verbose: bool):
    endpoint = f'{url.rstrip("/")}/songs/songs'
    headers = {
        'X-Auth-Token': token,
        'Content-Type': 'application/json',
    }
    if verbose or dry_run:
        print(f'POST {endpoint}')
        print(f'Headers: {{"X-Auth-Token": "{token}", "Content-Type": "application/json"}}')
        print(f'Payload:')
        print(json.dumps(payload, indent=2, ensure_ascii=False))
    if dry_run:
        print('DRY-RUN: request skipped')
        return
    resp = requests.post(endpoint, headers=headers, json=payload)
    if resp.ok:
        print(f'OK (HTTP {resp.status_code}): song created')
        if verbose:
            print(json.dumps(resp.json(), indent=2, ensure_ascii=False))
    else:
        print(f'ERROR (HTTP {resp.status_code}): {resp.text}', file=sys.stderr)
        sys.exit(1)


def prompt_field(name: str, value: str) -> str:
    print(f"\n{name}: {value}")
    try:
        response = input("Do you accept? [Y/n]: ").strip().lower()
    except (EOFError, KeyboardInterrupt):
        print()
        raise
    if response in ("", "y", "yes"):
        return value
    try:
        return input(f"Enter {name}: ").strip()
    except (EOFError, KeyboardInterrupt):
        print()
        raise


def main():
    parser = argparse.ArgumentParser(
        description='Upload Songbook-format chord files to django_chords server.')
    parser.add_argument('file', help='Path to Songbook .txt file')
    parser.add_argument('--artist', '-a', help='Artist name (default: parsed from filename)')
    parser.add_argument('--url', default='http://localhost:8008',
                        help='Server URL (default: http://localhost:8008)')
    parser.add_argument('--token', default='authtoken',
                        help='X-Auth-Token value (default: authtoken)')
    parser.add_argument('--language', '-l', default='pl', help='Language code (default: pl)')
    parser.add_argument('--notation', '-n', type=int, default=3, choices=[1, 2, 3, 4],
                        help='Chords notation: 1=GERMAN, 2=GERMAN_IS, 3=ENGLISH, 4=SOLFEGE (default: 3)')
    parser.add_argument('--author', help='Uploader username (optional)')
    parser.add_argument('--title', '-t', help='Override title (default: parsed from filename)')
    parser.add_argument('--dry', '-d', action='store_true',
                        help='Print payload without sending')
    parser.add_argument('--verbose', '-v', action='store_true', help='Verbose output')
    args = parser.parse_args()

    if not os.path.isfile(args.file):
        print(f'File not found: {args.file}', file=sys.stderr)
        sys.exit(1)

    parsed_artist, parsed_title = parse_filename(args.file)

    artist = args.artist or parsed_artist
    title = args.title or parsed_title

    if not artist:
        print('No artist: provide --artist or use filename "Artist - Song.txt"',
              file=sys.stderr)
        sys.exit(1)

    action = 'DRY-RUN' if args.dry else 'Upload'
    print(f'{action}: {args.file}')

    try:
        artist = prompt_field("Artist", artist)
        title = prompt_field("Title", title)
    except (EOFError, KeyboardInterrupt):
        print('Cancelled.', file=sys.stderr)
        sys.exit(1)

    check_category_exists(args.url, args.token, artist, args.verbose)

    payload = build_payload(
        filepath=args.file,
        artist=artist,
        language=args.language,
        notation=args.notation,
        author=args.author,
        title=title,
    )
    upload_payload(payload, args.url, args.token, args.dry, args.verbose)


if __name__ == '__main__':
    main()
