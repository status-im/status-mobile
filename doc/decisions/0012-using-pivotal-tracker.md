Using Pivotal Tracker
===

## Abstract

The Core Improvements swarm tracks its workflow in a Pivotal project. Although GitHub items can still be created to facilitate communication, the official way to track progress of a functionality/bug fix is in Pivotal.

[Project board link](https://www.pivotaltracker.com/n/projects/2232205)

## Creating Pivotal Stories

Fill in `Story Type`, `Requester`, `Owner` (if already assigned), and `Points` (if an estimate is available). Add a description and optionally group it using a label. As a rule of thumb, Pivotal stories should be grouped in epics for better high-level visibility. Although labels can be used to group stories, the prescribed way to do that is through epics.

You can add tasks under a Story for smaller items that don't warrant its own Story.

Do add the link to the Pull Request to the Story when one is available.

## Lifetime of a Pivotal story

The `State` field in the Pivotal story is used to track the progress of a Pivotal Story, and has the following interpretation:

| State      | Meaning  |
| ---------- | -------- |
| `Unstarted`  | In backlog     |
| `Started`    | Developer has started work on story     |
| `Finished`   | Developer has finished work on story (Pull Request for story is in [REVIEW](https://github.com/status-im/status-react/projects/7#column-1843024) column in [Pipeline for QA](https://github.com/status-im/status-react/projects/7) board) |
| `Delivered`  | Pull Request for story is in testing in [Pipeline for QA](https://github.com/status-im/status-react/projects/7) board) board     |
| `Accepted`   | PR has been merged     |
| `Rejected`   | PR has been aborted     |

### Rules of thumb

- **Q:** _When to create a Pivotal story?_
  **A:** Stories should be created whenever estimated work exceeds half a day, or when part of a bigger epic.

- **Q:** _What do points equate to?_
  **A:** We've agreed to make 3 points equate roughly to 1 day of work. From there, we can extrapolate:

  | Points   | Estimated Time   |
  | -------- | ---------------- |
  | 0 points | < 1 hour         |
  | 1 points | 1 hour - 0.5 days|
  | 2 points | 0.5 - 1 days     |
  | 3 points | ~1 day           |
  | 5 points | 2 - 3 days       |
  | 8 points | ~1 week          |

  We should avoid 8 point stories by breaking them down into smaller stories as much as possible.