--------------------------------------------------------------------------------
:page/title How I learned to stop worrying and love the API
:tech-blog/published #time/ldt "2023-10-27T12:00"
:tech-blog/tags [:tag/productivity]
:tech-blog/description

As a follow-up to my [post on backward
compatibility](/backwards-compatibility/), I'm sharing some practical tips today
on how to design HTTP APIs that provide a great user experience over time.

:open-graph/description

Some practical tips on how to design HTTP APIs that provide a great user
experience over time.

--------------------------------------------------------------------------------
:section/kind :centered
:section/theme :dark1
:section/title How I learned to stop worrying and love the API

:section/body

[På norsk](https://parenteser.mattilsynet.io/bakoverkompatibilitet-i-praksis/).

As a follow-up to my [post on backward
compatibility](/backwards-compatibility/), I'm sharing some practical tips today
on how to design HTTP APIs that provide a great user experience over time.

Most application developers today manage one or more HTTP/REST APIs, whether
it's for communication between microservices, from backend to frontend, or for
external teams and customers. I have a few specific tips for designing such APIs
to ensure they can evolve in a backward-compatible way.

## Never return a bare list

You've implemented a search API. It should return search results, so you go with
a classic list:

```js
[{
   id: 627,
   title: "Cheez Crunchers",
   score: 98.2
 }, {
   id: 32647,
   title: "Bacon crisp",
   score: 47.5
 }, {
   id: 934,
   title: "Sørlandschips",
   score: 32.3
 }]
```

The problem with a bare list appears when you want to add some meta data to the
endpoint. Maybe you want to indicate which indexes were searched or what query
string these results came from. When the body is just a list, there's nowhere to
put that, and you're forced to change the response type—anything but
backward-compatible.

An elegant solution is to never (there's that word again) return a bare list.
Always wrap it in an object, giving yourself room to grow later:

```js
{
  results: [{
    id: 627,
    title: "Cheez Crunchers",
    score: 98.2
  }, {
    id: 32647,
    title: "Bacon crisp",
    score: 47.5
  }, {
    id: 934,
    title: "Sørlandschips",
    score: 32.3
  }]
}
```

This response has all the space it needs to grow in a way that won't bother
those already using your API. Lovely.

## Never return naked objects

I'm not here to judge your API content, but by "naked" I mean: don't return raw
domain objects directly in the body. This is a variation on the point above:
give yourself room to grow.

Consider this API for creating a user:

```sh
POST https://worlds-best-api.com/users
Content-Type: application/json

{
  givenName: "Christian",
  familyName: "Johansen",
  username: "cjohansen"
}
```

The API responds with 201 and includes the new user (with assigned id) in the body:

```js
{
  id: 123,
  givenName: "Christian",
  familyName: "Johansen",
  username: "cjohansen"
}
```

Now let's say you want to include some meta-info about the action. Where do you
put it? Stuffing it into the user object doesn't feel right. Just like with
lists, you can wrap the object in an envelope that gives you room to grow:

```js
{
  user: {
    id: 123,
    givenName: "Christian",
    familyName: "Johansen",
    username: "cjohansen"
  }
}
```

Voila! This response can grow with additional keys besides user, without
bothering anyone using the API today.

## Ignore extraneous information

Your API should simply ignore unnecessary data in incoming requests. Imagine
again we're creating a user:

```sh
POST https://worlds-best-api.com/users
Content-Type: application/json

{
  givenName: "Christian",
  familyName: "Johansen",
  username: "cjohansen",
  userAgent: "my-consumer"
}
```

This happens when people auto-map their own data structures into payloads, etc.
Whatever the reason—if you got what you need to do the job, there's no reason to
reject extra data.

Some of you may cringe at this suggestion, but tell me: when was the last time
you checked for extra URL parameters in your APIs? Right. You didn't.

There's one downside to this that needs addressing. Suppose given name and
family name are optional in your API. What happens here?

```sh
POST https://worlds-best-api.com/users
Content-Type: application/json

{
  givenName: "Cher",
  famiName: "Johansen",
  username: "cjohansen"
}
```

I've obviously misspelled "familyName". If the API ignores "extra" fields, this
isn't caught—and I've just created a user with no last name. One way to handle
this is to lean on the flexibility of our response structure and flag it:

```js
{
  user: {
    id: 123,
    givenName: "Cher",
    username: "cjohansen"
  },
  unrecognizedFields: ["famiName"]
}
```

## It's OK to duplicate data

Imagine an API that returns nutritional info about food. It has a field called
energy, a number:

```js
{
  food: "Cheeze Crunchers",
  energy: 567
}
```

Looking back, this wasn't a great choice. There's no indication of what unit
this number is in. Calories? Joules? Kilowatts? Who knows? The API must improve.

The devil on your shoulder says you should change it to this:

```js
{
  food: "Cheeze Crunchers",
  energy: {
    measure: 567,
    unit: "kcal"
  }
}
```

Much better. But what about users already depending on the old format? They
won't like the break—even if they appreciate the clarification. Since energy is
in use, keep it. Just add a new field:

```sh
{
  food: "Cheeze Crunchers",
  energy: 567,
  energyMeasure: {
    measure: 567,
    unit: "kcal"
  }
}
```

This API will work well for both old and new users. Update the docs to say that
energy is historical and should be avoided (but will never be removed).

This might offend your sense of aesthetics. Isn't this ugly? Maybe. But code
that's seen some winters has a different kind of beauty. This API speaks of
experience and respect for its users.

## What about versioning?

Many use API versioning to make backward-incompatible changes. It can be a
decent strategy, but users often end up forced to chase your changes anyway,
since older versions get retired when new ones arrive.

The goal of backward compatibility is for today's integrations to keep working 5
years from now, without anyone lifting a finger. If you regularly release new
versions and deprecate old ones, you're not really offering backward
compatibility.

## Think of your existing users

When designing APIs, think: "Can I add information here without breaking anyone
already using this?" I'm not saying everything should be overly abstract and
generic, just that you make small tweaks that give you room to grow.

Finally: not everything has to live in one endpoint. It's totally OK to have two
endpoints for creating a user. If your implementation isn't tightly coupled to
its HTTP representation, it should be trivial to offer the same logic behind
multiple HTTP interfaces.
