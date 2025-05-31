--------------------------------------------------------------------------------
:page/title On keys and their usage
:tech-blog/published #time/ldt "2024-02-06T12:00"
:tech-blog/tags [:tag/clojure :tag/datomic]
:tech-blog/description

In Clojure, you can namespace your keys — a seemingly trivial detail with major
implications. Let's explore how this helps with data modeling.

:open-graph/description

A peek into data modeling in Clojure and Datomic using namespaced keys.

--------------------------------------------------------------------------------
:section/kind :centered
:section/theme :dark1
:section/title On keys and their usage
:section/body

In Clojure, you can namespace your keys — a seemingly trivial detail with major
implications. Let's explore how this helps with data modeling.

--------------------------------------------------------------------------------
:section/body

[På norsk](https://parenteser.mattilsynet.io/nokler/).

In Clojure, we often model data using the language's built-in data types: maps,
lists, sets, strings, numbers, keywords, and booleans. They're all immutable and
safe to share.

Maps with keywords as keys are the most common way to represent data in Clojure
— for example, this Norwegian municipality:

```clj
{:nummer "3107"
 :navn "Fredrikstad"}
```

Keywords (`:nummer`) are a kind of specialized string used for what I like to
call "technical use." Where strings are used for textual content, keywords are
used as keys and short strings with specific meaning (such as enum values). To
support their role as keys, keywords can be used as functions to look themselves
up in a map:

```clj
(def kommune
  {:nummer "3107"
   :navn "Fredrikstad"})

(:navn kommune)
;;=> "Fredrikstad"
```

Clojure keywords can also have a namespace. At first glance, this might seem
like a fancier or more cumbersome way to name keys:

```clj
(def kommune
  {:kommune/nummer"3107"
   :kommune/navn "Fredrikstad"})

(:kommune/navn kommune)
;;=> "Fredrikstad"
```

When a keyword has a namespace, it's an established convention in Clojure that
it is global. This means `:kommune/navn` can always be expected to have the same
semantics, no matter where it shows up in your codebase. The same can't be said
for `:navn`.

## Ownership

Namespaces can indicate the context of a data point. For example: if I want to
store a municipality in a database, I have to give it a unique ID. Relying too
heavily on a natural ID can quickly go wrong. For instance, anyone using
municipality numbers as natural ids would have had a bad time during
municipality mergers in 2017, 2018, and 2020.

So the municipality gets a synthetic ID that’s more about my database than about
the municipality itself. Namespaces can clarify this distinction:

```clj
{:db/id 17592186046486
 :kommune/nummer "3107"
 :kommune/navn "Fredrikstad"}
```

Here we have a single entity, but it’s clear that the ID isn't the
municipality’s domain ID — it's a synthetic one used by the database.

## Structure

With namespaces, keywords also get built-in structure. That makes it possible to
describe a piece of data along multiple axes without resorting to nesting to
convey structure.

On [smilefjes.mattilsynet.no](https://smilefjes.mattilsynet.no), we have
[dedicated pages for
municipalities](https://smilefjes.mattilsynet.no/kommune/fredrikstad/). One way
to model this is to say that a page has a municipality:

```clj
{:page/uri "/kommune/fredrikstad/"
 :page/kind :page.kind/kommune-page
 :page/kommune {:kommune/nummer "3107"
                :kommune/navn "Fredrikstad"}}
```

This is fine, but it’s a bit odd that a page “has” a municipality. With
namespaces we can convey the structure while flattening the data. We can simply
say that a municipality has a page URL:

```clj
{:db/id 17592186046486
 :kommune/nummer "3107"
 :kommune/navn "Fredrikstad"
 :page/uri "/kommune/fredrikstad/"
 :page/kind :page.kind/kommune-page}
```

Now all the data is on a single level, without losing structure or ownership.
There is a page for the municipality Fredrikstad at the URL
`/kommune/fredrikstad/`, but the naming makes it clear that the URL isn’t an
inherent property of the municipality.

Is it a page? Yes. Is it a municipality? Also yes. And no. It depends on the
context of the question.

You might not agree with me that this is beautiful. At the very least, it’s a
bit unconventional. But don't let familiarity guide your judgement, this is a
powerful modeling tool.

## PS!

How do we store this? Well, this is where
[Datomic](https://magnars.com/datomic-tidbits/) (once again) shines. Datomic
stores data as attributes. In fact, the example above is pulled straight from
the database behind the [Smilefjes pages](https://smilefjes.mattilsynet.no/).
