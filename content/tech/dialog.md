--------------------------------------------------------------------------------
:page/title Simpler and Better Modals
:page/locale :en
:tech-blog/published #time/ldt "2023-06-07T12:00"
:tech-blog/tags [:tag/html :tag/javascript :tag/css]
:tech-blog/description

All modern browsers now include a built-in building block that helps us create
accessible modals with less code. If you, like me, had no idea this existed,
then this post is for you.

:open-graph/description

All modern browsers now include a built-in building block that helps us create
accessible modals with less code. I'll show you how to use it.

--------------------------------------------------------------------------------
:section/kind :centered
:section/theme :dark1
:section/title Simpler and Better Modals
:section/body

All modern browsers now include a built-in building block that helps us create
accessible modals with less code. If you, like me, had no idea this existed,
then this post is for you.

--------------------------------------------------------------------------------
:section/body

[På Norsk](https://www.kodemaker.no/blogg/2023-06-dialog/).

How many dropdown menus, dialogs, and modals have you built for a web app? And
how many of them had the correct [ARIA
attributes](https://developer.mozilla.org/en-US/docs/web/Accessibility/ARIA/Attributes),
worked well for users who can’t see, could be closed by clicking outside or
pressing escape, *and* were free of weird scroll behavior, glitchy transparent
backgrounds, and other quirks? Well, it might be easier than you think.

## An Example Modal

Let’s create a small modal to illustrate the point. In addition to the markup,
there’s a tiny bit of JavaScript that toggles the `display` property of the
`.modal` and `.backdrop` elements between `"none"` and `"block"`.

```html
<div id="modal1" class="ex">
  <button>Show me a modal!</button>
  <div class="backdrop" style="display: none"></div>
  <div class="modal" style="display: none">
    <h2>Hi there!</h2>
    <p>
     Would you be interested in making some exciting choices about which third
     parties we’ll leak your data to — accompanied by a heartwarming message about
     how deeply and sincerely we care about your privacy?
    </p>
    <p>
      <button>No, thank you</button>
    </p>
  </div>
</div>
```

CSS:

```css
.backdrop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
}

.modal {
  background: #fff;
  padding: 40px;
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  max-width: 500px;
  width: 90vw;
  max-height: 500px;
}
```

<div id="modal1" class="ex">
  <button>Show me a modal!</button>
  <div class="backdrop" style="display: none"></div>
  <div class="modal" style="display: none">
    <h2>Hi there!</h2>
    <p>
     Would you be interested in making some exciting choices about which third
     parties we’ll leak your data to — accompanied by a heartwarming message about
     how deeply and sincerely we care about your privacy?
    </p>
    <p>
      <button>No, thank you</button>
    </p>
  </div>
</div>

<script type="text/javascript">
(function (el) {
var showing = false;

function toggle() {
  if (showing) {
    el.querySelector(".modal").style.display = "none";
    el.querySelector(".backdrop").style.display = "none";
  } else {
    el.querySelector(".modal").style.display = "block";
    el.querySelector(".backdrop").style.display = "block";
  }

  showing = !showing;
}

el.querySelector(".modal button").addEventListener("click", toggle);
el.querySelector("button").addEventListener("click", toggle);
}(document.getElementById("modal1")));
</script>

Beautiful. What more could one ask for? Well, a few things.

Try opening the modal and then press Tab to navigate around. Fortunately for me,
this site is built sensibly enough that this exercise starts off promising: you
tab through clickable elements in the modal. But then it goes downhill — Tab
continues to navigate to links behind the modal. Not very modal, if you ask me.

When you're done with the modal and want to close it, the next problem appears:
clicking the backdrop has no effect, nor does pressing the Escape key. Building
good modals is a lot of work. Your only option, unfortunately, is to click "No
thanks."

And if you're unlucky enough to be reading this article with a screen reader, I
don't need to explain the other issues this solution introduces.

## A better modal

It turns out the browser has already solved most of these problems for us. Now
let’s do what I love most about my job: solve problems by **removing code**.

The HTML is almost identical, except now the modal itself is a `dialog`, not a
`div`, and the `div` that created the backdrop is completely gone:

```html
<div id="modal2" class="ex">
  <button>Vis meg en modal!</button>
  <dialog>
    <h2>Hei der!</h2>
    <p>
      Kan vi interessere deg i noen spennende valg om hvilke tredjeparter vi
      skal lekke dataene dine til med en hjertevarmende tekst om hvor dypt og
      inderlig vi bryr oss om personvernet ditt?
    </p>
    <p>
      <button>Nei takk</button>
    </p>
  </dialog>
</div>
```

Uten at vi legger til noe CSS ser det sånn ut:

<div id="modal2" class="ex">
  <button>Show me a modal!</button>
  <dialog>
    <h2>Hi there!</h2>
    <p>
     Would you be interested in making some exciting choices about which third
     parties we’ll leak your data to — accompanied by a heartwarming message about
     how deeply and sincerely we care about your privacy?
    </p>
    <p>
      <button>No, thank you</button>
    </p>
  </dialog>
</div>

<script type="text/javascript">
function initModal(el) {
  var modal = el.querySelector("dialog");

  function toggle() {
    if (modal.open) {
      modal.close();
    } else {
      modal.showModal();
    }
  }

  el.querySelectorAll("button").forEach(b => b.addEventListener("click", toggle));
}

initModal(document.getElementById("modal2"));
</script>

It won't look super fancy, but it looks like a modal, and most importantly, it
behaves exactly like a modal should: It sits above the rest of the content, the
background is grayed out, keyboard focus is limited to the modal, the Escape key
closes it, and screen readers get the correct information that this is a modal
dialog. Not bad!

All this with a `dialog` element and the following JavaScript:

```js
var el = document.getElementById("modal2");
var modal = el.querySelector("dialog");

function toggle() {
  if (modal.open) {
    modal.close();
  } else {
    modal.showModal();
  }
}

el.querySelectorAll("button")
  .forEach(b => b.addEventListener("click", toggle));
```

To tighten up the visual look, we can add some CSS for the modal box itself. The
backdrop is a pseudo-element and can be styled in any way you like:

```css
.modal2 {
  background: #fff;
  padding: 40px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  max-width: 500px;
  width: 90vw;
  max-height: 500px;
}

.modal2::backdrop {
  background: #591e1e66;
}
```

<div id="modal3" class="ex">
  <button>Show me a beautiful modal!</button>
  <dialog class="modal2">
    <h2>Hi there!</h2>
    <p>
     Would you be interested in making some exciting choices about which third
     parties we’ll leak your data to — accompanied by a heartwarming message about
     how deeply and sincerely we care about your privacy?
    </p>
    <p>
      <button>No, thank you</button>
    </p>
  </dialog>
</div>

<script type="text/javascript">
initModal(document.getElementById("modal3"));
</script>

Nice! Notice the red-tinted backdrop.

## More ways to close

Earlier I tempted you with the possibility to click the backdrop to close the
modal, but that’s not the case yet. This is functionality we don’t get out of
the box, but in classic W3C API style, we can achieve it, albeit in a somewhat
clunky way.

Since the backdrop is a pseudo-element, it is unfortunately invisible to the
DOM. In the DOM’s eyes, the modal covers the entire screen when open. If we move
all padding from the modal itself to a `div` inside it, then any click directly
on the `dialog` element will be a click on the backdrop.

Updated HTML:

```html
<div id="modal4" class="ex">
  <button>Vis meg en modal!</button>
  <dialog class="modal3">
    <div class="modal3-content">
      <h2>Hi there!</h2>
      <p>
       Would you be interested in making some exciting choices about which third
       parties we’ll leak your data to — accompanied by a heartwarming message about
       how deeply and sincerely we care about your privacy?
      </p>
      <p>
        <button>No, thank you</button>
      </p>
    </div>
  </dialog>
</div>
```

CSS:

```css
.modal3 {
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  max-width: 500px;
  width: 90vw;
  max-height: 500px;
}

.modal3-content {
  background: #fff;
  padding: 40px;
}
```

And JavaScript:

```js
document
  .querySelector("#modal4 dialog")
  .addEventListener("click", function (e) {
    if (e.target.tagName == "DIALOG") {
      e.target.close();
    }
  });
```

Et voila!

<div id="modal4" class="ex">
  <button>Vis meg en modal!</button>
  <dialog class="modal3">
    <div class="modal3-content">
      <h2>Hi there!</h2>
      <p>
       Would you be interested in making some exciting choices about which third
       parties we’ll leak your data to — accompanied by a heartwarming message about
       how deeply and sincerely we care about your privacy?
      </p>
      <p>
        <button>No, thank you</button>
      </p>
    </div>
  </dialog>
</div>

<script type="text/javascript">
initModal(document.getElementById("modal4"));
document.querySelector("#modal4 dialog").addEventListener("click", function (e) {
  if (e.target.tagName == "DIALOG") {
    e.target.close();
  }
});
</script>

These are just some of the things `dialog` can do for you. It can also be used
for non-modal dialogs — check out [MDN’s
reference](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/dialog) for
more details.

<style type="text/css">
.ex button {
  font-family: inherit;
  font-size: 0.8rem;
  padding: 4px 12px;
}

.ex h2, .ex p {
  margin: 20px;
}

.backdrop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
}

.modal {
  background: #fff;
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  max-width: 500px;
  width: 90vw;
  max-height: 500px;
  padding: 40px;
}

.modal2 {
  background: #fff;
  padding: 40px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  max-width: 500px;
  width: 90vw;
  max-height: 500px;
}

.modal2::backdrop {
  background: #591e1e66;
}

.modal3 {
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  max-width: 500px;
  width: 90vw;
  max-height: 500px;
}

.modal3-content {
  background: #fff;
  padding: 40px;
}

.modal3::backdrop {
  background: rgba(0, 0, 0, 0.3);
}

</style>
