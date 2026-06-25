#!/usr/bin/env bash
set -euo pipefail

csv="${1:?JaCoCo aggregate CSV path required}"
min="${2:?Minimum line coverage ratio required}"

if [[ ! -s "$csv" ]]; then
  echo "Missing JaCoCo aggregate report: $csv" >&2
  echo "Run full reactor verify with -Pcoverage (for example: mvn verify -Pcoverage)." >&2
  exit 1
fi

line_count="$(awk 'END { print NR }' "$csv")"
if (( line_count <= 1 )); then
  echo "JaCoCo aggregate report has no class rows: $csv" >&2
  exit 1
fi

ratio="$(awk -F, 'NR > 1 { missed += $8; covered += $9 } END { if (missed + covered == 0) print 0; else print covered / (missed + covered) }' "$csv")"

awk -v ratio="$ratio" -v min="$min" 'BEGIN {
  if (ratio + 0 < min + 0) {
    printf "Aggregate line coverage %.2f%% is below minimum %.2f%%\n", ratio * 100, min * 100 > "/dev/stderr"
    exit 1
  }
  printf "Aggregate line coverage %.2f%% (minimum %.2f%%)\n", ratio * 100, min * 100
}'
