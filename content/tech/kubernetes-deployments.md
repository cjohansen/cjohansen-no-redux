--------------------------------------------------------------------------------
:page/title Zero downtime Kubernetes deployments
:page/locale :nb
:tech-blog/published #time/ldt "2022-09-17T12:00"
:tech-blog/tags [:tag/kubernetes :tag/continuous-delivery]
:open-graph/description

Deployment til Kubernetes uten nedetid kommer ikke av seg selv. Her er noen tips
på veien.

:tech-blog/description

Når du deployer appen din til det romskipet av en rigg som er Kubernetes så
skjer det uten noen form for nedetid, ikkesant? Dårlige nyheter: med mindre du
har gått veldig aktivt inn for det har tjenestene dine svært sannsynlig litt
nedetid under deployment. Men hvorfor?

--------------------------------------------------------------------------------
:section/kind :centered
:section/theme :dark1
:section/title Zero downtime Kubernetes deployments
:section/body

Når du deployer appen din til det romskipet av en rigg som er Kubernetes så
skjer det uten noen form for nedetid, ikkesant? Dårlige nyheter: med mindre du
har gått veldig aktivt inn for det har tjenestene dine svært sannsynlig litt
nedetid under deployment. Men hvorfor?

--------------------------------------------------------------------------------
:section/title Is there a problem?
:section/body

Kanskje tenker du at du ikke har problemer med nedetid. La oss få tallene i tale
før vi løser et problem vi ikke har. Eksperimentet er nokså enkelt: Start et
shell der du poller tjenesten din med feks `watch`:

```sh
watch -n 1 curl -i https://myservice.mycluster/health
```

Denne er grei å sette opp mot noe som svarer fort, for eksempel helsesjekken
din. Deretter gjør du et deployment:

```sh
kubectl apply -f ci/service.yml
```

Så følger du bare med på `watch`-prosessen. Får du én eller flere tomme svar?
Gratulerer -- du har et problem med deploymentet ditt. Fikk du ingen feil?
Gjenta øvelsen med et kortere intervall:

```sh
watch -n 0.5 curl -i https://myservice.mycluster/health
```

NB! Noen `watch`-implementasjoner støtter ikke intervaller under 1 sekund.
Ubuntu sin går ned til `0.1`, så kjør dette fra en Docker container med Ubuntu
om du ikke har tilgang til noe som virker lokalt.

--------------------------------------------------------------------------------
:section/title Problem 1: Liveness og Readiness
:section/theme :light1
:section/body

Pod-ene dine bør ha minst én av `livenessProbe` og `readinessProbe` --
sannsynligvis begge. Kort fortalt vil den første styre når Kubernetes restarter
poden din, mens den andre styrer når Kubernetes ruter trafikk til poden fra en
service. Dersom du ikke definerer noen av disse vil Kubernetes sende trafikk til
poden din så fort den selv mener det er greit, noe som svært sannsynlig er for
tidlig.

Vel-konfigurert `readinessProbe` og/eller `livenessProbe` sørger altså for at
pod-en din ikke får trafikk før den er klar for det.

--------------------------------------------------------------------------------
:section/title Problem 2: The dying pod
:section/body

Det andre problemet er litt mindre åpenbart, og handler i praksis om at podene
dine får trafikk av Kubernetes mens de er på vei ned. Slow clap.

Når en pod skal ned så skal den bort fra flere steder:

1. `kubelet` skal stenge ned poden
2. `kube-proxy` på alle nodene i clusteret skal fjerne podens IP-adresse fra
   `iptables`
3. Poden skal meldes ut av `endpoints` for servicen den er en del av

Det var med mildt sjokk det gikk opp for meg at Kubernetes ikke gjør et døyva
forsøk på å orkestrere dette på noe annet vis enn å gjøre alt i parallell. Det
er jo et distribuert system, må vite! Dermed er det stor sjans for at en service
får beskjed om å melde pod-en din ut av sine endpoints **etter** at pod-en har
gått i shutdown. Eller at det kommer trafikk til podens IP etter shutdown.
Hurra.

[Denne artikkelen](https://blog.gruntwork.io/delaying-shutdown-to-wait-for-pod-deletion-propagation-445f779a8304)
går mer i dybden på hvorfor dette er som det, uten å helt overbevise meg om at
det **må** være sånn, men det er nå en gang sånn, så hva skal vi gjøre?

Hacket som foreslås i den nevnte artikkelen er å sørge for at pod-ens shutdown
tar såppass med tid at den nokså sikkert ikke stenger ned _før_ den er meldt ut
av `iptables` og `endpoints`. Hvordan man gjør dette kommer litt an på hva som
kjører i pod-en, men en enkel og YOLO tilnærming er å kjøre `sleep` som en
`preStop`:

```yml
lifecycle:
  preStop:
    exec:
      command: ["/bin/bash", "-c", "sleep 10"]
```

Denne skal altså inn under hver enkelt oppføring under `containers`.

--------------------------------------------------------------------------------
:section/title Problem 3: Connection draining
:section/theme :dark1
:section/body

Ok, så nå har vi unngått å få trafikk _før_ poden er klar, og _etter_ at den er
død. Men hva med trafikken som prosesseres _i selve dødsøyeblikket_? Her vil du
fortsatt se connection drops med mindre du aktivt sørger for _connection
draining_, altså at tjenesten din selv sørger for å bremse shutdown inntil alle
requests som er "in flight" er avsluttet.

Her har jeg dessverre ikke noe raskt hack til deg, ettersom dette er avhengig av
teknologi, og må løses på applikasjonsnivå. Men, får du dette på plass har du
all grunn til å gi deg selv en klapp på skuldra, for da har du oppnådd
deploymentets nirvana: absolutt uten nedetid.
