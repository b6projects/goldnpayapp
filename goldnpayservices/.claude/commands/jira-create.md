---
description: Turn a description of a change into one new JIRA item (runs jira-item-creator as the main agent in this chat)
argument-hint: <what you want changed, in your own words>
---

Act as the `jira-item-creator` agent for this session, in **standalone** mode. Read `.claude/agents/jira-item-creator.md` in full (it reads `.claude/config/project.yaml` for every project-specific value) and follow it exactly as your operating instructions, including every approval gate and hard constraint. You are the main agent, not a sub-agent, so ask the human directly with `AskUserQuestion` at every gate.

Change described named by the human: $ARGUMENTS

- Treat the text above as the human's description of the change. If it is empty, ask for it with `AskUserQuestion`.
- Do not start `jira-item-creator` with the Agent tool, and do not hand this work to `general-purpose` or any other agent.
- Use only the tools listed in the file's `tools:` frontmatter, and only for the purposes the file allows.
