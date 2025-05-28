--------------------------------------------------------------------------------
:page/title Devops! Dev? Ops!
:page/locale :en
:tech-blog/published #time/ldt "2019-12-17T12:00"
:tech-blog/tags [:tag/aws]
:open-graph/description

Devops joins the ranks of buzzwords that once meant something but which the
industry has butchered. Rant incoming!

:tech-blog/description

There is currently no shortage of companies in the IT world boasting about doing
Devops. But are they really? If the solution is a "devops team" or job ads for
"a devopser," unfortunately the chances of getting the best that devops has to
offer are slim.

--------------------------------------------------------------------------------
:section/kind :centered
:section/theme :dark1
:section/title Devops! Dev? Ops!
:section/body

[På Norsk](https://www.kodemaker.no/blogg/2019-12-devops/).

There is currently no shortage of companies in the IT world boasting about doing
Devops. But are they really? If the solution is a "devops team" or job ads for
"a devopser," unfortunately the chances of getting the best that devops has to
offer are slim.

--------------------------------------------------------------------------------
:section/body

Let’s start with an approach to a definition: What really is devops?

1. An ops person who knows YAML?
2. A developer who can provision infrastructure with YAML?
3. A culture?

I personally put my bet on option 3.

--------------------------------------------------------------------------------
:section/title Devops in my eyes
:section/theme :light1
:section/body

The term Devops, as far as I know, comes from
[Devopsdays](https://devopsdays.org/about), a conference about software
development, IT operations, and the collaboration between them.

<blockquote class="bq text-content">
  <div class="bq-source"><a href="https://www.kodemaker.no/devops/">Kodemakers pages about devops</a></div>
  <div class="bq-quote">
    <p>
The DevOps movement works to reduce the traditional conflict between
the change focus of software development and the stability focus of IT operations. If you
want to increase the rate of change in software development, investing in DevOps
should be high on your priority list.
    </p>
    <p>
Among the core values in DevOps are culture, automation, measurement, and
sharing.
    </p>
  </div>
</blockquote>

The core here is thus a culture of collaboration, where everyone has the
company’s key goals as their guiding principle, instead of each focusing only on
their own small patch.

--------------------------------------------------------------------------------
:section/title Devops in practice
:section/body

What does this look like in practice? It can of course manifest as "a developer
who can provision infrastructure with YAML," as I somewhat cheekily wrote
earlier. Many companies today have autonomous teams where developers themselves
are responsible for their own production environments, and take ownership of
these as part of the ecosystem around their applications. If the competence in
the team is broad enough, I believe this is one of the most effective ways to
work with software today.

Another way to practice devops is to bridge traditional development and
operations environments. When I worked at NRK a few years ago, this was done
very successfully: IT operations moved from owning the operation of applications
to offering a platform — Mesos and Marathon at first — which enabled developers
to deploy more often and with better predictability. Then operations staff were
embedded in development teams to work closely with developers.

In this way, developers took more ownership of the deployment of their apps
(configuration etc.), while still having highly qualified operations people to
take care of networking, hardware, and all the difficult stuff. At the same
time, a culture of collaboration was developed by having operations people in
development teams who could contribute to debugging, help set up monitoring, and
serve as a bridge to the operations department. Culture development. The result
was more frequent releases, shorter time to fix bugs, and more efficient IT
workers.

--------------------------------------------------------------------------------
:section/title Devops gone wrong
:section/theme :dark1
:section/body

As devops becomes more and more popular, it unfortunately also becomes
increasingly common to implement a misunderstood process and call it devops.
This is nothing new; we in the IT industry love to dress ourselves up with new
buzzwords and movements without really changing how we work. How many companies
don’t adorn themselves with "agile" while their processes are anything but
agile?

Many companies today are looking for a devops developer for their devops team.
When we boil an entire culture down to a job description and/or a separate team,
we lose the benefits that come from changing the culture across the entire
company.

## What does "a devopser" do?

So what does a devops team do? They build platforms. Today we are swimming in
tools from the big tech companies that we ourselves can set up with a cloud
provider or on internal hardware. These solutions are often so complex that they
require a team of infrastructure-interested people to put them together. Here I
think we find the origin of many devops teams: They are not really doing devops
so much as they are doing traditional IT operations, but with modern tools.
Tools that perhaps are designed to solve [much harder problems than they
themselves
have](https://mobile.twitter.com/Carnage4Life/status/1205664370920833025).

![Galactic Algorithm](/images/galactic-algorithm.jpg)

But is there anything wrong with an ops team working on platforms for
developers? Not necessarily. But the whole point of devops is this culture —
everyone works together to deliver to production and help the company reach its
goals. If you have a devops team that enjoys writing Terraform and configuring
Kubernetes and Istio more than actually making sure the company hits its goals,
then calling it "devops" is wrong.

Is it wrong for some to spend a lot of time setting up Kubernetes and the
ecosystem around it in a way that fits the company’s needs? No, maybe not. But
do we really keep the company’s needs in mind? Do we remember to think about
[MVP](https://en.wikipedia.org/wiki/Minimum_viable_product)? This principle also
applies to infrastructure — and I’m not saying it should be insecure and rickety
— it should be "viable." But it should not be more complicated than it needs to
be. And we must remember that it should be pleasant for developers to use — they
are the customers of such a solution. If the devops team has been locked away
for months cooking up a solution that developers hate to deploy on, then we have
missed the point.

--------------------------------------------------------------------------------
:section/title Devops at its best
:section/body

I want to leave you with a recommendation. Everyone working with software
development today, whether operationally as a developer, in operations, or in
some business function/leadership role close to IT: Read [The Phoenix
Project](https://www.amazon.com/Phoenix-Project-DevOps-Helping-Business/dp/0988262592).
This novel beautifully and vividly exemplifies what a devops culture can look
like. I’m personally looking forward to reading the sequel [The Unicorn
Project](https://www.amazon.com/Unicorn-Project-Developers-Disruption-Thriving-ebook/dp/B07QT9QR41)
over the holidays, which from reviews appears to be just as good.

Happy devopsing!
