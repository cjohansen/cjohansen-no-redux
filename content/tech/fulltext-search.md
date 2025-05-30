--------------------------------------------------------------------------------
:page/title Fulltext search from scratch
:tech-blog/published #time/ldt "2023-11-07T12:00"
:tech-blog/tags [:tag/javascript :tag/search]
:tech-blog/description

Imagine, all of Shakespeare’s collected works hidden in a box of alphabet
biscuits. That’s roughly the approach we’ll take as we build a tiny search
engine in JavaScript to understand how full-text search works.

:open-graph/description

Let's build a tiny search engine in JavaScript to understand how full-text
search works.

--------------------------------------------------------------------------------
:section/kind :centered
:section/theme :dark1
:section/title Fulltext search from scratch
:section/body

Imagine, all of Shakespeare’s collected works hidden in a box of alphabet
biscuits. That’s roughly the approach we’ll take as we build a tiny search
engine in JavaScript to understand how full-text search works.

--------------------------------------------------------------------------------
:section/body

[På norsk](https://parenteser.mattilsynet.io/fulltekstsok/).

Full-text search can seem like magic. What’s really going on when I see results
for "clown research" after searching for "research"? In this post, I’ll build a
small search engine in JavaScript that attempts to explain exactly that. There
are many ways to build a search engine, but the approach I’ll use here is
loosely based on the model used in ElasticSearch (and the underlying Lucene).

## The Index

A fast search requires an index. The index lets us look things up in constant
time, rather than iterating over large amounts of data to find matches. An index
trades space/memory for speed. At its simplest, a search index can be a mapping
between search phrases and a list of documents that match:

```js
{
  "clown": ["id1", "id2", "id3"],
  "research": ["id1", "id3"],
  "feet": ["id2"],
  "humor": ["id1", "id2"]
}
```

To search this index, we simply look up the search term and see which documents
we find. This index can only return results for the four specific terms. If we
want to answer many different queries, the index needs to be dramatically larger
– but it still can’t contain every conceivable search string. So we’ll need some
clever tricks.

## Indexing

Indexing is the process we use to build the index from our source material.
Let’s start with the following dataset:

```js
var data = [
  {
    id: "a1",
    title: "Searching for Humor Among Clowns",
    description: "About clown research’s exciting insights into the humor of the future."
  },
  {
    id: "a2",
    title: "The Role of Comical Feet in Humor",
    description: "How clown feet affect our experience of humor."
  },
  {
    id: "a3",
    title: "Surprising Discoveries in Clown Research",
    description: "Discover the unexpected findings that clown researchers have made."
  }
];
```

A good starting point is to index all the titles. Since we don’t want users to
have to search using the exact title, we need to massage the data a bit before
it goes into the index. In ElasticSearch, you use *analyzers* to break a text
field down into tokens that are stored in the index. An analyzer can consist of
a series of tools, but the most important is what’s called a *tokenizer* — an
algorithm that determines how a text string is split into individual tokens.

A good first step is to get rid of uppercase letters and split the string on
spaces and other punctuation. The resulting index will look like this:

```js
var index = {
  "searching": ["a1"],
  "for": ["a1"],
  "humor": ["a1", "a2"],
  "among": ["a1"],
  "clowns": ["a1"],
  "the": ["a2"],
  "role": ["a2"],
  "of": ["a2"],
  "comical": ["a2"],
  "feet": ["a2"],
  "in": ["a2", "a3"],
  "surprising": ["a3"],
  "discoveries": ["a3"],
  "clown": ["a3"],
  "research": ["a3"]
};
```

## Search

To search the index, we simply look up the search string and see what we find. A
very naive approach looks like this:

```js
function search(index, q) {
  return index[q];
}
```

This can answer exact queries:

```js
search(index, "clown"); //=> ["a1", "a3"]
```

But it doesn't take a whole lot of imagination to be disappointed:

```js
search(index, "Clown"); //=> null
search(index, "role humor"); //=> null
```

Even if we indexed the title `"The Role of Comical Feet in Humor"`, a search for
`"role humor"` returns no results.

### Search Tokens

The index was built by generating tokens from the source material. To get good
matches from the index, we need to analyze the search string using the same
tools and then look up all the tokens we end up with. Let's start by creating a
helper function that finds all the IDs matching a single token:

```js
function lookupToken(index, token) {
  var hitsById = _.countBy(index[token]);

  return Object.keys(hitsById)
    .map(id => ({
      id: id,
      token: token,
      score: hitsById[id]
    }));
}
```

First, the token is looked up in the index, and then [`countBy` from
lodash](https://lodash.com/docs/4.17.15#countBy) gives us an overview of which
IDs match and how many times. In the end, we get a list of objects where each
pair of ID and search token appears once, along with a "score" that—for now—is
simply the number of matches. The result looks like this:

```js
[
 {
   "id": "a1",
   "token": "humor",
   "score":1
 },
 {
   "id": "a2",
   "token": "humor",
   "score":2
 }
]
```

When we potentially have multiple search tokens, we need to decide whether the
search should be "OR" (all documents where at least one of the tokens matches)
or "AND" (only the documents where all tokens matched)—or something in between.
Let's start by requiring all:

```js
function isRelevantResult(results, n) {
  return results.length >= n;
}

function search(index, q) {
  var tokens = tokenize(q);
  var hits = _.flatMap(tokens, t => lookupToken(index, t));
  var results = _.groupBy(hits, r => r.id);
  var n = tokens.length;

  return Object.keys(results)
    .filter(r => isRelevantResult(results[r], n));
}
```

First, we break the search string into tokens. Then we look up each token, which
gives us a list of id/token pairs with a score.
[`flatMap`](https://lodash.com/docs/4.17.15#flatMap) ensures all of these lists
are combined into one large list. To find the documents that matched all the
search tokens, we group by document ID and return the IDs that have as many
matches as there are tokens.

This search finds document IDs regardless of case and even when the search terms
are out of order:

```js
search(index, "clown"); //=> ['a1', 'a3']
search(index, "role humor"); //=> ['a2']
```

## Indexing, part deux

Let’s expand the index by including the description as well. Here’s a snippet of
the result:

```js
{
  "the": ["a1", "a1", "a1", "a2", "a3"],
  "bright": ["a1"],
  "future": ["a1", "a1"],
  "of": ["a1", "a1", "a2", "a2"],
  "clown": ["a1", "a1", "a2", "a3", "a3"],
  ...
}
```

### Scoring

The word "clown" appears multiple times in article 1. That means article 1
should be considered more relevant and appear at the top of the results. We can
achieve this by giving each document a total score based on the number of
matches per token, and then sorting:

```js
function getScoredResult(id, results) {
  return {
    id: id,
    score: results.reduce((score, r) => score += r.score, 0)
  };
}

function search(index, q) {
  var tokens = tokenize(q);
  var hits = _.flatMap(tokens, t => lookupToken(index, t));
  var results = _.groupBy(hits, r => r.id);
  var n = tokens.length;

  return Object.keys(results)
    .filter(r => isRelevantResult(results[r], n))
    .map(id => getScoredResult(id, results[id]))  // Nytt
    .toSorted((a, b) => b.score - a.score);       // Nytt
}
```

A bit to chew on, but `results[id]` gives us all the id/token pairs with the
score found in `lookupToken` — for example `{id: "id1", token: "humor", score:
2}`. We get the total score for an id by summing up the individual scores.

## Fuzzy Search

The search function now handles multiple terms, but you still have to type them
exactly as they appear in the source material. One way to improve this is to
break the tokens into smaller parts than whole words. There are many techniques
for this, and the one we'll look at now is called "ngrams".

The easiest way to understand ngrams is to see them. The word "humor" consists
of these ngrams when `n = 2`:

```
hu
um
mo
or
```

Let's update our index with ngrams of the title using `n = 2`. The result looks
like this:

```js
{
  "the": ["a1", "a1", "a1", "a2", "a3"],
  "bright": ["a1"],
  "future": ["a1", "a1"],
  "of": ["a1", "a2"],
  "clown": ["a1", "a1", "a2", "a3", "a3"],
  "research": ["a1", "a3"],
  "about": ["a1"],
  "researchs": ["a1"],
  "exciting": ["a1"],
  ...
  "th": ["a1", "a2"],
  "he": ["a1", "a2"],
  "br": ["a1"],
  "ri": ["a1", "a3", "a3"],
  "ig": ["a1"],
  "gh": ["a1"],
  "ht": ["a1"],
  "fu": ["a1"],
  "ut": ["a1"],
  "tu": ["a1"],
  "ur": ["a1", "a3"],
  "re": ["a1", "a1", "a3"],
  ...
}
```

If we break the query string into similar tokens, we can choose how many of them
we require a match for in order to include an id in our results.

Let's consider the word "research":

```
re
es
se
ea
ar
rc
ch
```

The query string "search" contains the following ngrams:

```
se
ea
ar
rc
ch
```

All of the ngrams in "search" are contained in the set of ngrams in "research".
Even if one of them where missing, we should consider it a good match:

```js
function isRelevantResult(results, n) {
  return results.length  >= Math.floor(n * 0.8);
}
```

Now "search" should find both "search" and "research":

```js
search(index, "search")
  .map(res => findArticle(res.id).title)

//=> [
//     "Searching for Humor Among Clowns",
//     "Surprising Discoveries in Clown Research"
//   ]
```
### Edge ngrams

If you're implementing an autocomplete-style search, "edge ngrams" are useful.
These are created by generating ngrams only from the beginning of the word
outward. For example, "research" becomes the following edge ngrams with lengths
from 2 to 8:

```
re
res
rese
resea
resear
researc
research
```

## What’s the Point?

Hopefully you’ve now learned a bit about how search — especially in
ElasticSearch — actually works. But not only that: this little search engine can
also be a handy way to implement simple frontend search if your dataset isn’t
too large. Say you have a few thousand titles — then these 100 lines of
JavaScript are enough to give you a decent search.

By extending the search engine with a few simple weighting concepts, this can
become quite powerful. I'll return to that in a future post.
