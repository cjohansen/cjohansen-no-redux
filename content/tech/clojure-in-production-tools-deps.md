--------------------------------------------------------------------------------
:page/title Clojure in Production with tools.deps
:tech-blog/published #time/ldt "2019-04-14T12:00"
:tech-blog/updated #time/ldt "2019-08-16T12:00"
:tech-blog/tags [:tag/clojure :tag/tools.deps]
:tech-blog/description A lean and fast setup for packaging Clojure applications for production using tools.deps
--------------------------------------------------------------------------------
:section/kind :centered
:section/theme :dark1
:section/title Clojure in Production with tools.deps
:section/body

In this post I'll show you how my project is packaging and running Clojure apps
in production, using [`tools.deps`](https://github.com/clojure/tools.deps.alpha)
as a build tool. Credit where credit's due: my awesome colleague [Alf Kristian
Støyle](https://www.kodemaker.no/alf-kristian) did the heavy lifting on this
setup.

--------------------------------------------------------------------------------
:section/body

Our setup at a glance:

- Use `tools.deps` to manage dependencies and build class paths
- AOT compile sources (and possibly some dependencies) to class files in `classes`
- Place all dependency jars in `lib`
- Package `classes` and `lib` in a Docker container
- Run the app with `java -cp "classes:lib/*" our-app.core`

It is a very simple setup that requires very little additional tooling. We use
only one library on top of `tools.deps`,
[badigeon](https://github.com/EwenG/badigeon). Badigeon has a very nice
[bundler](https://github.com/EwenG/badigeon/blob/master/src/badigeon/bundle.clj#L140)
that can collect all kinds of dependencies supported by `tools.deps` for further
packaging. Importantly, this helps us with [git
libs](https://www.clojure.org/guides/deps_and_cli#_using_git_libraries), which
we use for internal libraries and more. Since we already have it on the
classpath, we also use Badigeon's AOT compiler, which is a thin wrapper over
Clojure's that provides a few niceties like ensuring that the target directory
exists before putting files in it.

--------------------------------------------------------------------------------
:section/theme :light1
:section/body

`deps.edn`:

```clj
{:paths ["src"]
 :deps {org.clojure/clojure
        {:mvn/version "1.10.1-beta2"}}
 :aliases
 {:build
  {:extra-paths ["build"]
   :extra-deps
   {badigeon/badigeon
    {:git/url "https://github.com/EwenG/badigeon.git"
     :sha "dca97f9680a6ea204a2504c4414cafc4ba182a83"}}}}}
```

`build/package.clj`:

```clj
(ns package
  (:require [badigeon.bundle :refer [bundle make-out-path]]
            [badigeon.compile :as c]))

(defn -main []
  (bundle (make-out-path 'lib nil))
  (c/compile 'our-app.core {:compile-path "target/classes"}))
```

--------------------------------------------------------------------------------
:section/body

Now you can stage classes and jars like this:

```sh
clojure -A:build -m package
```

The `Dockerfile` uses a specific version of the relevant JDK image. Never use
fleeting tags like `latest` for a production build - you want those to be
predictable and repeatable:

```docker
FROM openjdk:11.0.2-slim

ADD target/lib/lib /app/lib
ADD target/classes /app/classes

WORKDIR /app

CMD java $JAVA_OPTS -cp "classes:lib/*" our-app.core
```

Externalizing JVM parameters with `$JAVA_OPTS` allows us to tweak runtime
characteristics without building a new artifact. Here's an example of setting it
from a Kubernetes deployment descriptor to configure JMX and heap memory:

```yaml
containers:
  - name: our-service
    image: our.repo.com/our-app:ae31ade5ba
    env:
      - name: JAVA_OPTS
        value: "-Dcom.sun.management.jmxremote.rmi.port=9090
                -Dcom.sun.management.jmxremote=true
                -Dcom.sun.management.jmxremote.port=9090
                -Dcom.sun.management.jmxremote.ssl=false
                -Dcom.sun.management.jmxremote.authenticate=false
                -Dcom.sun.management.jmxremote.local.only=false
                -Djava.rmi.server.hostname=localhost
                -Xms128m
                -Xmx128m"
```

## Tying it all together

We use a `Makefile` to tie everything together, so we can do things like:

```sh
make docker # AOT compiles first if sources have changed
```

Here's something to get you started:

```make
VERSION:=$(shell git rev-parse --short=10 HEAD)

target:
    mkdir -p target

target/classes/our_app/core.class: deps.edn src/**/* target
    clojure -A:build -m package

build: target/classes/our_app/core.class

docker: target/classes/our_app/core.class
    docker build -t our-app:$(VERSION) .

clean:
    rm -fr target

.PHONY: build docker clean
```

This is a very straight-forward approach that uses little tooling, has few
concepts to understand, no runtime component, and starts quickly.

--------------------------------------------------------------------------------
:section/theme :light1
:section/body

## Alternatives

There are several alternatives around for packaging Clojure apps. One of the
first approaches we tried was using Capsule and One-JAR, through
[pack.alpha](https://github.com/juxt/pack.alpha/). `pack.alpha` makes it very
easy to add packaging to your `tools.deps` project. It is very nice for building
["skinny jars" for libraries](/tools-deps-figwheel-main-devcards-emacs/) but for
single jar deployments, the resulting jar will likely include more than you
bargained for.

[Capsule](http://www.capsule.io) jars are basically a lightly packaged build
tool, and it boasts features like selecting the JVM version at startup,
installing dependencies on the run and more. None of those features are
desirable for reproducible application server deployments.

[One-JAR](http://one-jar.sourceforge.net) loads all the bytecode into memory up
front _"making for improved runtime performance"_. The problem is, loading
bytecode is very unlikely a bottleneck, and not in any real need of optimizing.
Besides, an application likely ships with dependencies from which it only uses a
few functions. Prematurely loading all that bytecode into memory is pretty much
guaranteed to waste resources. This is especially true because One-JAR loads it
into heap space.

I'm not saying that these tools don't have their use cases, I'm just saying that
they didn't fit our requirements. They gave us too slow startup times, and the
One-JAR solution landed us at a baseline heap size of a whopping 250 megabytes.
The solution presented above puts us at just around 30MB after startup, and
between 64MB and 96MB after running for a few days - and that's without any of
the bytecode from before, which no is longer loaded into heap space.
