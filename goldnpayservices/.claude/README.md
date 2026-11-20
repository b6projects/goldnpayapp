# Claude SDLC agents — reusable setup

This `.claude/` folder holds a generic, JIRA-driven SDLC agent set. All project-specific values live in **`config/project.yaml`**; the agents and commands contain none. To reuse it in another repository, copy the whole folder and edit that one file.

## Layout
```
.claude/
├── config/project.yaml          # ← the only project-specific file (plus resources/ and projectdoc/)
├── agents/                      # 10 generic agents
├── commands/                    # 10 slash commands, one per agent
├── templates/                   # analysis plan, JIRA validation, test report shapes
├── resources/dbhealthchecksql.md# read-only DB health-check queries (project-specific content)
├── skills/                      # SKILL.md folders the agents load (config.paths.skills_dir) — copy in
├── projectdoc/                  # FunctionalDetails.md, TechnicalDetails.md, dbstructure.md — create
└── settings.json                # tool permissions
```

## Pipeline
| Command | Agent | What it does | JIRA status it sets |
|---|---|---|---|
| `/jira-create <text>` | jira-item-creator | Free text → one approved new JIRA item | — |
| `/jira-update <KEY>` | jira-item-updater | Fill missing required fields (approval-gated) | — |
| `/jira-analyse <KEY>` | jira-item-analysis | Implementation plan for approval | Analysed |
| `/jira-develop <KEY> [bug-fix]` | jira-item-developer | Implement the approved plan, build modules | Developed |
| `/jira-test <KEY>` | jira-item-tester | Tests, standards + security review, package check | Testing → Done / Bug Fix |
| `/jira-close <KEY>` | jira-item-closure | Commit (and merge) after a fresh report | Packaged / Released |
| `/sprint-validate <sprint>` | sprint-validator | Prechecks, item order, sprint branch, green signal | — |
| `/sprint-process <sprint>` | sprint-processor | Drives analysis → dev → test → closure per item | (via stage agents) |
| `/sprint-close <sprint>` | sprint-closure | Branch audit, sprint→source merge, cleanup | Released |
| `/db-health-check <ENV>` | db-health-check | Read-only DB diagnostics report | — |

## Setup checklist for a new project
1. **Fill in `config/project.yaml`** — every `# TODO`. At minimum: `jira.project_key`, `jira.sprint_name_prefix`, `jira.statuses` (must match your JIRA workflow exactly), `git.source_branch`, and each module under `testing.modules` (`stack`, `build`, `unit_test`, `package`, `artifacts`). Agents stop and name any missing key rather than guess.
2. **Add stack profiles** — `stacks:` ships with `spring-boot` and `react`. Add one for any other stack a module uses (layering, skills, standards/security checklists, artifact checks, runtime-fit rule).
3. **Copy the skills** into `config.paths.skills_dir` (default `.claude/skills/<skill>/SKILL.md`). Missing skills don't block a run — the agent notes it and follows its own rules — but they carry the detailed conventions.
4. **Write the project docs** at `config.docs.*`. `jira-item-creator` will not run without the functional and technical docs.
5. **Allow your build commands** in `settings.json` → `permissions.allow` (e.g. `Bash(./mvnw test:*)`, `Bash(npm run build:*)`), so foreground sub-agents aren't silently denied during `/sprint-process`.
6. **Gitignore** `.claude-workspace/` (agents' working area) and decide whether `ai-reports/` is tracked.
7. **JIRA workflow** must contain the statuses in `jira.statuses`; agents pick transitions by target status name only.
8. **DB health check**: fill `database:` and add checks to `resources/dbhealthchecksql.md`; set each environment's `password_env_var` in your shell.

## Notes
- The JIRA tools in each agent's `tools:` frontmatter use the Atlassian Rovo connector names (`mcp__claude_ai_Atlassian_Rovo__*`). Frontmatter can't read config, so if your connector is named differently, update the `tools:` lines.
- `.claude-workspace/` and `ai-reports/` are fixed conventions, not config values.
