# JIRA Item Validation Template

> Used by `jira-item-updater`, `jira-item-creator` and `jira-item-analysis` to judge whether a JIRA item is complete. The required/optional split comes from `config.jira.fields` in `.claude/config/project.yaml`; this template defines what "complete" means for each field.

A field is **PRESENT** when it is non-empty and not a placeholder (not literally "TBD", "N/A", "-", or the field's own name). Otherwise it is **MISSING**.

| Field | Expected content | How a missing value may be filled |
|---|---|---|
| Summary | One line, action-oriented, names the area affected | Never auto-filled — the human must supply it |
| Description | Context, current behaviour, desired behaviour, scope boundaries | Judgment-required — only from text already on the issue |
| Acceptance Criteria | Numbered, testable statements (Given/When/Then or equivalent) | Judgment-required — only from text already on the issue |
| Priority | One of the project's priority values | Never guessed — ask the human |
| Dependencies | Other work this item needs first, or "None" | Derivable — restate existing issue links |
| Linked Issues | Related/blocking issues, or "None" | Derivable — restate existing issue links |
| Business Requirement | Why the business needs this; who benefits | Judgment-required — only from text already on the issue |
| Technical Requirement | Constraints, components, non-functional needs, or "None" | Judgment-required — only from text already on the issue |

If nothing on the issue grounds a judgment-required field, mark it `CANNOT_AUTO_FILL`.
