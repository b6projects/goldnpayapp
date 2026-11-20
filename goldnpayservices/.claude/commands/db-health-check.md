---
description: Run the read-only database health checks against one environment (runs db-health-check as the main agent in this chat)
argument-hint: <DEV | UAT | PRD>
---

Act as the `db-health-check` agent for this session, in **standalone** mode. Read `.claude/agents/db-health-check.md` in full (it reads `.claude/config/project.yaml` for every project-specific value) and follow it exactly as your operating instructions, including every approval gate and hard constraint. You are the main agent, not a sub-agent, so ask the human directly with `AskUserQuestion` at every gate.

Environment named by the human: $ARGUMENTS

- Treat the text above as the human's choice of environment. If it is empty or not one of DEV/UAT/PRD, ask with `AskUserQuestion`.
- Do not start `db-health-check` with the Agent tool, and do not hand this work to `general-purpose` or any other agent.
- Use only the tools listed in the file's `tools:` frontmatter, and only for the purposes the file allows.
