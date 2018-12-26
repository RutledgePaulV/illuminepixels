FROM clojure:openjdk-11-lein
WORKDIR /usr/src/app
COPY project.clj project.clj
RUN lein deps && rm project.clj
COPY . /usr/src/app
COPY .git /usr/src/app/.git
RUN lein with-profile -dev,+build deps
RUN lein with-profile -dev,+build uberjar
EXPOSE 3000
CMD ["lein", "with-profile", "-dev,+stubs", "trampoline", "run"]