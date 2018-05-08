## Background
This document outlines the development and participation of contributors to
Status. In particular we have found: As an open source organisation, we should
improve and further incentivize community involvement and development Status,
where people feel comfortable working on whatever they want There are Core
Contributors [CC] at Status who are paid salaries to solve particular problems.

The purpose of the document is to help core contributors understand how Status
currently organizes swarms (and their shortcomings) and steps towards improving
this process.

## Swarms: permission-less and compensated work within Status

Suppose Person A is undertaking a task, and Person B is funding it. Who should
be evaluating Person A's work?

Naturally it makes sense for Person B to, as they are assuming the risk, though
they might choose to delegate this responsibility to a trusted person.

### Current Model

In our case, Carl and Jarrad (major SNT holders) are to a large extent
delegating funding of work through Nabil (COO) and, by further extension, Status
core contributors. Decision making is done by core contributors. This makes up
the 'main Status entity', which is currently a form of 'DAO 0.5'.

So how do we currently want to evaluate work? As an entity, we can choose to
delegate this evaluation to paid core contributors as swarm leads (trusted
model). Additionally, we can choose to require PM/UX/Eng representation for
user-facing swarms, as well as a minimum of three contributors per swarm. These
are evaluation and funding mechanisms we can choose to adapt and formalize,
where 'contributor time' is a form of funding mechanism, as salary compensation
is decoupled from the swarm process.

### Shortcomings
One shortcoming of this is it does not support funding from other SNT holder and
the wider community. The evaluation and funding mechanism for this could be
improved/adapted. Similarly, requiring specific roles or minimum contributor
count is an arbitrary restriction that isn't desirable as a general compensation
mechanism for getting work done. Example: lone hacker in middle of nowhere doing
something that is a public good in Status and a funder believes they are able to
execute on by themselves. Specifically what other forms of evaluation and
funding mechanisms we want to make easy is something that we will find out as
time goes on, largely through SOB and its experiments.

What happens with work that doesn't fall neatly within existing categories?? It
depends on the nature of it. It might be completely isolated from the core app,
e.g. in the form of organizing meetups, writing content, developing a dapp that
doesn't require coordination with main code base, etc. If it does involve
changes in the core app, there's a selection mechanism where we can choose to
accept or deny such work (example: pull requests in status-react). Since we are
open source, in case of strong disagreement, one could imagine a fork and
alternative app artifact being distributed. But in the 99% case it would be in
the interest of the swarm, as well as its funder, that this work gets into the
core app. One could look at Status Core Contributors as custodians of the core
app, and this swarm would likely need to coordinate with CCs to ensure their
work has impact.

### Where to go from here?

1. Acknowledge that:

a) We will experiment with Swarm evaluation and funding mechanisms (both time
and money)

b) Swarms experiments won't be restricted by constraints outlined below in [2]

c) To the extent experiments are successful, this might impact below constraints

2. Encode the following evaluation and funding mechanism for salaried core
contributors:

a) Delegate evaluation of swarm's performance to core contributors in a swarm
(evaluation process still tbd)

b) Require PM/UX/Eng contributors for user-facing swarms

c) Require a minimum of three core contributors per swarm
