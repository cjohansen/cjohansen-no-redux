--------------------------------------------------------------------------------
:page/title Some letters are more equal than others
:tech-blog/published #time/ldt "2023-11-21T12:00"
:tech-blog/tags [:tag/javascript :tag/search]
:tech-blog/description

In the previous post, we looked at how full-text search works, treating all
symbols in the index as equally important. But that’s not always how it works in
practice — for instance, matches on whole words are usually better than matches
on word fragments. This can be solved with weighting, which is today’s topic.

:open-graph/description

How weighting terms in an index — both during indexing and searching — can give
us more relevant search results.

--------------------------------------------------------------------------------
:section/kind :centered
:section/theme :dark1
:section/title Some letters are more equal than others
:section/body

In the previous post, we looked at how full-text search works, treating all
symbols in the index as equally important. But that’s not always how it works in
practice — for instance, matches on whole words are usually better than matches
on word fragments. This can be solved with weighting, which is today’s topic.

--------------------------------------------------------------------------------
:section/body

[På norsk](https://parenteser.mattilsynet.io/sok-vekting/).

There are many ways to apply weighting in search. We can apply weights during
indexing, based on which field a symbol comes from and what kind of tokenization
was used. Maybe ngrams should count for less than full words. In addition, we
can apply weights at search time, which allows us to offer different types of
searches from the same index.

In the [first post about how full-text search works](/fulltext-search/), we
built a single large index. That limits our ability to apply weighting at query
time, since we’ve already lost some information about the symbols in the index.
A strategy that gives us more flexibility is to build several smaller indexes
and tune searches by combining them in different ways.

Here’s a refresher of the data we’re indexing:

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

Last time, we indexed the titles, the descriptions, and 2-length ngrams from the
titles. Now let’s do it again — but this time, we’ll place them into three
separate indexes:

```js
{
  "title": {
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
  },
  "description": {
    "about": ["a1"],
    "clown": ["a1", "a2", "a3"],
    "researchs": ["a1"],
    "exciting": ["a1"],
    "insights": ["a1"],
    "into": ["a1"],
    "the": ["a1", "a1", "a3"],
    "humor": ["a1", "a2"],
    "of": ["a1", "a2"],
    "future": ["a1"],
    "how": ["a2"],
    "feet": ["a2"],
    "affect": ["a2"],
    "our": ["a2"],
    "experience": ["a2"],
    "discover": ["a3"],
    "unexpected": ["a3"],
    "findings": ["a3"],
    "that": ["a3"],
    "researchers": ["a3"],
    "have": ["a3"],
    "made": ["a3"]
  },
  "titleNgram": {
    "se": ["a1", "a3"],
    "ea": ["a1", "a3"],
    "ar": ["a1", "a3"],
    "rc": ["a1", "a3"],
    "ch": ["a1", "a3"],
    "hi": ["a1"],
    "in": ["a1", "a2", "a3", "a3"],
    "ng": ["a1", "a1", "a3"],
    "fo": ["a1"],
    "or": ["a1", "a1", "a2"],
    "hu": ["a1", "a2"],
    "um": ["a1", "a2"],
    "mo": ["a1", "a1", "a2"],
    "am": ["a1"],
    "on": ["a1"],
    "cl": ["a1", "a3"],
    "lo": ["a1", "a3"],
    "ow": ["a1", "a3"],
    "wn": ["a1", "a3"],
    "ns": ["a1"],
    "th": ["a2"],
    "he": ["a2"],
    "ro": ["a2"],
    "ol": ["a2"],
    "le": ["a2"],
    "of": ["a2"],
    "co": ["a2", "a3"],
    "om": ["a2"],
    "mi": ["a2"],
    "ic": ["a2"],
    "ca": ["a2"],
    "al": ["a2"],
    "fe": ["a2"],
    "ee": ["a2"],
    "et": ["a2"],
    "su": ["a3"],
    "ur": ["a3"],
    "rp": ["a3"],
    "pr": ["a3"],
    "ri": ["a3", "a3"],
    "is": ["a3", "a3"],
    "si": ["a3"],
    "di": ["a3"],
    "sc": ["a3"],
    "ov": ["a3"],
    "ve": ["a3"],
    "er": ["a3"],
    "ie": ["a3"],
    "es": ["a3", "a3"],
    "re": ["a3"]
  }
}
```

Let’s adjust the search function. Previously, it accepted an index and a search
string, performed a hardcoded tokenization before looking up in the index, and
used a hardcoded threshold for how many search tokens had to match in order to
include an ID. This time, we’ll parameterize all of those.

```js
function search({index, requiredMatches, tokenizer}, q) {
  var tokens = tokenizer(q);
  var hits = _.flatMap(tokens, t => lookupToken(index, t));
  var results = _.groupBy(hits, r => r.id);
  var n = Math.floor(tokens.length * requiredMatches || 1);

  return Object.keys(results)
    .filter(r => isRelevantResult(results[r], n))
    .map(id => getScoredResult(id, results[id]))
    .toSorted((a, b) => b.score - a.score);
}
```

To recreate the search from last time, we can call it as such:

```js
search({
  index: index,
  tokenizer: tokenize,
  requiredMatches: 0.8
}, "humor feet");
```

## Searching Across Multiple Indexes

Now that we have multiple indexes, we can perform multiple searches and combine
the results. Let’s write a small helper function to assist with this:

```js
function searchAll({queries}, q) {
  var results = _.groupBy(
    _.flatMap(queries, config => search(config, q)),
    r => r.id
  );

  return Object.keys(results)
    .map(id => getScoredResult(id, results[id], 1));
}
```

This function is quite similar to the search function and works in much the same
way, but in practice it performs an OR across all the queries that are passed
in. Now we can specify that a search should look in multiple indexes and combine
the results:

```js
searchAll({
  queries: [
    {
      index: index.titleNgram,
      requiredMatches: 0.8,
      tokenizer: s => tokenizeNgrams(s, 2)
    },
    {
      index: index.title,
      tokenizer: tokenize
    }
  ]
}, "research humor");
```

### Weighting

Now that we can search by querying the different indexes with different
parameters, we can finally introduce weighting. By adding a `boost` to a query,
all matches from that query will be multiplied by that number:

```js
searchAll({
  queries: [
    {
      index: index.titleNgram,
      requiredMatches: 0.8,
      tokenizer: s => tokenizeNgrams(s, 2)
    },
    {
      index: index.title,
      tokenizer: tokenize,
      boost: 20
    },
    {
      index: index.description,
      tokenizer: tokenize,
      boost: 5
    }
  ]
}, "research humor");
```

Now we’re searching across all indexes, but we give higher weight to matches on
whole words, especially those that appear in the title.

## Auto-complete

Last time, I mentioned the possibility of using so-called edge ngrams for
auto-complete searches. Let’s build one more index:

```js
var index = {
  title: indexDocuments(data, a => tokenize(a.title)),
  description: indexDocuments(data, a => tokenize(a.description)),
  titleNgram: indexDocuments(data, a => tokenizeNgrams(a.title, 2)),
  titleEdgeGrams: indexDocuments(data, a => tokenizeEdgeNgrams(a.title, 2, 15))
};
```

```js
searchAll({
  queries: [
    {
      index: index.titleEdgeGrams,
      tokenizer: s => tokenizeEdgeNgrams(s, 2, 15)
    }
  ]
}, "the rol");

//=> "The Role of Comical Feet in Humor"
```

And with that, we’ve got a fairly powerful little search engine in [just over
100 lines of plain
JavaScript](https://gist.github.com/cjohansen/c4ff8f7f997f654f2af396c55a7e9fde)
– and that’s even with the three lodash functions inlined, making the code
completely dependency-free. Not bad!

We use a search engine like this to search through 2,000 food items on the new
[matvaretabellen.no](https://www.matvaretabellen.no). To reduce the amount of
work done on the client, we serve a [pre-chewed
index](https://www.matvaretabellen.no/search/index/nb.json) from the server (a
[statically generated one](https://magnars.com/static-sites/), of course). You
can judge for yourself how well it works, but we think it turned out quite nice.
