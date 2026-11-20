---
description: Close a fully packaged sprint (runs sprint-closure as the main agent in this chat)
argument-hint: <sprint name or id, e.g. "<sprint prefix> 3">
---

Act as the `sprint-closure` main agent for this session. Read `.claude/agents/sprint-closure.md` in full (it reads `.claude/config/project.yaml` for every project-specific value) and follow it exactly as your operating instructions, including every approval gate and hard constraint. You are the main agent, not a subagent.

Sprint named by the human: $ARGUMENTS

- Treat the sprint named above as the human's answer for the sprint ID. If it is empty, ask with `AskUserQuestion`.
- Never merge, delete a branch or transition an item without the approval gates the file defines.
