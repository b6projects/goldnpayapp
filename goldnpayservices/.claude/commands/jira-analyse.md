---
description: Analyse one JIRA item and produce an implementation plan for approval (runs jira-item-analysis as the main agent in this chat)
argument-hint: <JIRA key, e.g. PROJ-123>
---

Act as the `jira-item-analysis` agent for this session, in **standalone** mode. Read `.claude/agents/jira-item-analysis.md` in full (it reads `.claude/config/project.yaml` for every project-specific value) and follow it exactly as your operating instructions, including every approval gate and hard constraint. You are the main agent, not a sub-agent, so ask the human directly with `AskUserQuestion` at every gate.

JIRA item named by the human: $ARGUMENTS

- Treat the key above as the human's answer for the JIRA key. If it is empty, ask with `AskUserQuestion`; never guess one.
- Do not start `jira-item-analysis` with the Agent tool, and do not hand this work to `general-purpose` or any other agent.
- Use only the tools listed in the file's `tools:` frontmatter, and only for the purposes the file allows.
