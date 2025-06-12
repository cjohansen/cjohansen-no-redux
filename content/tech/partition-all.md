--------------------------------------------------------------------------------
:page/title Batching with partition
:tech-blog/published #time/ldt "2024-02-20T09:00:00"
:tech-blog/tags [:tag/clojure :tag/datomic]
:tech-blog/description

Sometimes data must be batch processed. Clojure's got just what you need in
`partition`.

--------------------------------------------------------------------------------
:section/kind :centered
:section/theme :dark1
:section/title Batching with partition
:section/body

Sometimes data must be batch processed. Clojure's got just what you need in
`partition`.

--------------------------------------------------------------------------------
:section/body

[På norsk](https://parenteser.mattilsynet.io/porsjoner/).

I recently requested a bunch of ids from a database, like this:

```clj
(defn get-last-served [conn meal-ids]
  (db/q conn
   '{:select [:meal-id (max :served-at)]
     :from :meals
     :where (in :meal-id ?meal-ids)}
   {:params {:meal-ids meal-ids}}))
```

In other words: given all these meal IDs, return a list with each ID and the
last time it was served.

The problem came when I requested too many meals at once. This query was going
to a database server that didn’t appreciate receiving more than 1000 IDs at once
in an `in` clause.

The solution was to batch my query. So how do we do that? Batching is really two
operations: split the input into manageable chunks, and gather the results into
one data structure.

So how do you split a data structure in Clojure? With `partition` or
`partition-all`:

```clj
(partition 2 [0 1 2 3 4])
;;=> ((0 1) (2 3))

(partition-all 2 [0 1 2 3 4])
;;=> ((0 1) (2 3) (4))
```

As you can see, `partition` may omit data. That’s because it only returns tuples
of the specified size (2, in this case). If there's a "remainder", it gets left
out. This can be useful, but not for batching.

`partition-all` includes all input, even if that means returning tuples with
varying numbers of elements. That works well for us, since we now have a list
with a manageable number of inputs to send to the database server.

Given a database connection in `conn` and a list of IDs in `ids`, we can now
loop over this list and fetch results for each individual batch:

```clj
(map
 (fn [batch]
   (get-last-served conn batch))
 (partition-all 1000 ids))
```

This gives us a list of lists of results. These need to be collected into a
single list. The simplest way to do that is to replace `map` with `mapcat` —
`mapcat` expects the function you pass to it to return a list, and then it
concatenates all the results into one flat list:

```clj
(mapcat
 (fn [batch]
   (get-last-served conn batch))
 (partition-all 1000 ids))
```

And just like that, we have partitioning! Let's make a function of it:

```clj
(defn batch [f batch-size xs]
  (mapcat f (partition-all batch-size xs)))
```

We can use it like so:

```clj
(defn get-last-served [conn meal-ids]
  (batch
   (fn [batch]
     (db/q conn
      '{:select [:meal-id (max :served-at)]
        :from :meals
        :where (in :meal-id ?meal-ids)}
      {:params {:meal-ids batch}}))
   1000
   meal-ids))
```

Beautiful!
