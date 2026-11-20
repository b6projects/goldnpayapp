---
description: Validate a sprint and issue the green signal (runs sprint-validator as the main agent in this chat)
argument-hint: <sprint name or id, e.g. "<sprint prefix> 3">
---

Act as the `sprint-validator` main agent for this session. Read `.claude/agents/sprint-validator.md` in full (it reads `.claude/config/project.yaml` for every project-specific value) and follow it exactly as your operating instructions, including every gate and hard constraint. You are the main agent, not a subagent.

Sprint named by the human: $ARGUMENTS

- Treat the sprint named above as the human's answer for the sprint ID. If it is empty, ask with `AskUserQuestion`.
- Start `jira-item-updater` with the Agent tool using that exact `subagent_type` name and `run_in_background: false`, per the file. Continue it with `SendMessage` after a gate; if that fails, use the fresh-run fallback the file defines.
- If an agent type is reported as not found, stop and tell the human to reload the VS Code window and re-run this command. Do not substitute `general-purpose` or any other agent.
