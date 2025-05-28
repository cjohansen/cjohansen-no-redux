--------------------------------------------------------------------------------
:page/title Zero downtime Kubernetes deployments
:page/locale :en
:tech-blog/published #time/ldt "2022-09-17T12:00"
:tech-blog/tags [:tag/kubernetes :tag/continuous-delivery]
:open-graph/description

Deploying to Kubernetes without downtime doesn't happen by itself. Here are some
tips along the way.

:tech-blog/description

When you deploy your app to that spaceship of a rig called Kubernetes, it
happens without any form of downtime, right? Bad news: unless you have actively
worked for it, your services most likely have some downtime during deployment.
But why?

--------------------------------------------------------------------------------
:section/kind :centered
:section/theme :dark1
:section/title Zero downtime Kubernetes deployments
:section/body

[På Norsk](https://www.kodemaker.no/blogg/2020-01-kubernetes-deployment-nedetid/).

When you deploy your app to that spaceship of a rig called Kubernetes, it
happens without any form of downtime, right? Bad news: unless you have actively
worked for it, your services most likely have some downtime during deployment.
But why?

--------------------------------------------------------------------------------
:section/title Is there a problem?
:section/body

Maybe you think you don't have downtime problems. Let the numbers speak before
we solve a problem we don't have. The experiment is quite simple: Start a shell
where you poll your service with e.g. `watch`:

```sh
watch -n 1 curl -i https://myservice.mycluster/health
```

Point it at something that responds quickly, like your health check endpoint.

```sh
kubectl apply -f ci/service.yml
```

Then keep an eye on the `watch` process. Do you get one or more empty responses?
Congratulations — you have a problem with your deployment. Didn't get any
errors? Repeat the exercise with a shorter interval:

```sh
watch -n 0.5 curl -i https://myservice.mycluster/health
```

NB! Some `watch` implementations do not support intervals under 1 second.
Ubuntu's version supports down to `0.1`, so run this from a Docker container
with Ubuntu if you don't have access to something that works locally.

--------------------------------------------------------------------------------
:section/title Problem 1: Liveness og Readiness
:section/theme :light1
:section/body

Your pods should have at least one of `livenessProbe` and `readinessProbe`—
probably both. In short, the first controls when Kubernetes restarts your pod,
while the second controls when Kubernetes routes traffic to the pod from a
service. If you don’t define either of these, Kubernetes will send traffic to
your pod as soon as it thinks it’s okay, which is very likely too early.

A well-configured `readinessProbe` and/or `livenessProbe` ensures that your pod
does not receive traffic before it is ready.

--------------------------------------------------------------------------------
:section/title Problem 2: The dying pod
:section/body

The second problem is a bit less obvious and practically concerns your pods
receiving traffic from Kubernetes while they are shutting down. Slow clap.

When a pod is shutting down it must be removed from several places:

1. `kubelet` must shut down the pod
2. `kube-proxy` on all nodes in the cluster must remove the pod’s IP address
   from `iptables`
3. The pod must be removed from the `endpoints` of the service it is part of

It was with mild shock that I realized Kubernetes does not even try to
orchestrate this in any way other than doing everything in parallel. It is,
after all, a distributed system! Thus, there is a high chance that a service
gets told to remove your pod from its endpoints **after** the pod has started
shutting down, or that traffic reaches the pod’s IP after shutdown. Hooray.

[This article](https://blog.gruntwork.io/delaying-shutdown-to-wait-for-pod-deletion-propagation-445f779a8304)
goes more in depth on why this is the case, without fully convincing me that it
**must** be this way, but it is how it is, so what can we do?

The hack suggested in that article is to ensure the pod’s shutdown takes enough
time so that it almost certainly does not shut down _before_ it has been removed
from `iptables` and `endpoints`. How you do this depends somewhat on what runs
in your pod, but a simple and YOLO approach is to run `sleep` as a `preStop`:


```yml
lifecycle:
  preStop:
    exec:
      command: ["/bin/bash", "-c", "sleep 10"]
```

You want this one under each element under `containers`.

--------------------------------------------------------------------------------
:section/title Problem 3: Connection draining
:section/theme :dark1
:section/body

Ok, so now we have avoided traffic _before_ the pod is ready, and _after_ it is
dead. But what about the traffic being processed _at the very moment of death_?
Here you will still see connection drops unless you actively ensure _connection
draining_, meaning your service itself manages to slow down shutdown until all
"in-flight" requests are completed.

Unfortunately, I don’t have a quick hack for this, as it depends on the
technology and must be handled at the application level. But if you get this
right, you have every reason to give yourself a pat on the back, because then
you have achieved deployment nirvana: absolutely zero downtime.
