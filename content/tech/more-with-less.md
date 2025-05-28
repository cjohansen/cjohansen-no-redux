--------------------------------------------------------------------------------
:page/title Achieve more by doing less
:page/locale :en
:tech-blog/published #time/ldt "2021-11-09T12:00"
:tech-blog/tags [:tag/clojure :tag/functional-programming]
:tech-blog/description

A short story about how a new feature in an open source library was scaled down
and yet became more useful.

--------------------------------------------------------------------------------
:section/kind :centered
:section/theme :dark1
:section/title Achieve more by doing less
:section/body

[På Norsk](https://www.kodemaker.no/blogg/2021-11-mer-mindre/).

I maintain a rendering library ([Dumdom](https://github.com/cjohansen/dumdom))
that lets you render the DOM with components, much like React. I’m also
unusually keen on representing as much as possible as pure data. With virtual
DOM and components, we can have a data representation of the UI, with a few
exceptions:

```clj
[:button {:style {:background "#ff0000"}
          :onClick (fn [e]
                     (println "Clicked the button!"))}
 "Click me"]
```

This is plain Clojure data: some keywords, strings, maps, and vectors. What is
not data, however, is the event handler. Functions are opaque objects — they
cannot be serialized as data, they make testing trickier, and they reduce the
rendering library’s ability to make efficient decisions about what needs
updating in the DOM and what does not.

--------------------------------------------------------------------------------
:section/body

Usually, I code event handlers to publish messages on the app’s message bus. In
other words, I translate a generic "click" into more app-specific messages:

```clj
[:form
 [:input {:value "christian@kodemaker.no"}]
 [:button
  {:onClick
   (fn [e]
     (bus/publish [[:save-in-store :email-status :saving]
                   [:ui-event :saved-email]
                   [:save-email "christian@kodemaker.no"]]))}
  "Save email address"]]
```

Here, someone filled in their email address, and if they now press the button,
the app will receive three messages, which are implemented as "actions"
somewhere.

Our code was full of such examples. It was time to roll up our sleeves and
create a new feature: built-in support for event handlers that are just data.

--------------------------------------------------------------------------------
:section/theme :light1
:section/title Iteration #1: Dumdom's message bus
:section/body

I started working on defining a protocol for the message bus in our client code.
The idea was that Dumdom should have a default implementation of this as a
convenience, but that it should be possible to bring your own message bus if you
wanted. The protocol looked like this:

```clj
(defprotocol EventBus
  (watch [_ name topic handler])

  (unwatch [_ name topic handler])

  (publish [_ topic args]))
```

When my colleague [Anders](https://kodemaker.no/anders/) saw this, he
immediately said: "That protocol describes too much!" And he was absolutely
right: Dumdom itself should only call one of these functions, `publish`, so why
should it care about how the others look?

The first simplification was a fact: the protocol only needs to specify
`publish`. The built-in implementation can still offer `watch` and `unwatch`,
but if you provide your own implementation you can subscribe to messages however
you want.

--------------------------------------------------------------------------------
:section/title Iteration #2: No message bus
:section/body

After further discussions, we concluded that Dumdom doesn't actually need an
implementation of a message bus. It's a rendering library, after all. The value
proposition of this feature is that you should be able to express DOM event
handlers as data, so Dumdom can do its job more efficiently, and you avoid
polluting your data with functions.

The second simplification was to remove the message bus — Dumdom facilitates
that you *can* use one, but you create it yourself.

--------------------------------------------------------------------------------
:section/theme :dark1
:section/title Iteration #3: Format of event data
:section/body

If Dumdom is to publish event data, it necessarily must have some opinions about
how event data looks. And to truly be able to manage without functions, it must
be possible to specify that you want to include usual properties from the event
object such as `target`, the value from the target element, etc.

Here is a draft that lets you — with pure data — define an event handler that
gets the value of the input field:

```clj
[:input
 {:onChange [[:save-in-store :email :dumdom.event/target-value]]}]
```

The idea is that Dumdom will replace `:dumdom.event/target-value` with
`e.target.value` such that your app receives this message:

```clj
[:save-in-store :email "christian@kodemaker.no"]
```

All well and good, but delivering this requires a lot of assumptions. It means
quite a bit of functionality to implement, quite a bit to document. Data on
event handlers must be validated.

Are we heading in the wrong direction? What we want is to make it possible to
express event handlers as data, not to dictate how your app should send and
receive messages.

The third simplification comes in the form of renaming `publish` to
`handle-event`. And that protocol? A protocol with one function is a very
object-oriented way to attach a function to "one thing". In a functional
language, we might as well just pass the function along.

--------------------------------------------------------------------------------
:section/title Iteration #4: Dumdom’s top-level event handler
:section/body

When we finally reached the end, this is what we had left:

```clj
(d/render
 [:form
  [:input {:value "christian@kodemaker.no"}]
  [:button
   {:onClick [[:save-in-store :email-status :saving]
              [:ui-event :saved-email]
              [:save-email "christian@kodemaker.no"]]}
   "Lagre e-postadressen"]]

 (js/document.getElementById "app")

 {:handle-event (fn [e data]
                  (println "Event triggered")
                  (println data)
                  (println (.-target e)))})
```

The original idea of giving Dumdom a message bus has been scrapped in favor of
Dumdom supporting a global event handler which is called if you specify
something other than a function on attributes like `:onClick`. But the dream of
an integrated message bus in the rendering library is not dead: Dumdom now
provides just enough tools for us to hook into the message bus we already have
in our app.

Note that Dumdom no longer needs to know anything about your event data. It just
forwards it to the handler, and it’s up to you to do something sensible with it.
Want to forward the data to a message bus? Go ahead! Want to interpolate values
from the event? Fine, create an interpolation function and plug it in.

## Achieve more by doing less

The resulting
[commit](https://github.com/cjohansen/dumdom/commit/fe642dc7a1de71bb63f011823692f60698517b6d)
was small and neat, and roughly ~10 lines of code were added to Dumdom for this
feature, ignoring docs, tests, etc.

Why ship something so stripped down? With this change, Dumdom has created
opportunities for its users without succumbing to scope creep. It remains a
small, lightweight rendering library, and if you take advantage of this new
feature, Dumdom does its job even better.

"Look at all the things I'm not doing," was once said in a legendary screencast.
Back then it was about implicit assumptions, here it’s about doing no more than
exactly enough. If I still want to deliver a "batteries included" solution for
messaging in Dumdom, I can build that in a separate library. By composing these
from the outside, we ensure Dumdom a stable future. All the assumptions, code,
and documentation discarded along the way are code that won’t gather new bugs or
security holes, won’t confuse users, and won’t open the door to accumulating
even more nearly-related features.

This way, Dumdom can become "finished". Finished code is code you don’t have to
waste time updating, maintaining, and chasing after. That frees your time for
something more valuable. You simply achieve more by having your things do less.

A big thanks to [Magnar](https://magnars.com) and Anders, who deserve much of
the credit for Dumdom getting such a nice extension as it did.
