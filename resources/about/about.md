Hi, I'm Paul.

My interest in programming began in early highschool with a TI-83+ Silver Edition.
I taught myself TI-Basic by creating apps that solved physics equations while 'showing the work'. 
I quickly got hooked on solving classes of problems instead of instances and started to prefer 
programming to video games.

I went to college for Mathematics and Computer Science; rose through the ranks of a small software company;
discovered a proclivity for architecture, devops, and security; fell in love with functional programming; 
taught myself Clojure; taught my team Clojure; and built a successful multi-tenant product suite from the 
ground up with Clojure and Kubernetes. Recently I moved on to a large company with different challenges but 
within a few years I expect to start my own tech company.

<br/>

___

<br/>

### Leadership

I've led orchestra sections, karate classes, resident assistants, and software teams.

- Led transition from monolithic to microservice architecture
- Led transition from customer specific development to configurable multi-tenant SaaS
- I identified Clojure as an important technology and led a successful adoption in 2016
- I identified Kubernetes as an important technology and led a successful adoption in 2015

### Projects

I've delivered impactful software that sees daily use by thousands of users.

- I was the lead developer and architect behind [Pepper Flow](https://www.vodori.com/products/pepper-flow-commercial/).
- I prototyped the idea and lead the development of [Pepper Flow Digital Review](https://www.vodori.com/products/pepper-flow-digital-review/).


### Implementations

I've written lots of technically challenging code.

- Wrote a predicate expression language grammar and compilers
- Created a generic file conversion service modeled as a graph
- Wrote a type safe and database agnostic query builder library
- Created a custom NGINX security module for accessing S3 files via signed links
- Implemented a periodic backup / cleanup process with per-customer live restoration
- Created a secure web accessible Clojure console to perform audited database migrations
- Implemented a collaborative document viewer using websockets and authoritative server state
- Designed a network protocol with discovery, routing, and buffers to unify communication across browser iframes and web extension contexts (content, background).

<br/>

___

<br/>

### Technology

This site is built with [clojure](https://clojure.org/about/rationale) and
[clojurescript](https://clojurescript.org/about/rationale). It uses [re-frame](https://github.com/Day8/re-frame) 
for frontend components and [reitit](https://github.com/metosin/reitit) for routing. State is 
communicated using topic subscriptions multiplexed over a single websocket connection and serialized 
with [transit](https://github.com/cognitect/transit-format). As you navigate to various pages your 
browser subscribes and unsubscribe to various topics. On the server, subscriptions are fed by 
[core.async](https://github.com/clojure/core.async) channels and go blocks. This site is deployed
as a docker container on a [kubernetes](https://kubernetes.io/) cluster hosted at [digital ocean](https://www.digitalocean.com/).
Requests route through a HA load balancer, [ingress-nginx](https://github.com/kubernetes/ingress-nginx) webserver, and finally land 
at an embedded jetty server. Builds and deployments are automated using [github actions](https://github.com/features/actions)
that trigger on every commit.


Isn't that overkill for a blog? [Perhaps](https://www.youtube.com/watch?v=KwIo9Y9iJ6A).