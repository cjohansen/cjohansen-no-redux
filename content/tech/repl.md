--------------------------------------------------------------------------------
:page/title What Exactly Is a REPL?
:page/locale :en
:tech-blog/published #time/ldt "2022-10-25T12:00"
:tech-blog/tags [:tag/clojure]
:tech-blog/description

The REPL breathes life into your program and lets you develop it from inside the
running process. Join me as I try to explain exactly why a REPL is the essential
tool you might not know you're missing in your (work) life.

:open-graph/description

The REPL breathes life into your program and lets you develop it from inside the
running process. Allow me to demonstrate.

--------------------------------------------------------------------------------
:section/kind :centered
:section/theme :dark1
:section/title What Exactly Is a REPL?
:section/body

A good REPL breathes life into your program and lets you develop it from inside
the running process. Join me as I try to explain exactly why a REPL is the
essential tool you might not know you're missing in your (work) life.

--------------------------------------------------------------------------------
:section/body

[På Norsk](https://www.kodemaker.no/blogg/2022-10-repl/).

I recently watched the talk [Stop Writing Dead
Programs](https://www.youtube.com/watch?v=8Ab3ArE8W3s) from Strange Loop 2022,
and was reminded that one of the things I really enjoy when writing Clojure is
that it's an interactive process with a live program. It's Clojure’s REPL that
makes this possible, and I'm now going to try to explain why a REPL is the
essential tool you might not know you're missing in your (work) life.

## What Is a REPL?

"REPL" stands for Read-Eval-Print-Loop. In short, it's a process where you can
type in some code, have it evaluated, and see the result printed out. The REPL
was first created for Lisp in the 1960s, but today, most programming languages
have some sort of approach to this process. But a good REPL can do more than
just read, evaluate, and print.

If you have node.js installed on your machine, you can easily try such a
rudimentary REPL from your terminal:

```sh
✗ node
Welcome to Node.js v18.2.0.
Type ".help" for more information.
> console.log("lol")
lol
```

This kind of programming language prompt can be useful for exploratory work, but
since they rely on throwing a bit of code into a black hole and getting a
response back, their usefulness is limited for larger tasks and reusable code.
You can get a bit of help from shell history and similar tools, but the need for
a persistent file with code quickly becomes apparent.

## Interaction with a Running Process

Something much closer to what we’ll be discussing for the rest of this post is
the devtools in your browser. There you’ll find a console similar to what Node
just gave us—it can read and evaluate code. But it doesn't stop there, because
the console in the browser is part of the app process of your frontend. This
means you can interact with both the code and the data that flows through your
app.

For example, a well-placed global variable can give you access to data from deep
inside your frontend code after you've clicked a few buttons and navigated
around a bit:

```js
function UserButton(user) {
  return div({
    className: 'button',
    onClick(e) {
      window.user = user;
      // ...
    }
  }, user.name);
}
```

After you've clicked this button, `user` will be available for inspection in the
console. If you've worked in this console a bit, you've likely noticed that its
ergonomics impose some limitations on how long you can keep at it, and how much
you're able to accomplish.

## Interacting with the REPL from the Editor

By integrating the REPL into the editor, we can achieve much better ergonomics.
Thanks to its Lisp heritage, a REPL comes bundled when you start a Clojure
process, which we can integrate into the editor. This gives us a similar console
within the editor, but instead of writing code there, we can use editor
integration to “send to the REPL.”

From here on, I’ll talk specifically about Emacs tools for Clojure, but similar
tools exist for other editors.

When you're working with some code in Emacs, you can at any point use a
keybinding to send the expression to the left of the cursor to the REPL for
evaluation, and Emacs prints the result to the right of your cursor (or in a
dedicated window using a specific command—for larger datasets).

![I evaluate a math expression in the REPL](/images/regnestykke-repl.gif)

The text to the right disappears when I move the cursor. In this way, we've
solved the black-hole problem: now we can instead put lots of useful code
snippets into a file and work on it over time. The only limit to what might be
useful in such a file is your imagination: API call examples, exploratory use of
Clojure's core libraries, third-party libraries, or your own code—anything at
all.

## Interacting with the Running Process from the Editor

Since the REPL runs as part of the app process, we can interact with it just
like we did with the frontend code in the browser. And because the REPL is
integrated into the editor, we also don’t need to work with compiled/processed
code. This opens up an interactive way of programming that I wouldn’t want to
live without.

With editor integration, we've moved away from typing code into a console.
Instead, we send code from source files to the REPL process. This means you can
modify a function in your codebase and send it to the REPL just like the math
expression above. The code will be compiled, and the new definition will replace
the old one.

```clj
(defn login-handler [req]
  (let [result (auth/attempt-login req)]
    (if (:success? result)
      {:status 301
       :headers {"location" "/"}}
      {:status 401
       :body "Oh no, you don't!"})))
```

Here's a hypothetical small HTTP handler that performs login. Let’s say I’m in
the middle of testing something and want to bypass the login. I can just tweak
the code a bit and send it to the REPL (I don’t even need to save the file):

```clj
(defn login-handler [req]
  (let [result (auth/attempt-login req)]
    (if (or (= "christian" (-> req :params :username))
            (:success? result))
      {:status 301
       :headers {"location" "/"}}
      {:status 401
       :body "Oh no, you don't!"})))
```

If I send this to the REPL, I can easily bypass the entire authentication
process. This level of REPL-ing resembles hot reloading of code. One important
difference from hot reloading is that I’m only redefining this one function –
any state that has been built up in the process won’t be lost, nor will any
other modifications I’ve made in the same manner.

### Where’s my gif?

When I wrote this post, I added an animated gif to illustrate REPL usage. It
didn’t show up at first, partly because our blog system didn’t pick up gifs from
disk, and partly because I’m a klutz. Below you can see how I solved the
problem.

<div class="gif-movie">
  <div class="gm-play-button"><div class="gmpb-head"></div></div>
  <img class="gm-still" title="Debugging with the REPL" src="/images/debugging-repl-still.jpg">
  <img class="gm-movie" src="/images/debugging-repl.gif">
</div>

## Interacting with the process’s data

Hot reloading may not be that exciting, but how about being able to tinker with
the process data? Let’s say I evaluate this version of my login handler:

```clj
(defn login-handler [req]
  (let [result (auth/attempt-login req)]
    (def login-data {:req req :result result})
    (if (:success? result)
      {:status 301
       :headers {"location" "/"}}
      {:status 401
       :body "Oh no you don't!"})))
```

Now my function will create a variable `login-data` every time `login-handler`
is called. To do this, I can simply log in through the browser — the REPL is
part of the same process as my app. Afterwards, I can work with the data that
was captured in the REPL, and I have the entire application state and library of
functions readily available. The REPL gives me two-way interaction with the
running code. That’s both pretty cool and extremely useful!

It’s not ideal to leave lots of debugging code lying around in production code.
I can move it to a separate location, or I can use Clojure’s handy `comment`:

```clj
(comment

  ;; Evaluer denne for å se på resultatet
  (:result login-data)

  ;; Mener systemet at brukeren som logget inn er admin?
  (auth/admin-user? (-> login-data :result :user))

)
```

What does `comment` do? Nothing! The compiler strips it out, so it doesn't make
it into the production build. But during development, I have the code right
where it's useful and can evaluate it when needed.

Below you see an example where I'm poking around in the Kodemaker blog’s
internals to figure out how we actually select relevant blog posts. At one
point, I swap out `defn` for `defn*` from
[snitch](https://github.com/AbhinavOmprakash/snitch), which exposes all
arguments and local variables from a function call as global variables in the
namespace:

<div class="gif-movie">
  <div class="gm-play-button"><div class="gmpb-head"></div></div>
  <img class="gm-still" title="Using the REPL to understand some code" src="/images/blogg-relevant-repl-still.jpg">
  <img class="gm-movie" src="/images/blogg-relevant-repl.gif">
</div>

If you're curious about the implementation, the [source code for
kodemaker.no](https://github.com/kodemaker/kodemaker.no) is open on GitHub.

## Permanent comments

In the code I work on daily, there’s a `comment` in almost every file we touch.
These contain various useful snippets of code we can evaluate to gain insight
into how things work, inspect data, test functions, investigate customer support
cases, or whatever else might come up.

These snippets can be even more useful than the ones we’ve seen so far. Ours,
for example, provide access to the “system” — the object that holds all the
app’s processes — the web server, database connection, queue system, messaging
subsystem, app config, etc. With that, you can do a lot of fun stuff.

Below is a real example from my work codebase. By retrieving my token from the
browser and pasting it in as `token`, I can run the functions that the handlers
our frontend calls are using.

```clj
(comment

  (def context (:app/context integrant.repl.state/system))

  (def token "...")

  (def request
    {:context context
     :jwt (vite.service/decode-jwt token)})

  (get-vehicle-data request)

  (smart-charging/get-vehicle-vendors
    context
    (auth/get-requesting-user request))

  (get-vehicle-data
   {:context context
    :jwt {:claims {:smart-charging {:user-id "a70e9989-9d09-...-....-............"}}}})
)
```

These blocks go into git along with the rest of the code and save us a lot of
time every single day, while also helping to document the code in a more
interactive way.

## REPL as a TDD replacement

I used to be very enthusiastic about test-driven development because of the
feedback loop it provides (I still like TDD for certain tasks, but that’s
another story). REPL-driven development offers an even tighter feedback loop
than TDD, and I often use it instead of, or in combination with, TDD.

Since the REPL lets you easily evaluate snippets of code, you can start with
some data and one line of code, evaluate it, build out, evaluate again, and work
iteratively until you have something that can be moved into a function.

Below you can see me leaning on the REPL to drive the creation of a function
that merges periods if the dates overlap — date fiddling is typical code I mess
around with, and where it’s especially useful to get a lot of feedback along the
way. When you see "`=>`" I have evaluated the code. The session lasts a few
minutes and can give you an impression of how this iterative process works.

<div class="gif-movie">
  <div class="gm-play-button"><div class="gmpb-head"></div></div>
  <img class="gm-still" title="A function being created with help from the REPL" src="/images/collect-periods-repl-still.jpg">
  <img class="gm-movie" src="/images/collect-periods-repl.gif">
</div>

## REPL in production

The Clojure REPL comes with a network protocol, so you can connect your editor
to a running process. I don’t often do this, but when you have a problem that
neither logs nor local debugging can solve, this is a superpower that can save
you.

The last time I ran a REPL against production, we found a JVM bug related to
text encoding. All the hair pulling aside — this problem could not be reproduced
locally. We literally had to snap the values in the production process to
conclude. Without a REPL in the process, we probably would never have gotten to
the bottom of that particular issue.

## REPL live and in person

The joy of a good REPL becomes most evident when you get to work with it
yourself. Second best is to watch someone else use it.
[Magnar](https://magnars.com) and I make screencasts both in
[Norwegian](https://zombieclj.no) and
[English](https://www.parens-of-the-dead.com) where we show how we work with
Clojure — and where the REPL is a central star of the show. Feel free to drop
by!

Finally, I’d like to once again recommend, and urge you to [Stop Writing Dead
Programs](https://www.youtube.com/watch?v=8Ab3ArE8W3s)!

<script type="text/javascript">
(function () {
document.querySelectorAll(".gif-movie").forEach(function (el) {
  var playing = false;
  var canvas = el.querySelector(".gm-still");
  var still = canvas.src;
  var movie = el.querySelector(".gm-movie").src;
  var button = el.querySelector(".gm-play-button");
  el.addEventListener("click", function (e) {
    canvas.src = playing ? still : movie;
    button.style.opacity = playing ? 1 : 0;
    playing = !playing;
  });
  fetch(movie);
});
}())
</script>
