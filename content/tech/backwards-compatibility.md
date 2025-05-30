--------------------------------------------------------------------------------
:page/title Stop Breaking My Stuff
:tech-blog/published #time/ldt "2023-10-20T12:00"
:tech-blog/tags [:tag/productivity]
:tech-blog/description

The IT industry seems to love redoing old things. In our endless chase for the
perfect API, we throw out things that work just fine, just to serve up something
that looks different on the surface but does basically the same thing
underneath. It’s more trouble than it’s worth.

:open-graph/description

A fire-y rant on the IT industry's complete lack of respect for developer's time
and backwards compatibility.

--------------------------------------------------------------------------------
:section/kind :centered
:section/theme :dark1
:section/title Stop Breaking My Stuff

:section/body

[På norsk](https://parenteser.mattilsynet.io/bakoverkompatibilitet/).

Have you ever stopped doing actual value-creating work to make changes to code
you were already done with—just to update a library? Or maybe you’ve deleted
`node_modules` only to discover your app won’t start anymore? Welcome to the
club. Imagine if we didn’t have to waste our time on this kind of thing.

## API improvements, or?

This week, we sat down to update
[clean-css](https://github.com/clean-css/clean-css) from version 3.x to the
latest, 5.3. That version jump should be enough to fill even the most seasoned
developer with dread. And yep, there were [breaking
changes](https://github.com/clean-css/clean-css#important-40-breaking-changes).
Let’s look at a couple:

> - splits `inliner: { request: ..., timeout: ... }` option into `inlineRequest` and `inlineTimeout`
> - renames `keepSpecialComments` to `specialComments`

This is a classic kind of change. No doubt made with the best intentions—to
improve the API. But for me as an existing user, it means combing through all
the code using the library to mirror the changes. If I'm lucky, everything still
works like it used to when I'm done.

Some might argue the changes were necessary. Maybe the old API was confusing or
inconsistent. Maybe `specialComments` is a big improvement over
`keepSpecialComments`. But none of that gave *me* any value—quite the opposite,
it just wasted my time.

And this was just a tiny library—those of you using bigger frameworks like
Rails, Spring, or React know the cold sweat that comes with a major version
bump. I once spent a month upgrading from Rails 2 to Rails 3, and I’ve seen
people give up on a Spring Boot upgrade after days of trying—on a relatively
fresh codebase.

## The cost of change

So what’s the problem with breaking changes?

Best case: you spend some time reviewing and tweaking code—code that was already
done—just to make a library work again. When you're finished, your app works
just like before. So at best, you've “just” wasted some time.

Other times, you don’t *have* time to fiddle with old code. Then you skip the
update. That could mean missing out on critical security fixes or helpful new
features.

Worst case: you make the changes, but introduce new bugs while you're at it. So
congrats, you've updated the library but broken your own solution in the
process. Been there, done that.

## Deprecation and semantic versioning

Some of you are probably shaking your heads now. "We have [semantic
versioning](https://semver.org/) to signal breaking changes!" And sure, maybe
the API I was whining about had been deprecated for ages.

But neither warnings about future breakage nor a system for announcing breakage
really help. Breaking changes are still breaking changes, and they still cost
time and energy from everyone who has to deal with them. Like [Rich Hickey
said](https://www.youtube.com/watch?v=oyLBGkS5ICk): "breaking changes are
broken."

## So how *do* I improve APIs?

Okay, so we don’t remove or change existing functionality—but we can still add
stuff. Want to change a function signature or rename something? Add a new one!
And leave the old one alone. Or have the old one call the new one.

You can even remove things from the official docs so new users don’t stumble
into APIs we’re no longer proud of.

The clean-css update I mentioned earlier was integrated in
[Optimus](https://github.com/magnars/optimus). That API was improved too—but in
a way that let existing users upgrade without changing a single line of their
own code. If Optimus can cover for clean-css like that, then clean-css could've
done the same. From Optimus’ README:

> In earlier versions of Optimus, this was a curated set of options. These old
> options will still work (we're trying not to break your stuff), but it is
> probably a good idea to take a look at all the available settings in
> clean-css.

The Optimus update consisted of:

- A new API—for the benefit of new users
- A tiny bit of code to map old options to new ones
- The old options were moved out of the spotlight in the docs and linked as
  "legacy options"

## Applications vs libraries

There’s a key difference between general-purpose libraries and application code.
In apps, you *should* clean up and improve things as you go—because it’s only
used internally by your team. You’re not messing with unknown developers out in
the wild.

But if you’re working on a library used by thousands of developers, you can’t
just change things willy-nilly to make things marginally more "correct" or
pretty. And remember—if you're building an HTTP API that your app exposes to the
world, you’re in *library mode*. No breaking stuff allowed.

## So when *can* I break the API?

Ideally? Never.

In practice, I think it’s okay to remove things that are actively harmful. For
example, GitHub Actions deprecated [a feature that made it a bit *too* easy to
leak sensitive info from
builds](https://github.blog/changelog/2022-10-11-github-actions-deprecating-save-state-and-set-output-commands/).
That kind of thing is fair game—yes, it hurts, but supporting it might hurt even
more.

## What can we do?

Next time you find yourself rewriting working code just to update a library, ask
yourself: how much more work would it be to just solve this problem myself?
Maybe you can write what you need and drop the dependency entirely. If not,
maybe you can switch to a library that respects your time more.

Personally, I've long committed to only making backward-compatible changes in my
open source projects. To make that obvious, I've stopped using semantic
versioning. I version libraries with dates instead—they're all compatible, and
the latest is just the best one *so far*.

Since I swore off breaking changes, I've also started spending more time on the
initial design of new libraries—trying them out thoroughly before documenting
and releasing them. As a result, I've rarely felt the need to make breaking
changes.

All the APIs our team builds will follow the same principle—so users can spend
time creating value instead of fiddling with things that were done last year.

You should try it too.
