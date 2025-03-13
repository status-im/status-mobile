# 1. Record Decisions

| Date       | Tags    |
|------------|---------|
| 2018-02-16 | process |

## Status

Proposed

## Context

We make a lot of decisions during the life of a project and
documenting those decisions would help new team members and outside
contributors follow our thinking. It also opens up an opportunity for
constructive criticism around those decisions which will result
in better decision making.

We want to develop a product in the open. Opening up our decision making process for outside contributors is an important part in providing the opportunity to take ownership and impact the project in non-trivial ways.

Recording decisions will also be a useful form of documentation in general as it does not get out of date. All that's documented is that a decision has been made at a certain point in time.

## Decision

We will adopt a format similar to [Architecture Decision
Records](http://thinkrelevance.com/blog/2011/11/15/documenting-architecture-decisions) (ADR)
to propose and document notable decisions. In contrast to ADRs we will try to embrace this tool for more than just architectural concerns. Decisions regarding development practices and product direction are just as important.

What decisions are notable and which are not is left to common-sense and informal consensus among the team.

Decisions may be proposed and discussed informally, should however eventually end up — with all relevant discussion summarized — in the `doc/decisions/` directory of this repository.

Decisions are numbered to have a clear identifier.

## Consequences

- We need to document that we record decisions this way and where people can find those documents.
- We should probably provide some pointers around tooling to better work with decision records. [`adr-tools`](https://github.com/npryce/adr-tools) comes to mind.
- Since we deviate from ADRs a bit (we document more than architecture-related decisions) we might need to provide different templates than what comes with tools like `adr-tools` by default.
  A template like this may also be useful to guide decision authors to provide appropriate context.
