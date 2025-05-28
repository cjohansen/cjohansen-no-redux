--------------------------------------------------------------------------------
:page/title When the AWS Bill Skyrockets
:page/locale :en
:tech-blog/published #time/ldt "2019-09-25T12:00"
:tech-blog/tags [:tag/aws]
:open-graph/description

What do you do when you discover that the AWS bill is **much** higher than you
expected?

:tech-blog/description

It's easy to get up and running in the cloud. Unfortunately, it's also much too
easy to end up in a situation where you're practically throwing money in
Amazon's direction. What do you do when you discover that the AWS bill is
**much** higher than you expected?

--------------------------------------------------------------------------------
:section/kind :centered
:section/theme :dark1
:section/title When the AWS Bill Gets Sky-High
:section/body

[På Norsk](https://www.kodemaker.no/blogg/2019-09-aws-regning/).

These days many are moving their systems to the cloud. The promises are many:
operations will almost run by themselves, systems become instantly "web scale",
and thanks to the "pay as you go" model, costs should go down —
drastically.

In practice, none of these things are true unless you work to make them so.
Still, I believe what hits most projects hardest is that once things are up and
running, it turns out Amazon (or Google, Microsoft, etc.) wants obscene amounts
of money for their services. So what should you do when you discover your AWS
bill has suddenly become sky-high?

--------------------------------------------------------------------------------
:section/title Send the Bill to the Right Place
:section/body

AWS billing often goes through two phases: when developers start experimenting,
usually one of them puts in their private credit card and fronts the expenses.
When the project moves into a more established phase, billing changes so it goes
directly to a manager or a dedicated department if you're in a large enough
company. The end result is often that those paying the bill are not the same as
those developing the services or who have an overview of what the bill actually
covers.

How big an AWS bill is too big? $1000? $10,000? $100,000? It depends on what you
get back for the money. But what kind of assumptions can a finance person make to
assess this? Here lies the source of a rather stubborn feedback loop: Developers
spin up servers and services while the bill grows and grows, and only when the
amount gets serious does the alarm go off somewhere completely different in the
company.

Make sure the bill goes through someone close to the project who can evaluate
whether the costs can be justified based on what is delivered.

--------------------------------------------------------------------------------
:section/title Hold Developers Accountable
:section/theme :light1
:section/body

Who knows best what is delivered other than those who actually **deliver**? Give
developers access to billing and include the report in suitable regular rituals,
such as retrospectives. A project manager might not raise an eyebrow over a
monthly bill of a few thousand polished dollars, but a developer who knows that
what has been delivered so far could just as well run on a $50 VPS will get tics
from those same numbers.

On the other hand, developers should have an active relationship to costs when
designing and shaping solutions. In a discussion about whether something should
be solved with one (or more) [EC2](https://aws.amazon.com/ec2/) instances or a
[DynamoDB](https://aws.amazon.com/dynamodb/) table, a
[Lambda](https://aws.amazon.com/lambda/) and
[ApiGateway](https://aws.amazon.com/api-gateway/), the price **for the current
volume** can well be the deciding factor.

--------------------------------------------------------------------------------
:section/title Keep an Eye on It!
:section/body

Closely related to the previous point. For many, high bills for cloud services
come as a surprise. That can only happen if no one is monitoring before it has
already gone too far. If everyone on the project looks at billing once a month,
you will quickly notice if costs are rising faster than the value delivered by
the services being built. A review of the billing report can also reveal obvious
measures for savings.

I have heard **several** stories about sky-high cloud bills caused by forgetting
to turn off things that were spun up "just to try." This doesn't happen if
someone with enough project knowledge actually follows the bill. So make sure to
go through the billing report with the team monthly.

--------------------------------------------------------------------------------
:section/title What is Expensive?
:section/theme :dark1
:section/body

Before you start going through the AWS bill and get heart palpitations from the
amounts, it's good to have an idea of what running your service **should** cost.
What can it cost? When does it become **too** expensive? Many projects have
absolutely no answers to these questions and therefore need the bill to become
very high before it feels right to react.

If you move your services from onsite hosting to the cloud, you have a kind of
target. I assume you move the services to gain new opportunities and achieve a
degree of freedom and flexibility that is difficult to recreate locally, but
costs should also be part of the picture. It is reasonable to expect that
operations will not become much more expensive in the cloud. Rather the opposite.

If you build new services, it is harder to assess what is the right price level.
But remember that it is not your private wallet paying the bill.

A useful exercise that puts prices in perspective is converting savings in
operations into expenses for labor or time lost to time to market. Spending
15,000 kroner on a consultant to reduce your AWS bill by $50 a month is probably
not a great deal — it won't break even before three years.

Operational costs can also easily be assessed against revenue from the systems
being operated. This calculation should ideally be positive for the company.

--------------------------------------------------------------------------------
:section/title Free Suddenly Becomes Expensive
:section/body

Many of Amazon's services have a startup mode that is artificially cheap, or even
free. I have previously told
[a story about when free suddenly became a nightmare](/aws-free-tier/).

--------------------------------------------------------------------------------
:section/title Use Multiple Accounts
:section/theme :light1
:section/body

If you want to assess the reasonableness of a bill, it is crucial to understand
**what** you are paying for. I highly recommend being generous with creating new
accounts to separate services that have nothing to do with each other. An
account costs nothing, and they can be grouped under an organization account,
where you can also get so-called [consolidated
billing](https://docs.aws.amazon.com/awsaccountbilling/latest/aboutv2/consolidated-billing.html),
i.e. one consolidated bill for multiple accounts. By linking the accounts in an
organization, you can also use IAM for cross-account access control, and more.
This is really a no-brainer. Consolidated billing gives you one bill, but also
the ability to analyze costs per account and share volume discounts across
accounts.

What should you make separate accounts for? At a minimum, I recommend using a
dedicated account for production and dedicated accounts for any development and
staging/test environments. This allows you to easily see costs for individual
environments, and also gives you a better foundation for a secure solution with
good access control — but that's another blog post.

If you are a large company with multiple departments/products all on AWS, each
should have separate accounts for each of their environments. Everything that
doesn't need much interaction should be separated. Err on the side of too many
accounts rather than too few; you can always merge things later. Splitting after
things have become too entangled is often much harder.

--------------------------------------------------------------------------------
:section/title Tags, Tags, Tags
:section/body

Tagging is the traditional advice for knowing what things are used for, since you
can break down billing by tags. Separate accounts give the best separation —
not everything can be tagged, and tagging requires more active effort than
creating resources in the right account. But one doesn't exclude the other.
Tag everything that can be tagged. If you have microservices, use their names to
tag all relevant resources, and use other relevant classifications as
appropriate. Tags help you further understand where the money goes.

--------------------------------------------------------------------------------
:section/title Automate Everything
:section/theme :dark1
:section/body

Automation is relevant to billing in that it gives you even better traceability.
As I mentioned earlier, I have talked to many who have paid dearly for things
they forgot to turn off. This is much easier to imagine with manually provisioned
resources.

Automation also makes it easier to ensure resources are tagged as far as
practical. Automation can even enforce internally formulated rules on how things
should be tagged. Automation obviously has a cost itself, and "everything" is
often not a practically achievable solution — but since automation also helps
with so much else, I strongly recommend being on top of this early in your
project.

On the other hand, too high-level automation can also abstract away/hide costs.
It is not easy to hit the target.

--------------------------------------------------------------------------------
:section/title Take Down Test Environments
:section/body

The production environment must always be available. But this probably does not
apply to the other environments. If you have a fully duplicated dev environment,
there is a relatively high chance it does not need to run in the evenings and
nights, or on weekends.

If your dev environment only "runs" from 07:00 to 18:00 Monday to Friday, it
runs only 55 of the week's 128 hours — 42% of the time. Not everything is paid by
the hour, but if you have many things that are, it can quickly be worth investing
some time in creating a solution to bring things up and down around working
hours. Just remember to create a manual override so that you can keep things
running in urgent situations.

--------------------------------------------------------------------------------
:section/title Use the Right Service
:section/theme :light1
:section/body

Choosing the right service matters a lot for the bill. You can easily spin up
an [EC2](https://aws.amazon.com/ec2/) instance, but it can be more expensive
than running something similar in a container or on a [Fargate](https://aws.amazon.com/fargate/)
cluster.

Likewise, having a database in a managed solution like
[RDS](https://aws.amazon.com/rds/) or DynamoDB is more expensive than running
your own database on a well-optimized EC2 instance.

It pays off to spend time investigating the actual needs before choosing which
services to use. Some services look expensive upfront but provide scalability
and robustness that make them more cost-effective in the long run.

--------------------------------------------------------------------------------
:section/title Use Budgets and Alerts
:section/body

AWS provides
[Budgets](https://docs.aws.amazon.com/cost-management/latest/userguide/budgets-managing-costs.html)
that let you set a monthly budget and send alerts when costs exceed a certain
threshold. This helps avoid surprises and keeps everyone informed.

--------------------------------------------------------------------------------
:section/title Summary
:section/theme :dark1
:section/body

- Have the bill go to someone close to the project.
- Involve developers actively with the bill.
- Review billing reports regularly.
- Use multiple accounts for separation.
- Tag resources diligently.
- Automate resource management and tagging.
- Shut down non-production environments when not in use.
- Choose the right AWS services for your needs.
- Set budgets and alerts to monitor costs.
