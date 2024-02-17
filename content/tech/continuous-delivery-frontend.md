--------------------------------------------------------------------------------
:page/title Continuous Delivery on the Frontend
:tech-blog/published #time/ldt "2022-08-17T12:00"
:tech-blog/tags [:tag/frontend]
:open-graph/description

How to ensure that your users are on the latest version of your long-lived
frontend application?

:tech-blog/description

You have a Single Page Application (SPA). Users typically keep the app open in
their browser for a long time - many days, or even weeks. At the same time,
you're pushing out new versions several times a day. How on earth do you ensure
that people don't end up stuck on an old frontend that may have bugs you've
already fixed, or are using an outdated API client? Well, here's one suggestion.

--------------------------------------------------------------------------------
:section/kind :centered
:section/theme :dark1
:section/title Continuous Delivery on the Frontend
:section/body

You have a Single Page Application (SPA). Users typically keep the app open in
their browser for a long time - many days, or even weeks. At the same time,
you're pushing out new versions several times a day. How on earth do you ensure
that people don't end up stuck on an old frontend that may have bugs you've
already fixed, or are using an outdated API client?

There are several ways to fix this problem, and in this post I will detail the
approach my team took.

--------------------------------------------------------------------------------
:section/title Step 1: Distinguish between different versions
:section/body

In order to determine whether a user is using an outdated version of our app, we
need some form of versioning. This can be anything, preferably something that
can be generated. The only requirement is that you get a new version each time
you deploy. Examples like a git sha or a timestamp are easily available (see
below for a better suggestion).

Once you have chosen a string that represents a version, you need to expose it
so that the client can query the server for the current version. One way to do
this is to put the string in a file. This also works with serverless hosting of
frontend apps (S3 or similar).

In other words, you can go a long way with:

```sh
git rev-parse --short HEAD > public/version.txt
```

--------------------------------------------------------------------------------
:section/title Step 2: Note the running version at startup
:section/theme :light1
:section/body

For your app to know if a new version is available, it needs to know what the
current version is. So, as your app starts up, you do something like:

```js
var APP = {};

function getVersion() {
  return fetch("/version.txt")
    .then(res => res.text());
}

getVersion().then(version => APP.currentVersion = version)
```

The benefit of this approach is that we can use exactly the same code snippet to
check for new versions. The disadvantage is that the version is not delivered
atomically with the script. You can fix this by bundling the version directly
into the script:

```sh
echo ';window.APP = {version:"'$(cat version.txt)'"};' >> app.js
```

--------------------------------------------------------------------------------
:section/title Step 3: Check for new versions
:section/body

After the app has started up, it must periodically check the server for a newer
version. This can be done by polling the file we checked at startup, and
comparing the version with the one we found at startup. If they are different, a
new and (hopefully) better version awaits. For now, let's just note this:

```js
function pollVersion() {
  getVersion().then(version => {
    if (APP.currentVersion !== version) {
      APP.needsRefresh = true;
    } else {
      setTimeout(pollVersion, 60000);
    }
  });
}

pollVersion();
```

--------------------------------------------------------------------------------
:section/title Step 4: Help the user move to the new version
:section/theme :dark1
:section/body

It's time to actually load a new version. Ideally, this should happen as quickly
as possible, but what if the user is in the middle of filling out a form?

A fairly common solution is to have a discreet popup saying "new version
available - click to update", but that's suboptimal for at least two reasons:

- The user may be in the middle of filling out a form but doesn't realize
  they'll lose their work by clicking the button.
- It's left up to the user when to upgrade.

No, we need to upgrade automatically, and as soon as possible, but at a safe
time. If you have good enough control over the state of the interface to know
whether the user is working on a form or doing something that shouldn't be
interrupted, you might be able to do a `location.reload()` as soon as you have a
safe state. But this is risky because you still risk losing peripheral state for
your app such as scroll position, highlighted text, etc.

A safe time to sneak in a reload is when navigating between "pages". When
navigating, we can check `APP.needsRefresh`. If it's set, we do
`location.href = url`, which forces a reload of the HTML page (and thus sends
the user to the new version) instead of `history.pushState`:

```js
function navigate(location) {
  var url = router.url(location);

  if (APP.needsRefresh) {
    location.href = url;
  } else {
    renderApp(location);
    history.pushState(null, "", url);
  }
}
```

And there you have it: a simple and pragmatic solution to ensure that your users
are mostly on the latest version of your frontend. This solution assumes that
you use URLs to address the state in your app, but you already do, don't you?

--------------------------------------------------------------------------------
:section/title Improvement 1: Preloading
:section/body

After step 3, we can make a clever little move: preload the version we're soon
going to force the user onto. If the app is just one JavaScript bundle, you can
use [rel="preload"](https://developer.mozilla.org/en-US/docs/Web/HTML/Preloading_content):

```js
function preload(url, type) {
  var preloadLink = document.createElement("link");
  preloadLink.href = url;
  preloadLink.rel = "preload";
  preloadLink.as = type;
  document.head.appendChild(preloadLink);
}

preload("/ny-app.js", "script");
```

This assumes that you know the URL of the new version: either it's static (not
so good for caching), or the URL can be calculated with the version string, or
the version endpoint returns data that provides the URL, for example:

```js
{
  "appFile": "/bundles/e35fe20b.js"
}
```

If the app consists of several script files, and maybe a CSS file or two, you
can still use the technique above, just include a list of resources to preload
instead:

```js
version.appResources
  .forEach(resource => preload(resource.url, resource.type));
```

A cheeky brute-force approach to preloading is to fetch the entire app in an
invisible iframe that you remove as soon as it's loaded:

```js
frame = document.createElement("iframe");
frame.src = "/";
frame.style.width = 0;
frame.style.height = 0;
frame.addEventListener("load", () => document.body.removeChild(frame));
document.body.appendChild(frame);
```

This suggestion should perhaps be taken with a grain of salt as you'll be
running a fully functioning client in the background (even though it's removed
again).

--------------------------------------------------------------------------------
:section/title Improvement 2: Selective updates
:section/theme :dark1
:section/body

It's conceivable that your frontend is deployed without the user really needing
to refresh the page. The CI server may build and deploy regardless of what
happens - whether you just change the readme, or add some tests. To avoid
forcing a refresh on users in these situations, you can generate a version
string that represents the content of the app bundle rather than the time it was
built.

If your app is just a single JS file, this can be done as follows:

```sh
cat app.js | shasum  -a 256 | awk '{print $1}' > version.txt
```

If the app consists of multiple files, you can concatenate all the files
together before creating a checksum.

This way, the client will only be updated when there are actual changes in the
files that make up the app.
