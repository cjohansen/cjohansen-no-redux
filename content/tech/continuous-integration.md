--------------------------------------------------------------------------------
:page/title Continuously Indignant
:tech-blog/published #time/ldt "2023-12-08T12:00"
:tech-blog/tags [:tag/methodology]
:tech-blog/description

Do pull requests conflict with continuous integration? Is continuous integration
the same as CI? What about CI/CD? I try to clarify some confusion from my
previous post about pull requests.

:open-graph/description

Do pull requests conflict with continuous integration? We need some definitions
to answer that.

--------------------------------------------------------------------------------
:section/kind :centered
:section/theme :dark1
:section/title Continuously Indignant
:section/body

Do pull requests conflict with continuous integration? Is continuous integration
the same as CI? What about CI/CD? I try to clarify some confusion from my
previous post about pull requests.

--------------------------------------------------------------------------------
:section/body

[På norsk](https://parenteser.mattilsynet.io/kontinuerlig-integrasjon/).

To discuss the extent to which pull requests and continuous integration go
together, it’s useful to agree on some definitions. As usual, authoritative
definitions are hard to find, so the best I can do is explain what they mean to
me and which sources have informed my understanding.

## Continuous Integration

Continuous integration—or "CI" for short—is the process where all developers
working on the same codebase continuously merge their changes together. The term
appeared as early as 1991, but the expectations for "continuous" have changed
dramatically since then. The extreme programming (XP) movement embraced the
term, making clear that "continuous" means "many times per day."

At its core, this is it: a workflow that strives for everyone to have as similar
a version of the code on their machines as possible. The goal is to avoid
spending time on merge conflicts, avoid duplicated work, share a common
understanding of reality, enable faster exchange of work ("I’m using the
function you wrote earlier in this feature now!"), and facilitate getting things
faster into production.

## What about my CI system?

Ready for another IT industry classic? Many tool vendors have also embraced "CI"
and eagerly sell you CI systems. The result is that for many, continuous
integration/CI simply means "automated builds."

Systems like GitHub Actions can help teams automate their workflow, but they do
not themselves constitute continuous integration. You cannot buy or install CI
as a product any more than you can buy or install "agile."

A workflow where everyone works on their own branches, regularly gets merge
conflicts, and rarely delivers to production does not become "CI" just because
you add automated builds.

## CI/CD?

Some systems don’t stop at just being CI. They are CD too. CD stands for
"continuous delivery"—another process that many confuse with a system you can
buy.

Continuous delivery (CD) is the natural extension of continuous integration.
When all the code is merged continuously, we can also deliver continuously. CD
is about moving from milestone deliveries ("production release at the end of
each sprint," "release once a month," etc.) to continuous deliveries—to the
production environment. Many times per day. Preferably via automated builds.

Again: you don’t get continuous delivery by setting up a bunch of automated
processes that update your test environment. Continuous delivery is the process
of regularly delivering to your users.

## What about pull requests then?

Is it possible to combine PR with continuous integration? The very definition of
this workflow is that everyone’s changes are merged continuously. When you
commit to a branch and merge it to main via a PR, you by definition do not have
continuous integration.

CI is not necessarily either-or; it’s more of a spectrum. As a compromise, I can
agree that if your PRs come from branches that on average live less than one day
and do not require manual follow-up, then you are pretty close to the goal. But
I still think you would experience better flow without PRs, though that’s a
topic for another blog post.

In conclusion, I recommend [The DevOps
Handbook](https://www.amazon.com/DevOps-Handbook-World-Class-Reliability-Organizations/dp/1950508404),
a book that goes deeper into these concepts, and especially
[Accelerate](https://www.amazon.com/Accelerate-Software-Performing-Technology-Organizations/dp/1942788339),
which shows how research (!) demonstrates that they deliver good results on
multiple axes. See ["trunk-based
development"](https://dora.dev/devops-capabilities/technical/trunk-based-development/)
from DORA for updated research.
