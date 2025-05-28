--------------------------------------------------------------------------------
:page/title CSS Grid
:page/locale :nb
:tech-blog/published #time/ldt "2019-07-03T12:00"
:tech-blog/tags [:tag/css]
:open-graph/description

CSS grids have finally given us a simple and flexible model for web layout.
Learn how!

:tech-blog/description

CSS grids have finally given us a simple and flexible model for web layout. With
just a few properties, you can pretty much retire `float`, gain full control
over source order, and get a powerful tool for responsive design in the bargain.

--------------------------------------------------------------------------------
:section/kind :centered
:section/theme :dark1
:section/title CSS Grid
:section/body

CSS grids have finally given us a simple and flexible model for web layout. With
just a few properties, you can pretty much retire `float`, gain full control
over source order, and get a powerful tool for responsive design in the bargain.

--------------------------------------------------------------------------------
:section/body

[CSS grids](https://www.w3.org/TR/css-grid-1/) were finalized in December 2017
and now have [broad enough browser support](https://caniuse.com/#search=grid) to
be used practically anywhere. Grids are easy to work with and give you such fine
control that the order and placement in your HTML is completely separated from
the visual result. This makes it an ideal tool for responsive designs. In this
post, I’ll show a couple of fundamental properties that nicely demonstrate the
possibilities of CSS grids.

## Columns

To create a grid, all you need is a container element with display: grid and an
optional specification of the number and size of the columns and/or rows. The
child elements of the container will then place themselves into the grid in the
order they appear in the source (more on bending that rule later). Here’s a grid
with two columns — one that’s 200 pixels wide, and the other taking up the
remaining available space:

```css
.grid-container {
  display: grid;
  grid-template-columns: 200px auto;
}
```

```html
<div class="grid-container">
  <div class="child1"><p>Dette er element #1</p></div>
  <div class="child2"><p>Dette er element #2</p></div>
  <div class="child3"><p>Dette er element #3</p></div>
  <div class="child4"><p>Dette er element #4</p></div>
  <div class="child5"><p>Dette er element #5</p></div>
  <div class="child6"><p>Dette er element #6</p></div>
</div>
```

<style type="text/css">
.grid-container1 {
  display: grid;
  grid-template-columns: 200px auto;
}

.child1 { background: #247ba0; color: #fff; }
.child2 { background: #70c1b3; }
.child3 { background: #b2dbbf; }
.child4 { background: #f3ffbd; }
.child5 { background: #ff1654; color: #fff; }
.child6 { background: #247ba0; color: #fff; }
.grid-ex {margin: 0 0 3rem;}
</style>

<div class="grid-ex">
 <div class="grid-container1">
  <div class="child1"><p>This is element #1</p></div>
  <div class="child2"><p>This is element #2</p></div>
  <div class="child3"><p>This is element #3</p></div>
  <div class="child4"><p>This is element #4</p></div>
  <div class="child5"><p>This is element #5</p></div>
  <div class="child6"><p>This is element #6</p></div>
 </div>
</div>

## Rows

You can similarly define the height of rows using `grid-template-rows`. If you
have more elements than the number of rows specified by `grid-template-rows`,
the additional rows will behave as if they were defined with `auto`.

```css
.grid-container {
  display: grid;
  grid-template-columns: 25% auto;
  grid-template-rows: auto 200px;
}
```

<style type="text/css">
.grid-container2 {
  display: grid;
  grid-template-columns: 25% auto;
  grid-template-rows: auto 200px;
}
</style>

<div class="grid-ex">
 <div class="grid-container2">
  <div class="child1"><p>This is element #1</p></div>
  <div class="child2"><p>This is element #2</p></div>
  <div class="child3"><p>This is element #3</p></div>
  <div class="child4"><p>This is element #4</p></div>
  <div class="child5"><p>This is element #5</p></div>
  <div class="child6"><p>This is element #6</p></div>
 </div>
</div>

## Tables go home

If you spice up this setup with `grid-gap`, `grid-column`, and `grid-row`,
you’ll have a solution as solid as the good old HTML tables (these correspond to
`cellpadding`, `colspan`, and `rowspan`, respectively). You can even control
alignment in both directions:

```css
.grid-container3 {
  display: grid;
  grid-gap: 10px;
  grid-template-columns: 25% auto 25%;
  grid-template-rows: auto 200px;
}

.child1 {
  grid-column: 1 / 3;
}

.child2 {
  grid-column: 3;
  grid-row: 1 / 4;
}
```

<style type="text/css">
.grid-container3 {
  display: grid;
  grid-gap: 10px;
  grid-template-columns: 25% auto 25%;
  grid-template-rows: auto 200px;
}

.grid-container3 .child1 { grid-column: 1 / 3; }
.grid-container3 .child2 { grid-column: 3; grid-row: 1 / 4; }
</style>

<div class="grid-ex">
 <div class="grid-container3">
  <div class="child1"><p>This is element #1</p></div>
  <div class="child2"><p>This is element #2</p></div>
  <div class="child3"><p>This is element #3</p></div>
  <div class="child4"><p>This is element #4</p></div>
  <div class="child5"><p>This is element #5</p></div>
  <div class="child6"><p>This is element #6</p></div>
 </div>
</div>

The middle column above gets `auto` width, not 50%. This way, there's room for
grid gaps without the grid becoming wider than the available space (`25% +
10px + 50% + 10px + 25%`).

## Grid units

So far, we've defined row and column dimensions using percentages, pixels, and
`auto`. But the absolute coolest unit is the relative grid unit `fr`. The full
width of the grid is the sum of all `fr` units you've assigned to the columns,
and a single column will take up the portion of space that its `fr` value
represents relative to the total.

In short: with three columns set to `1fr`, each column will take up one third of
the space:

```css
.grid-container4 {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
}
```

<style type="text/css">
.grid-container4 {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
}
</style>

<div class="grid-ex">
 <div class="grid-container4">
  <div class="child1"><p>This is element #1</p></div>
  <div class="child2"><p>This is element #2</p></div>
  <div class="child3"><p>This is element #3</p></div>
  <div class="child4"><p>This is element #4</p></div>
  <div class="child5"><p>This is element #5</p></div>
  <div class="child6"><p>This is element #6</p></div>
 </div>
</div>

To create a grid with 2 + 1 + 3, you can simply distribute more `fr` units:

```css
.grid-container5 {
  display: grid;
  grid-template-columns: 2fr 1fr 3fr;
}
```

<style type="text/css">
.grid-container5 {
  display: grid;
  grid-template-columns: 2fr 1fr 3fr;
}
</style>

<div class="grid-ex">
 <div class="grid-container5">
  <div class="child1"><p>This is element #1</p></div>
  <div class="child2"><p>This is element #2</p></div>
  <div class="child3"><p>This is element #3</p></div>
  <div class="child4"><p>This is element #4</p></div>
  <div class="child5"><p>This is element #5</p></div>
  <div class="child6"><p>This is element #6</p></div>
 </div>
</div>

## Full flex

So far, we’ve seen how grids easily and elegantly solve some notoriously tricky
problems in CSS. But the best is yet to come: `grid-template-areas`. This is the
icing on the cake that lets you completely separate structural representation
(the HTML elements) from visual representation. It works by giving the container
element an ASCII-art representation of which elements should go where in the
grid, and then you name each of the child elements. Voila — your elements get
placed exactly where you want them.

```css
.grid-container6 {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  grid-template-areas: "child5 child1 child4"
                       "child2 child6 child3"
}

.child1 { grid-area: child1; }
.child2 { grid-area: child2; }
.child3 { grid-area: child3; }
.child4 { grid-area: child4; }
.child5 { grid-area: child5; }
.child6 { grid-area: child6; }
```

<style type="text/css">
.grid-container6 {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  grid-template-areas: "child5 child1 child4" "child2 child6 child3";
}

.grid-container6 .child1 { grid-area: child1; }
.grid-container6 .child2 { grid-area: child2; }
.grid-container6 .child3 { grid-area: child3; }
.grid-container6 .child4 { grid-area: child4; }
.grid-container6 .child5 { grid-area: child5; }
.grid-container6 .child6 { grid-area: child6; background: #000; }
</style>

<div class="grid-ex">
 <div class="grid-container6">
  <div class="child1"><p>This is element #1</p></div>
  <div class="child2"><p>This is element #2</p></div>
  <div class="child3"><p>This is element #3</p></div>
  <div class="child4"><p>This is element #4</p></div>
  <div class="child5"><p>This is element #5</p></div>
  <div class="child6"><p>This is element #6</p></div>
 </div>
</div>

As if that wasn’t cool enough on its own, you can also use this to visually and
neatly make elements span multiple rows and/or columns:

```css
.grid-container7 {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  grid-template-rows: 1fr 1fr 1fr 1fr;
  grid-template-areas: "child5 child1 child1"
                       "child5 child2 child2"
                       "child4 child2 child2"
                       "child3 child3 child6";
}
```

<style type="text/css">
.grid-container7 {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  grid-template-rows: 1fr 1fr 1fr 1fr;
  grid-template-areas: "child5 child1 child1"
  "child5 child2 child2"
  "child4 child2 child2"
  "child3 child3 child6";
}

@media screen and (max-width: 800px) {
 .grid-container7 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-areas: "child3 child4"
   "child3 child1"
   "child2 child1"
   "child6 child5";
 }
}

.grid-container7 .child1 { grid-area: child1; }
.grid-container7 .child2 { grid-area: child2; }
.grid-container7 .child3 { grid-area: child3; }
.grid-container7 .child4 { grid-area: child4; }
.grid-container7 .child5 { grid-area: child5; }
.grid-container7 .child6 { grid-area: child6; background: #000; }
</style>

<div class="grid-ex">
 <div class="grid-container7">
  <div class="child1"><p>This is element #1</p></div>
  <div class="child2"><p>This is element #2</p></div>
  <div class="child3"><p>This is element #3</p></div>
  <div class="child4"><p>This is element #4</p></div>
  <div class="child5"><p>This is element #5</p></div>
  <div class="child6"><p>This is element #6</p></div>
 </div>
</div>

**Note!** This example creates a layout with two columns (see below) on mobile
and screens under 800px, and three columns on larger screens.

For clarity, I should mention that there are also other ways to achieve colspan
and rowspan in CSS grids that don’t rely on `grid-template-areas`.

I mentioned earlier that CSS grids provide an ideal solution for responsive
layouts, and as an example, you can look at the grid above by resizing your
browser window to less than 800px (or wider than 800px, depending). You can
completely change the layout with a simple media query:

```css
@media screen and (max-width: 800px) {
  .grid-container7 {
    display: grid;
    grid-template-columns: 1fr 1fr;
    grid-template-areas: "child3 child4"
                         "child3 child1"
                         "child2 child1"
                         "child6 child5";
  }
}
```

It’s worth mentioning that `grid-template-areas` is a highly programmable layout
mechanism and can easily be used to programmatically control the layout in your
apps. Examples of this will follow in a later blog post.

## What about Internet Explorer?

It is not without a certain amount of frustration that it's 2019 and we still
have to ask this question. We live in difficult times. A time when there are
still people using IE11 on the internet. IE11 (and Edge up to version 15)
implements an early draft of CSS grids, and with this you can [make some things
work](https://docs.microsoft.com/en-us/previous-versions/windows/internet-explorer/ie-developer/dev-guides/hh673533(v=vs.85))
([Mozilla also has some
tips](https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_Grid_Layout/CSS_Grid_and_Progressive_Enhancement)).

Of the things I’ve shown here, for example, `grid-template-areas` is not
possible in these browsers.

I’ve stopped dealing with this problem by simply dropping `display: -ms-grid`.
IE then just shows a bunch of block elements stacked vertically. It’s still
functional, but takes up more space and has no elements side-by-side. Given the
low share of users still using these outdated browsers, my project is perfectly
fine with this solution. Others are advised to check their own browser
statistics before deciding if this works for their projects.

## Learn more

If you’re as enthusiastic as I am, you’ll probably want to learn more about
grids — there are lots of details I haven’t covered here. I highly recommend
[css-tricks.com’s visual
guide](https://css-tricks.com/snippets/css/complete-guide-grid/) — it covers
most of it with visual examples. I regularly use it as a reference.
