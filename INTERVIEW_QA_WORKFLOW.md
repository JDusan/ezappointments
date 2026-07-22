# Interview Q&A Workflow

## Summary

- Answer each Java Medior question in a concise
- Save generated markdown notes in `C:\Users\duja0807\Desktop\Svasta\privatno\priprema_pitanja`.

## Response Rules

- For each new question, give a short, direct answer first.
- Do not broaden into long theory unless the user asks for more detail.
- After answering, the user may:
  - ask for a deeper explanation
  - ask for a markdown file
  - move to the next question
- If the user asks a new question without requesting a markdown file for the previous one, treat the previous question as pending for markdown generation.

## Markdown File Spec

- Use a short, lowercase, hyphen-separated filename such as `hashmap-vs-hashtable.md`.
- Use this structure:

```md
# <full question>

## Short Version

- concise bullet
- concise bullet

## Full Answer

Tailored, fuller spoken-style answer.
```

## Working Defaults

- If a question is ambiguous, answer the most common Java interview interpretation first and state the assumption briefly.
- If the topic benefits from an example, include only one small example unless the user asks for more.
- If the answer starts getting too long, compress it rather than broadening it.
- If several questions accumulate, recommend starting a new chat when that becomes more token-efficient and provide a restart prompt that preserves the workflow.

## Behavior Cases

- One question, then detail request: expand the same answer without widening scope unnecessarily.
- One question, then markdown request: generate one markdown file using the template above.
- Question A, then Question B: answer Question B concisely and keep Question A pending for markdown generation.
- Broad topic such as Spring transactions: answer narrowly for interview use, not as a tutorial.

## Assumptions

- These markdown files are prep notes, not project documentation.
- Answer in English unless the user asks otherwise.
- Do not run builds or tests as part of this workflow.
