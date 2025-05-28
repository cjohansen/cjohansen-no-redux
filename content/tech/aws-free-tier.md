--------------------------------------------------------------------------------
:page/title AWS Free Tier
:page/locale :en
:tech-blog/published #time/ldt "2019-07-24T12:00"
:tech-blog/tags [:tag/aws :tag/efs]
:tech-blog/description

This is a short story about how [EFS](https://aws.amazon.com/efs/) almost
killed our Kubernetes cluster, and how you can avoid ending up in the same
pickle — even if you don't use EFS.

--------------------------------------------------------------------------------
:section/kind :centered
:section/theme :dark1
:section/title AWS Free Tier
:section/body

This is a short story about how [EFS](https://aws.amazon.com/efs/) almost
killed our Kubernetes cluster, and how you can avoid ending up in the same
pickle — even if you don't use EFS.

--------------------------------------------------------------------------------
:section/body

[På Norsk](https://www.kodemaker.no/blogg/2019-07-aws-free-tier/).

**Fall 2018**: A colleague and I set up a Kubernetes cluster on AWS. We went
to production without much fuss, and things worked as expected. Fast forward
5-6 months, and the situation turned upside down overnight.

**Spring 2019**: After running without problems for nearly half a year, our
Kubernetes cluster started to have issues. Well, at least the nodes. The first
symptom we observed was that many of our pods had a short lifespan, with
frequent restarts. After this went on for a while, we occasionally got a
troublesome node that Kubernetes itself couldn't manage to fix.

After inspecting logs from our apps and (what we assumed were) relevant
metrics, we couldn't see any obvious reason for the restarts. Since Kubernetes
still struggled to get control over certain nodes, we were forced on multiple
occasions to literally go into the EC2 console and "physically" shut down
certain nodes. Kubernetes would bring up a new node, which after a short time
would get stuck again. We had a deeper problem.

A bit of digging showed that the triggering symptom was that pods after a short
time started to take unusually long to respond to their health check. This
caused Kubernetes to decide to bring the pods down and then back up. This
played out over and over, and after enough of these loops, the node stopped
responding to the master's commands. What on earth was going on?

## Disk?

One day, for the nth time, I was looking at the output from `kubectl get pods`,
with tears in my eyes, when it hit me — all the pods that had problems used disk
from EFS. Many of the systems we run in this cluster are older and use disk.
As part of the migration to Kubernetes, we concluded that EFS would be good
enough for this disk access (which mostly consisted of config files). It
turned out to be almost true.

AWS sells EFS with the following text among others:

<blockquote class="bq text-content">
  <div class="bq-source">AWS documentation</div>
  <div class="bq-quote">
Throughput and IOPS scale as a file system grows and can burst to higher
throughput levels for short periods of time to support the unpredictable
performance needs of file workloads. For the most demanding workloads, Amazon
EFS can support performance over 10 GB/sec and up to 500,000 IOPS.
  </div>
</blockquote>

Sounds promising, right? But what else does AWS say about throughput?

<blockquote class="bq text-content">
  <div class="bq-source">AWS documentation</div>
  <div class="bq-quote">
<strong>Amazon EFS Bursting Throughput (Default)</strong>

In the default Bursting Throughput mode, there are no charges for bandwidth or
requests and you get a baseline rate of 50 KB/s per GB of throughput included
with the price of storage.
  </div>
</blockquote>

Our use of EFS mainly consisted of reading some config files. Very little
writing. Our data amounts were well below 1GB. So we were guaranteed a whole
0.05 Mbit/s transfer rate.

When we then knew that all the pods that had problems used the same EFS share,
and the transfer from EFS overall was throttled to 0.05 Mbit/s, it was quite
obvious where the problem lay. But why did it take 6 months before this became
an issue?

--------------------------------------------------------------------------------
:section/theme :light1
:section/body

## AWS burstable resources and credits

Many AWS services are so-called "burstable". That is, they have some kind of
baseline performance that you can exceed for a given time period each day. This
system is based on credits — as long as you stay under the burst limit, you
earn credits per hour, and when you go over you use up this credit balance.

EFS has burstable throughput, as explained in [dedicated
documentation](https://docs.aws.amazon.com/efs/latest/ug/performance.html#throughput-modes).
An immediate challenge here is that AWS assumes that the throughput need scales
linearly with the amount of data you store on EFS. For data that is "read mostly"
this does not fit very well. But still, one can ask — why did it take 6 months
before we ran into problems with this?

The concrete problem we ran into is so common that AWS has a dedicated FAQ page
for it: [What's up with EFS burst
credits?](https://aws.amazon.com/premiumsupport/knowledge-center/efs-burst-credits/)
EFS allows you to save up 2.1TB of burst traffic per TB you have on a share,
minimum 2.1TB. When you create a new share **you start with a full credit
balance**.

Here’s what the problem looked like for us:

![AWS EFS credit balance going downhill](/images/burst.png)

So:

- We created an EFS share for a handful of config and log files for older
  services that are mostly read, over 0.05 Mbit/s.
- We started with a credit balance of 2.1TB.
- Since our read demand was just a bit over the default throughput, it took half
  a year before we used up our credits.
- When the credits were gone, pods got throttled I/O when reading from EFS.

Thus, a number of pods failed their health checks and were restarted by
Kubernetes, over and over. This worsened the problem, as some of the services
read quite a bit of data from disk during startup. D'OH!

--------------------------------------------------------------------------------
:section/title What should we learn from this?
:section/body

So what should you, dear reader, take away from this little anecdote? We learned
several things from this blunder:

- **Read the manual**
- AWS’s burstable/credits system is used on many services; understand which,
  how it applies, and not least monitor the credit balance where relevant
- AWS scale is possibly orders of magnitude beyond your own scale
- Assessing risk with a given AWS service is non-trivial

(We already know about "avoid disk in cloud solutions", but all software is not
always in an ideal state).

When summarized like this, it might seem idiotic of us not to check how a
service works before using it. That is valid criticism, but given the amount
of external services and libraries we use every day, it’s probably utopian to
think we can have 100% insight into every detail for each of them. My hope
with this post is that you’ll be a little more careful next time you review
terms for a cloud product you haven’t used before.

Regardless of not knowing about this system, you might wonder why we didn’t
discover it through metrics or alarms? We have plenty of AWS metrics in Datadog,
but apparently so many that we can’t actively monitor all of them. In addition,
our alarms are still manually defined, rather than reporting general anomalies
— "WARNING! EFS Credits dropping sharply." These are challenges we can fix
after the fact, but surely we have more such blind spots we don’t yet know.

## The AWS Free Tier trap

It’s tempting to call this error a case of the "AWS free tier trap," a trap that
manifests itself in one of two ways. One is that things stop working, like in our
case. The other is that your operational costs suddenly skyrocket. Neither is
particularly fun, but unfortunately both are quite common. How do you avoid
them? Read the terms, and monitor AWS’s own metrics, **especially the credit
balance**, where relevant. And cross your fingers that you manage to keep track
of everything.
