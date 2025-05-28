--------------------------------------------------------------------------------
:page/title JavaScript Coercion 101
:page/locale :en
:tech-blog/published #time/ldt "2019-11-27T12:00"
:tech-blog/tags [:tag/javascript]
:open-graph/description

If you work with JavaScript (or things that compile to JavaScript), it's good to
know how the language constantly converts values. I'll teach you, and it'll only
take a few minutes.

:tech-blog/description

Developers love pointing out the madness in JavaScript, like `[] + 2 === "2"`,
but if your job is to write code in this language, or languages that compile to
JavaScript without hiding this behavior (for example TypeScript and
ClojureScript), you will save a lot of time debugging and coding by
understanding _why_ things are the way they are.

--------------------------------------------------------------------------------
:section/kind :centered
:section/theme :dark1
:section/title JavaScript Coercion 101
:section/body

Developers love pointing out the madness in JavaScript, like `[] + 2 === "2"`,
but if your job is to write code in this language, or languages that compile to
JavaScript without hiding this behavior (for example TypeScript and
ClojureScript), you will save a lot of time debugging and coding by
understanding _why_ things are the way they are.

--------------------------------------------------------------------------------
:section/body

[På Norsk](https://www.kodemaker.no/blogg/javascript-coercion/).

JavaScript is a dynamic language with more than average quirky rules for
automatic type conversion -- "coercion". Luckily, there are fewer rules than it
may seem, but together they can surprise anyone. If you know the rules, you
won't be surprised by this code example someone recently showed me:

```js
-1 < null // true
null < 1 // true
0 == null // false (exploding head)
```

So: because `null` is greater than -1, but less than 1, it was expected that it
at least would be equal to `0`, which it is not. Read on to learn why.

--------------------------------------------------------------------------------
:section/title Conversions
:section/theme :light1
:section/body

Let's start with the rules for conversion. We'll look at their usage later.

### Primitives

To convert a primitive to a string, i.e. `String(val)`:

- Numbers become their string representations, e.g. `"32"`
- Booleans become `"true"` or `"false"`
- `null` and `undefined` become `"null"` and `"undefined"` respectively

To convert a primitive to a number, `Number(val)`:

- Strings are parsed as numbers, `"23"` becomes `23`, but strings containing
  invalid characters become `NaN`.
- `true` becomes `1`, `false` becomes `0`
- `null` becomes `0`
- `undefined` becomes `NaN`

To convert a primitive to a boolean, `Boolean(val)`:

- `0`, `null`, `undefined`, `""`, `NaN` (and `false`) become `false` (these
  values are so-called "falsy values").
- Everything else becomes `true` ("truthy values").

### Objects

Non-primitive values are first converted to a string or a number, then possibly
further to e.g. a boolean, if necessary.

An object is converted to a primitive via:

- `.valueOf`, if the object has it and it returns a number
- `.toString`, if the object has it and it returns a string

In practice, it is rare to implement these directly, but all
JavaScript objects inherit from `Object.prototype`, which defines both:

- `valueOf` returns `this`, so `obj === obj.valueOf()`
- `toString` usually returns `"[object Object]"`

Since the built-in `valueOf` does not return a number, it is usually
`toString` that is used for primitive conversion, unless you have actively
done otherwise. One notable exception is `Date`, which returns
a timestamp from `valueOf`:

```js
var now = new Date();
now.valueOf() === now.getTime();
```

--------------------------------------------------------------------------------
:section/title A wild coercion appears
:section/body

Now that you understand which rules govern conversion, let's look a bit at
where these rules are applied.

### < and >

These two operators only make sense for numbers, and convert their arguments to
numbers:

```js
3 < "4" //=> 3 < Number("4")
        //=> 3 < 4
        //=> true

null < 1 //=> Number(null) < 1
         //=> 0 < 1
         //=> true

-1 < null //=> -1 < Number(null)
          //=> -1 < 0
          //=> true
```

With that example, we have explained 2/3 of our first code example.

Note that this can get quite tricky with objects:

```js
var clown = {
  age: 32,

  valueOf() {
    return this.age
  }
};

clown < 33 //=> ToPrimitive(clown) < 33
           //=> 32 < 33
           //=> true
```

Or with `toString`:

```js
var clown = {
  toString() {
    return "56";
  }
};

clown < 57 //=> ToPrimitive(clown) < 57
           //=> Number("56") < 57
           //=> 56 < 57
           //=> true
```

### +

The `+` operator in JavaScript is a bit more unpredictable than in other
languages. Like in other languages, it either performs string concatenation or
addition, but unlike other languages, it often mixes these two in the same
expression.

If you have a string or an object on either side, you get concatenation —
otherwise, you get addition. For concatenation, both arguments are converted to
strings; for addition, both are converted to numbers. If you have multiple plus
signs in the same expression, you apply this rule one plus at a time.

Some examples with numbers:

```js
2 + 3 //=> 5

2 + true //=> 2 + Number(true)
         //=> 2 + 1
         //=> 3

true + 4 //=> Number(true) + 4
         //=> 1 + 4
         //=> 5
```

Because you only get concatenation when you have an object involved, you can get
an unexpected result if you try to add a number to an object that can perfectly
well be converted to a number:

```js
var then = new Date(2019, 0, 1);

then + 1000 //=> "Tue Jan 01 2019 00:00:00 GMT+0100 (CET)1000"
```

What happened? Well, since one argument was an object, string concatenation
applies, and thus we get:

```js
then.toString() + String(1000)
"Tue Jan 01 2019 00:00:00 GMT+0100 (CET)" + "1000"
```

If you have a longer expression, you only evaluate one plus at a time:

```js
2 + 3 + true + []
    //=> 5 + true + []
    //=> 5 + Number(true) + []
    //=> 6 + []
    //=> String(6) + String([])
    //=> "6" + ""
```

An array has a `toString` that works like `.join(",")`:

```js
2 + [1, 2, 3] + true
    //=> String(2) + String([1, 2, 3]) + true
    //=> "2" + "1,2,3" + true
    //=> "21,2,3" + String(true)
    //=> "21,2,3true"
```

--------------------------------------------------------------------------------
:section/title ==
:section/theme :dark1
:section/body

Finally, we have JavaScript’s quirkiest operator, `==`. It’s so complex that you
really shouldn’t use it. But since it was part of the original example, let’s
quickly look at [the algorithm behind
it](https://www.ecma-international.org/ecma-262/10.0/index.html#sec-abstract-equality-comparison):

`x == y` always returns either `true` or `false`.

1. If `x` and `y` have the same type (`typeof`, not object type), return `x ===
   y` (primitive values only match "themselves" — except for `NaN`; objects `x`
   and `y` are equal only if they are the same instance — no value equality for
   objects).
2. If both `x` and `y` are `null` or `undefined`, return `true`.
3. If one argument is a number and the other a string, convert the string to a
   number and start over.
4. If one argument is a boolean, convert it to a number and start over (!!!).
5. If one argument is an object, convert it to a primitive and start over.
6. Return `false`.

As said, this is not a tool worth allocating brainpower to handle — use `===`.
But for our original example: how can `-1 < null` and `null < 1` be true when `0
!= null`? The first two were explained above, but let’s break down the last one:

1. `0` and `null` don’t have the same type (`"number"` vs `"object"`)
2. One argument is `null` or `undefined`, but not the other
3. Neither argument is a string
4. Neither argument is a boolean
5. `null` is not an object
6. Return `false`

And there you have the explanation.
