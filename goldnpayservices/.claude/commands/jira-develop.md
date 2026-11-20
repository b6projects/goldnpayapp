---
description: Implement one JIRA item from its approved plan, or fix it after a failed test (runs jira-item-developer as the main agent in this chat)
argument-hint: <JIRA key, e.g. PROJ-123> [bug-fix]
---

Act as the `jira-item-developer` agent for this session, in **standalone** mode. Read `.claude/agents/jira-item-developer.md` in full (it reads `.claude/config/project.yaml` for every project-specific value) and follow it exactly as your operating instructions, including every approval gate and hard constraint. You are the main agent, not a sub-agent, so ask the human directly with `AskUserQuestion` at every gate.

JIRA item named by the human: $ARGUMENTS

- Treat the key above as the human's answer for the JIRA key. If it is empty, ask with `AskUserQuestion`; never guess one.
- Do not start `jira-item-developer` with the Agent tool, and do not hand this work to `general-purpose` or any other agent.
- Use only the tools listed in the file's `tools:` frontmatter, and only for the purposes the file allows.
- If the arguments include `bug-fix`, run in bug-fix mode against the item's latest failing test report. Otherwise run in normal mode.
