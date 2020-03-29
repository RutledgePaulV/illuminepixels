Title: topofuncs
Slug: topofuncs
Summary: Describing function composition with graphs of data.

###  topofuncs

Imagine a function as a node in a graph. It has an inbound edge representing the
input and has an outbound edge representing the output. If the output of one node
aligns with the input of another node we say that there exists a graph edge between them 
and that there exists a third function obtained through composition. This is certainly not 
a new idea in the Clojureverse and has already been explored by libraries like [plumbing](https://github.com/plumatic/plumbing) 
and [pathom](https://github.com/wilkerlucio/pathom). Nevertheless, I created my own called 
[topofuncs](https://github.com/rutledgepaulv/topofuncs) that explores the same idea for file 
conversions and composing ring middleware.


#### file conversions

Sometimes we want to convert files from one format to another. We can reduce the multiplicity
of this problem by chaining conversions together to get from our source format to our target format.


We simply define individual conversions like this:
```
(require '[topofuncs.transcoding :as tf])

(tf/def-transcoding-graph convert)

(tf/def-transcoding-graph-impl convert
  {:inputs #{"image/png" "image/jpeg"}
   :output "image/jpeg"}
  [^InputStream input-stream ^OutputStream output-stream options]
  ...)

(tf/def-transcoding-graph-impl convert
  {:inputs #{"application/pdf"}
   :output "image/png"}
  [^InputStream input-stream ^OutputStream output-stream options]
  ...)


(def pdf->jpeg (convert "application/pdf" "image/jpeg"))

```

And topofuncs constructs a graph behind the scenes (attached to #'convert as metadata). You can then
perform any conversion for which a path in the graph exists. If more than one path exists, the path 
with the fewest hops is used. Converters are chained together using piped streams so that as one conversion
writes bytes to the output stream the next converter in the chain can start reading from its input stream.

``` 
(defn pipestream-combinator
  ([]
   (fn [^InputStream input-stream ^OutputStream output-stream _]
     (with-open [is input-stream os output-stream] (io/copy is os))))
  ([pipe1 pipe2]
   (fn [^InputStream input-stream ^OutputStream output-stream options]
     (with-open
       [sink   output-stream
        source (let [input  (PipedInputStream.)
                     output (PipedOutputStream.)]
                 (.connect input output)
                 (future
                   (with-open [source input-stream
                               sink   output]
                     (pipe1 source sink options)))
                 input)]
       (pipe2 source sink options)))))
```



#### ring middleware

If you've ever worked in a large application with many ring middleware you've probably run into problems related
to ordering of the middleware stack. Dependencies between middleware are often left implicit, but we can regain
confidence by describing dependencies in data and using code to produce a safe ordering.


When defining a single piece of middleware just describe whether it must occur before or after any other specific
middleware. 

```
(require '[topofuncs.middleware :as tm])

(tm/def-middleware-graph middleware)

(tm/def-middleware-graph-impl middleware
  {:name :query-params}
  [handler]
  (fn [request] 
    (let [params (parse-qp (:query-string request))]
        (handler (assoc request :query-params params)))))

(tm/def-middleware-graph-impl middleware
  {:name :form-params}
  [handler]
  (fn [request] 
    (let [params (parse-fp (:body request))]
        (handler (assoc request :form-params params)))))

(tm/def-middleware-graph-impl middleware
  {:name :parameter-validation
   :after #{:query-params :form-params}}
  [handler]
  (fn [request] 
    (if (valid? request)
        (handler request)
        (bad-request))))

(def param-validator-mw (middleware :parameter-validation))

```