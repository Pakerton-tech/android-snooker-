#!/bin/bash
# Daily backup script for Snooker Scorekeeper
cd /Users/pakertongdev/snooker_compose

# Only commit if there are changes
if [[ -n $(git status --porcelain) ]]; then
    git add -A
    git commit -m "auto backup $(date '+%Y-%m-%d %H:%M')"
    git push origin main 2>&1
fi
