# Database Health Check Queries

> Consumed by the `db-health-check` agent — see [.claude/agents/db-health-check.md](../agents/db-health-check.md). Every query below **must** be read-only (`SELECT`/`SHOW`/`DESCRIBE`/`EXPLAIN` only, one statement each). The agent validates this before running anything and skips (never "fixes") a query that fails that check.
>
> **Format**: each check is a `### <name>` heading, a one-line description, and exactly one fenced ```sql code block containing exactly one statement. Add new checks by following this same shape — the agent parses the file generically and does not special-case any check by name.
>
> Ground table and column names in the schema document at `config.docs.db_schema` (`.claude/config/project.yaml`). If the live schema has drifted from that document, update the queries here to match reality.
>
> This file is project-specific: it ships with only the connectivity probe. Add checks for this project's core tables, scheduled jobs, backlogs and feeds below it.

### Connectivity Probe
Confirms the connection itself is alive before any other check runs. Always keep this as the first check in the file.
```sql
SELECT 1 AS ok;
```

<!--
Example shapes to copy (replace table/column names with this project's own):

### Core Table Row Counts
Sanity-checks that core tables are not unexpectedly empty (e.g. after a bad migration or pointing at the wrong schema).
```sql
SELECT
  (SELECT COUNT(*) FROM <customer_table>) AS customer_count,
  (SELECT COUNT(*) FROM <order_table>) AS order_count;
```

### Pending Approval Backlog
Counts records still awaiting review.
```sql
SELECT COUNT(*) AS pending_count FROM <table> WHERE status = 'PENDING';
```
-->
