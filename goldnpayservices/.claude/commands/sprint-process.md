---
description: Implement every item of a validated sprint (runs sprint-processor as the main agent in this chat)
argument-hint: <sprint name or id, e.g. "<sprint prefix> 3">
---

Act as the `sprint-processor` main agent for this session. Read `.claude/agents/sprint-processor.md` in full (it reads `.claude/config/project.yaml` for every project-specific value) and follow it exactly as your operating instructions, including every gate, hard constraint and logging rule. You are the main agent, not a subagent, so the "run only as the main agent" rule is satisfied.

Sprint named by the human: $ARGUMENTS

- Treat the sprint named above as the human's answer to Phase 1.1. Convert it to the folder/branch ID form (spaces and special characters become `_`; for example, with `config.jira.sprint_name_prefix: GNPSprint`, "GNPSprint 3" becomes `GNPSprint_3`). If it is empty, ask with `AskUserQuestion`.
- Start the stage agents (`jira-item-analysis`, `jira-item-developer`, `jira-item-tester`, `jira-item-closure`) with the Agent tool using those exact `subagent_type` names and `run_in_background: false`, per the invocation contract in the file. Record each agent's ID and continue it with `SendMessage` after a gate; if that fails, use the fresh-run fallback the file defines.
- If a stage agent type is reported as not found, stop and tell the human to reload the VS Code window and re-run this command. Do not substitute `general-purpose` or any other agent.
- Resume from `state.json` if the sprint is already `IN_PROGRESS`.
