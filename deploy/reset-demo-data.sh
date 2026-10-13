#!/bin/sh
# Puts the demo data back as it was first seeded: empties the database, then
# restarts the API, which re-creates the tables and demo accounts on start-up.
# Claims, receipts and approvals made since are lost.
set -eu

cd "$(dirname "$0")"

docker compose exec -T db psql -v ON_ERROR_STOP=1 -U expenses -d expenses \
    -c 'DROP SCHEMA public CASCADE; CREATE SCHEMA public;'
docker compose restart api

echo "Demo data reset. The API is restarting; give it about a minute."
