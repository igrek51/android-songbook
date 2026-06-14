#!/usr/bin/env python3
# /// script
# requires-python = ">=3.13"
# dependencies = [
#     "requests",
# ]
# ///
#
# Usage:
#   uv run scripts/songbook_upload.py <file> -c <category> [-c <category> ...]
#
# Examples:
#   # Single category, parsed filename
#   uv run scripts/songbook_upload.py Artist_-_Song.txt -c "Rock"
#
#   # Override title/author, multiple categories
#   uv run scripts/songbook_upload.py file.txt -c "Jazz" -c "Live" \
#     --title "My Song" --author "Me"
#
#   # Dry-run to preview payload
#   uv run scripts/songbook_upload.py Artist_-_Song.txt -c "Pop" --dry-run
#
#   # Custom server URL and token
#   uv run scripts/songbook_upload.py file.txt -c "Rock" \
#     --url http://192.168.1.100:8008 --token mytoken
#
#   # Custom language and chords notation
#   uv run scripts/songbook_upload.py file.txt -c "Classical" \
#     --language en --notation 1
#
# Options:
#   file              Path to Songbook .txt file (inline [chords] format)
#   -c, --category    Category name (required, repeatable)
#   --url             Server URL (default: http://localhost:8008)
#   --token           X-Auth-Token value (default: authtoken)
#   -l, --language    Language code (default: pl)
#   -n, --notation    Chords notation: 1=GERMAN, 2=GERMAN_IS, 3=ENGLISH,
#                     4=SOLFEGE (default: 3)
#   -a, --author      Override author (default: parsed from filename)
#   -t, --title       Override title (default: parsed from filename)
#   -d, --dry-run     Print payload without sending
#   -v, --verbose     Verbose output
#
# Filename convention:
#   Artist_-_Song_Title.txt  -> author="Artist", title="Song Title"
#   Song_Title.txt           -> title="Song Title" (no author)
#
import argparse
import json
import os
import re
import sys
from pathlib import Path

import requests


def parse_filename(filename: str):
    stem = Path(filename).stem
    m = re.match(r'^(.+?)_-_(.+)$', stem)
    if m:
        author = m.group(1).strip().replace('_', ' ')
        title = m.group(2).strip().replace('_', ' ')
        return author, title
    return None, stem.replace('_', ' ')


def read_file(filepath: str) -> str:
    with open(filepath, 'r', encoding='utf-8') as f:
        return f.read()


def build_payload(filepath: str, categories: list[str], language: str, notation: int,
                  author: str | None, title: str | None) -> dict:
    content = read_file(filepath)
    parsed_author, parsed_title = parse_filename(filepath)
    payload = {
        'title': title or parsed_title,
        'content': content,
        'categories': categories,
        'chords_notation': notation,
        'language': language,
        'author': author or parsed_author or '',
        'state': 1,
    }
    return payload


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


def main():
    parser = argparse.ArgumentParser(
        description='Upload Songbook-format chord files to django_chords server.')
    parser.add_argument('file', help='Path to Songbook .txt file')
    parser.add_argument('--url', default='http://localhost:8008',
                        help='Server URL (default: http://localhost:8008)')
    parser.add_argument('--token', default='authtoken',
                        help='X-Auth-Token value (default: authtoken)')
    parser.add_argument('--category', '-c', action='append', required=True,
                        dest='categories', help='Category name (can be repeated, at least one required)')
    parser.add_argument('--language', '-l', default='pl', help='Language code (default: pl)')
    parser.add_argument('--notation', '-n', type=int, default=3, choices=[1, 2, 3, 4],
                        help='Chords notation: 1=GERMAN, 2=GERMAN_IS, 3=ENGLISH, 4=SOLFEGE (default: 3)')
    parser.add_argument('--author', '-a', help='Override author (default: parsed from filename)')
    parser.add_argument('--title', '-t', help='Override title (default: parsed from filename)')
    parser.add_argument('--dry-run', '-d', action='store_true',
                        help='Print payload without sending')
    parser.add_argument('--verbose', '-v', action='store_true', help='Verbose output')
    args = parser.parse_args()

    if not os.path.isfile(args.file):
        print(f'File not found: {args.file}', file=sys.stderr)
        sys.exit(1)

    payload = build_payload(
        filepath=args.file,
        categories=args.categories,
        language=args.language,
        notation=args.notation,
        author=args.author,
        title=args.title,
    )
    upload_payload(payload, args.url, args.token, args.dry_run, args.verbose)


if __name__ == '__main__':
    main()
