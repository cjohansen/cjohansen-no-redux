--------------------------------------------------------------------------------
:page/title Walking is good for you
:page/locale :en
:tech-blog/published #time/ldt "2023-04-14T12:00"
:tech-blog/tags [:tag/clojure]
:tech-blog/description

There’s a lot to enjoy in Clojure: pure functions, immutability, and the REPL,
to name some of the big things. Today, let’s instead look at two tiny but very
useful functions.

--------------------------------------------------------------------------------
:section/kind :centered
:section/theme :dark1
:section/title Walking is good for you
:section/body

There's a lot to enjoy in Clojure: pure functions, immutability, and the
[REPL](/repl/), to name just a few major features. But today, we’re going to
look at two tiny but extremely handy functions: `clojure.walk/postwalk` and
`clojure.walk/prewalk`.

--------------------------------------------------------------------------------
:section/body

[På Norsk](https://www.kodemaker.no/blogg/2023-04-14-walk/).

You can think of `clojure.walk` as `string/replace` for arbitrary data
structures, or maybe as `map` for nested data. Let’s start with an illustrative,
if trivial, example.

If I have a list of numbers, I can easily increment them by one by mapping over
the list:

```clj
(map inc [1 2 3 4 5 6]) ;;=> [2 3 4 5 6 7]
```

If my data is spread out in a deeper nested structure, I can do the same thing
with `clojure.walk`, albeit with a bit more ceremony:

```clj
(require '[clojure.walk :as walk])

(def data
  {:name "Christian"
   :hands [{:fingers 5}
           {:fingers 5}]
   :legs [{:toes 5}
          {:toes 5}]})

(walk/postwalk
 (fn [x]
   (if (number? x)
     (inc x)
     x))
 data)

;;=>
{:name "Christian"
 :hands [{:fingers 6}
         {:fingers 6}]
 :legs [{:toes 6}
        {:toes 6}]}
```

We can't just toss `inc` straight into the call to `postwalk` like we did with
`map`, because the function needs to handle all the elements in the data
structure — and they’re not all numbers. A good old-fashioned print gives more
insight into how `walk` works:

```clj
(def data
  {:name "Christian"
   :hands [{:fingers 5}
           {:fingers 5}]})

(walk/postwalk
 (fn [x]
   (prn x)
   x)
 data)

;;=>
;; :name
;; "Christian"
;; [:name "Christian"]
;; :hands
;; :fingers
;; 5
;; [:fingers 5]
;; {:fingers 5}
;; :fingers
;; 5
;; [:fingers 5]
;; {:fingers 5}
;; [{:fingers 5} {:fingers 5}]
;; [:hands [{:fingers 5} {:fingers 5}]]
;; {:name "Christian", :hands [{:fingers 5} {:fingers 5}]}
```

Alright. But what are we supposed to do with this?

## Mustache?

Many have probably come across some form of [a
mustache](http://mustache.github.io/) implementation — that is, strings with
placeholders inside curly braces (the moustaches):

```clj
(stache/render "<h1>Hei {{name}}</h1>" {:name "Christian"})

;;=> "<h1>Hei Christian</h1>"
```

This form of templating is so useful that JavaScript (and probably other
languages) have built it into the language itself. But what if you didn’t have
to limit yourself to strings?

```clj
(defn render [template data]
  (walk/postwalk
   (fn [x]
     (if (and (vector? x) (= :mu/stache (first x)))
       (get data (second x))
       x))
   template))

(render
 [:div
  [:h1 {:style {:color "red"}}
   "Hello" [:mu/stache :name]]]
 {:name "Christian"})

;;=>
[:div
  [:h1 {:style {:color "red"}}
    "Hello" "Christian"]]
```

Pretty sweet to avoid building a bunch of HTML inside a string! There’s nothing
special about `:mu/stache` — it’s just a keyword I use as a marker, similar to
the `{` mustaches in the string version. This 7-line function leans on the 10(!)
lines of code that implement `clojure.walk`, and is already a small templating
library.

## More Use Cases

By taking this line of thinking a bit further, [Magnar](https://magnars.com/)
and I made a neat little [i18n/theming/interpolation
library](https://github.com/cjohansen/m1p). It’s based on `walk` and provides a
handy feature set that lets you do i18n (and other things) in a fully
data-driven way. And it's barely 100 lines of code.

The UIs we build at work even have [data-driven event
handlers](/more-with-less/):

```clj
(Input
 {:type :text
  :value (get-in store [:temperature])
  :change [[:save-in-store
            [:temperature]
            :event/target.value]]})
```

Our code uses `walk` to replace `:event/target.value` with the value from the
field when the event fires. You can see this in action in [Parens of the
Dead](https://parensofthedead.com).

## Data!

Data makes code simpler while also enabling more use cases. `clojure.walk` is
just one example of a small tool Clojure gives you that allows for more
data-driven solutions than you might initially think. Next time, we’ll have a
chat about `walk`’s buddy: `tree-seq`.
