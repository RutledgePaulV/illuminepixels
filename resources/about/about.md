Hi, I'm Paul.

My interest in programming began in early highschool with a TI-83+ Silver Edition.
I taught myself TI-Basic by creating little apps that solved physics equations
while 'showing the work'. I quickly got hooked on solving classes of problems instead
of instances and started to prefer programming over video games.

Since then I went to college for Mathematics and Computer Science; discovered a proclivity for architecture, devops, and security; fell in love with 
functional programming; taught myself Clojure; taught my team Clojure; and built a successful multi-tenant
microservice product platform from the ground up with Clojure and Kubernetes. These days I continue
to lead the technical efforts while mentoring others as we scale our team and products to meet
the demands of *real customers*.

___

<br/>

### Leadership

I've lead orchestra sections, karate classes, resident assistants, and software teams. 

I choose to lead primarily by doing, teaching, and inspiring. I have little interest in tasking and tab keeping.

- Led transition from monolithic to microservice architecture
- Led transition from customer specific development to configurable multi-tenant SaaS
- I identified Clojure as an important technology and led a successful adoption in 2016
- I identified Kubernetes as an important technology and led a successful adoption in 2015

### Implementations

These are some of the individual contributor works I'm most proud of.

- Wrote type safe database agnostic query builders
- Wrote a predicate expression language grammar and compilers
- Created an NGINX security module for accessing S3 files via signed links
- Implemented a periodic backup / cleanup process with per-customer live restoration
- Implemented a collaborative document viewer using websockets and authoritative server state
- Created a secure web accessible Clojure console to perform audited database migrations in production


___

<br/>

### Technology

This site is built with [clojure](https://clojure.org/about/rationale) and
[clojurescript](https://clojurescript.org/about/rationale). It uses [re-frame](https://github.com/Day8/re-frame) 
for frontend components and [reitit](https://github.com/metosin/reitit) for routing. State is 
communicated using topic subscriptions multiplexed over a single websocket connection and serialized 
with [transit](https://github.com/cognitect/transit-format). As you navigate to various pages your 
browser subscribes and unsubscribe to various topics. On the server subscriptions are fed by 
[core.async](https://github.com/clojure/core.async) channels and go blocks. This site is deployed
as a docker container on a [digital ocean](https://www.digitalocean.com/) server and proxied by 
[caddy](https://caddyserver.com/) with automatic certificate renewal.

Isn't that overkill for a profile site? [Perhaps](https://www.youtube.com/watch?v=KwIo9Y9iJ6A).