--------------------------------------------------------------------------------
:page/title All PR is good PR?
:tech-blog/published #time/ldt "2023-12-05T12:00"
:tech-blog/tags [:tag/methodology]
:tech-blog/description

Pull requests are ubiquitous, even among in-house product teams. They're so
common as to be practically synonymous with using git. Is that a good thing?
Should every change come through a pull request? I'm not so sure.

:open-graph/description

Are pull requests a good collaboration method within development teams? I'm not
so sure, and neither is the available research.

--------------------------------------------------------------------------------
:section/kind :centered
:section/theme :dark1
:section/title All PR is good PR?
:section/body

Pull requests are ubiquitous, even among in-house product teams. They're so
common as to be practically synonymous with using git. Is that a good thing?
Should every change come through a pull request? I'm not so sure.

--------------------------------------------------------------------------------
:section/body

[På norsk](https://parenteser.mattilsynet.io/pull-requests/).

I don’t quite know when it happened, but it seems like almost every development
team now coordinates their work through pull requests. I find that strange,
especially since we know that [continuous integration](/continuous-integration/)
is [highly
correlated](https://dora.dev/devops-capabilities/technical/trunk-based-development/)
with high quality, faster bug fixes, and generally good results.

Let’s dive a bit deeper and examine why many choose to abandon continuous
integration in favor of pull requests.

## Pull requests in open source

Pull requests come from the free software movement. Technically, you could say
that an email patch on a mailing list is "the O.G. pull request." Github and
similar tools have refined the user experience around contributing changes to a
codebase, offering great tools for both automated checks and manual review and
discussion of changes.

In free software, changes usually come from people outside the team primarily
responsible for the software. These contributors (fortunately) do not have
direct commit access to the source code and must go through some bureaucracy to
propose their changes. When you don’t even know the person who wrote the code,
it’s reasonable to have a check before it goes into software potentially
distributed to millions of users.

Pull requests are a workflow designed for low-trust environments. How well does
importing this workflow into team work—hopefully a high-trust
environment—actually work?

## Code review

When I ask people what they want to achieve with pull requests, code review is
the most common answer. But do you get good reviews in a pull request? It’s
possible, but I think the opposite is more common. Compare feedback on small and
large pull requests: often they get similar amounts of feedback. Sometimes small
changes get even more feedback. How can that be?

Small changes are easy to review because you can quickly take it all in and form
opinions about the code details. Feedback on small pull requests often falls
into ["bike shedding"](https://en.wikipedia.org/wiki/Law_of_triviality): "Maybe
this function should be named differently?", "Should we swap the order of these
parameters?", etc.

Large changes are very demanding to review, and since we’re busy with our own
stuff, it’s easiest to glance at it from a bird’s-eye view and say "looks good
to me!" Maybe even find a tiny detail to bike-shed about, to give the impression
you really looked into it.

Maybe you work somewhere that has better code reviews on pull requests than I
describe here. That still doesn’t help with the biggest problem with using pull
requests for code review: that the discussion happens too late.

When someone on the team has already written 500 lines of code, the bar for
pointing out that the code is solving the wrong problem, that the approach
partly duplicates existing code, or other big problems, is pretty high. In
short, that a completely different approach should have been chosen. The most
productive (and nicest) way to avoid this is to have the discussion about the
planned solution _before_ starting work.

## Less experienced developers

Some choose pull requests because there are less experienced developers on the
team who can’t/won’t/shouldn’t push directly to main. There’s no shame in being
less experienced, but being asked to create pull requests and then having all
your mistakes pointed out in writing is both unpleasant and unhelpful.

If someone on the team is not ready for continuous integration, they should
instead pair with someone else on the team until they’re confident enough. Pair
programming is a much more constructive, educational, and enjoyable way to learn
from others than written reviews.

## The cost of pull requests

Let’s say you got good code reviews from pull requests. They still come with a
big cost: they stand in the way of continuous integration. Code sits waiting
until someone has time to look at your changes.

Pull requests also increase context switching. It’s unlikely someone is ready to
review your changes immediately after you create a pull request. When you get
feedback the next day, you might already be working on something else and have
to jump back. When the pull request is finally merged, something might go wrong
in production. Then you have to drop what you’re doing and dive into potentially
days-old work to figure out what’s wrong. Not fun.

## Alternatives

So if we shouldn’t use pull requests, what should we do? Crazy suggestion:
commit code, push to main branch. But what about code review? I have two
suggestions. Some code is more important than others. The choices made at the
start of a project, or when designing new major features, are more important
than those made when adding feature #39 to an established codebase. These should
be treated differently.

When designing the data model and building or evolving the system architecture,
we should work together. That means pair programming, or—if more than two—mob
programming. There’s no good reason to let individuals handle such important
tasks alone and hope to fix it with a pull request review afterward.

Code created during pair programming is more thought-through, more solid, and
comes review-ready on paper. Plus, you build "our code" together, instead of
your and my code.

Once you’ve found a good foundation together, you can get more done in the same
time by splitting up and working on different features—or different aspects of
the same feature. How should this code be reviewed?

Imagine you’ve made some commits but haven’t pushed them to GitHub. Someone else
pushed first. Since you committed, you’ve just finished a piece of work. Perfect
timing to pull down the latest commits on main and look at others’ work. If you
see something interesting, you walk over to the person who wrote it and chat.
It’s actually possible to tweak code after it hits main too.

## Should I never use pull requests?

Pull requests don’t belong in team work. But they’re a fantastic collaboration
method for free software. Why? Context! You can’t just rip a process out of its
original context (low-trust environments), call it "best practice," and stuff it
into totally different contexts (high-trust environments) and expect the same
result. Sadly, our industry is very good at doing exactly this.

Pull requests can still have a role at work, namely between teams. This context
is similar to the natural habitat of pull requests: you want to contribute to
someone else’s codebase.

## A life without pull requests

Our small team recently launched the new
[matvaretabellen.no](https://www.matvaretabellen.no). If you look at the [commit
log](https://github.com/Mattilsynet/matvaretabellen-deux/commits?after=e9299371a774a33ce7920bc467008e259c045a93+664)
you’ll see many early commits have two committers — pair programmed code. Just
like I suggested above. But you don’t have to take my word that this works well.
There’s plenty of [research](https://cloud.google.com/devops/state-of-devops/)
supporting the positive effects of continuous integration, which is also
summarized in [a very good
book](https://www.amazon.com/Accelerate-Software-Performing-Technology-Organizations/dp/1942788339).
Also see ["trunk-based
development"](https://dora.dev/devops-capabilities/technical/trunk-based-development/)
from DORA for updated research.
