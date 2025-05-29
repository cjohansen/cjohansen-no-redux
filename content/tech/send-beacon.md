--------------------------------------------------------------------------------
:page/title First-Class Analytics
:page/locale :en
:tech-blog/published #time/ldt "2023-07-06T12:00"
:tech-blog/tags [:tag/html :tag/javascript]
:tech-blog/description

Web analytics for 100% of users without dreadful consent dialogs is possible —
here’s how.

--------------------------------------------------------------------------------
:section/kind :centered
:section/theme :dark1
:section/title First-Class Analytics
:section/body

Web analytics for 100% of users without dreadful consent dialogs is possible —
here’s how.

--------------------------------------------------------------------------------
:section/body

[På Norsk](https://www.kodemaker.no/blogg/2023-07-send-beacon/).

To understand how our solutions work in reality, what users are most interested
in, and what they won’t bother using — we need some form of analytics. We also
need diagnostics that tell us whether the frontend behaves correctly on devices
"in the wild." Unfortunately, most commercial solutions care little about
privacy, many store data in the USA, and most are partially or completely
non-compliant with GDPR. The solution is to build our own collector.

By building our own collector, we control what data is collected about the user.
By sending to our own backend, it becomes harder for browsers to distinguish the
traffic from other traffic, and ad blockers no longer pose as big a threat. If
we make sure to only collect anonymous data, we also don’t need to ask users for
consent with huge, ugly dialogs full of "legalese."

"Aren’t we going to copy Google Analytics?" you might wonder. Relax, we’re not
going to copy Google Analytics. We’re only building a collector. If the data is
anonymous, we can safely send it to any analytics tool to get access to all the
great tools for making reports, funnel analysis, etc. Mixpanel, Google
Analytics, Sentry.io, and other tools have well-documented APIs for sending
data.

## sendBeacon

The browser has a built-in function dedicated to collecting analytics and
diagnostics. `sendBeacon(url, data)` makes an asynchronous `POST` to `url` with
"a small amount of data"
([MDN](https://developer.mozilla.org/en-US/docs/Web/API/Navigator/sendBeacon)).
The nice thing about `sendBeacon` is that you can safely call it even if the
user is leaving the page, because the browser will complete the request in the
background — in sharp contrast to `XMLHttpRequest`, which in the same case gets
canceled by the browser.

So to collect events describing the use of your website, gather some context and
fire off a call to `sendBeacon`:

```js
sendBeacon("/stats", JSON.stringify({
  "type": "download-csv",
  "context": {
    "build-date": "2023-04-24T12:36:58.429635Z",
    "git-sha": "af53678a56",
    "language": "en-US",
    "referrer": "http://localhost:8084/",
    "screen-size": "390x844",
    "tracking-id": "59f1e4bf-69eb-41d3-b45d-68a24d46793a",
    "url": "http://localhost:8084/...",
    "viewport-size": "390x844"
  }
}));
```

The example shows an event from the app I work in daily. We gather some context,
but not enough to [fingerprint](https://coveryourtracks.eff.org/) the user. We
also have `type: "page-view"` that fires on all navigation within the app.

## The Receiver

Sending the event from the client is only half the job. You also need an
endpoint that receives the data and either stores it — or forwards it to an
appropriate tool.

Google Analytics is a popular tool for web analytics. They have a
[well-documented
API](https://developers.google.com/analytics/devguides/collection/protocol/v1/parameters)
for ingestion. One possible server-side implementation is to map the client data
to something that fits Google's spec, then forward it. Afterwards, you can use
many of Google Analytics’ tools as usual — except for retargeting and other
features that violate users' privacy.

## Error Reports

The same approach can be successfully used to collect diagnostics from the
frontend. Tools like [sentry.io](https://sentry.io) are useful but can conflict
with privacy — and not least can be blocked by ad blockers and similar tools. If
there’s one thing you don’t want to lose to an ad blocker, it’s reports that
your frontend crashes.

To collect unexpected errors, you need a listener on the `error` event triggered
on `window`:

```js
window.addEventListener("error", function (e) {
  sendBeacon("/errors", JSON.stringify({
    // ...
  }));
});
```

Here you have the error object, information about the browser, and all other
useful information about your app. On the server, you can forward the data to
the [Sentry ingestion API](https://docs.sentry.io/api/), or come up with
something custom. In my project, we have this handler:

```clj
(defn handle-log [{:keys [body] :as request}]
  (let [user-agent (get-in request [:headers "user-agent"])]
    (when-not (exclude-ua? user-agent)
      (doseq [{:keys [level message data]} body]
        (log/log (get-log-level level)
                 message
                 (cond-> data
                   user-agent (assoc :userAgent user-agent)
                   (:stack data) (update :stack stacktrace/improve-stacktrace)
                   (:exception data) (assoc :stack (-> data :exception :stack stacktrace/improve-stacktrace))))))
    {:status 202}))
```

**TL;DR:** We receive data from the client and log it. The data then ends up in
Datadog along with the rest of our logs, so we find frontend logs and errors in
the same place as backend data. And it only took a few hours to set up.

Building good "first-party" solutions for web analytics and diagnostics isn’t
demanding. Once you have the ingestion endpoint in place and can guarantee only
anonymous data is collected, it’s safe to forward data to third-party tools for
analysis. [`sendBeacon`](https://caniuse.com/?search=sendbeacon) works in all
modern browsers, so there’s no reason to hesitate!
