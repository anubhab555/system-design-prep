# HLD Documentation Process

This file defines the process for converting Excalidraw HLD panels into Markdown notes in this repository.

## Purpose

Keep HLD documentation consistent across the `HLD/` directory by:
- Mirroring the precise text and flow shown in Excalidraw diagrams
- Using Mermaid diagrams for architecture and flow when a drawing is present
- Making diagrams readable in both dark mode and light mode
- Providing a reusable process so contributors do not need to repeat these instructions

## Naming conventions

- Use a descriptive markdown filename for each HLD concept, e.g. `TinyURL.md`.
- Pair the markdown with the corresponding Excalidraw file, e.g. `TinyURL_HLD_canvas.excalidraw`.

## Markdown structure

Each HLD markdown should include these sections in this order:

1. Problem
2. Requirements
3. Scale
4. API contract
5. Data model
6. Design notes
7. Architecture / core flow
8. Trade-offs and risks
9. Failure handling
10. Interview talking points
11. Excalidraw reference

## Diagram conversion process

When a diagram exists, convert it into Mermaid by:

1. Translating drawn boxes and labels into section headings and bullets.
2. Converting drawn flow arrows into Mermaid flowcharts or sequence diagrams.
3. Using dark-mode friendly theme variables at the top of each Mermaid block:

```mermaid
%%{init: {'theme': 'base','themeVariables': {'primaryColor': '#0a84ff','secondaryColor': '#252526','tertiaryColor': '#1e1e1e','lineColor': '#d4d4d4','textColor': '#d4d4d4','mainBkg': '#1e1e1e','clusterBkg': '#252526','clusterBorder': '#3c3c3c'}}}%%
```

4. Keeping text high contrast and avoiding pale colors that are hard to read in dark mode.

## Content rules

- Do not invent extra architecture that is not visible in the Excalidraw diagram.
- Keep the HLD focused on the drawn concepts and any interview call-outs implied by the diagram.
- Preserve exact wording from the diagram when possible.
- Add supporting context only when it clarifies the drawn design.

## Excalidraw reference

Always include the Excalidraw file name at the bottom of the markdown so reviewers know where the source diagram is located.
