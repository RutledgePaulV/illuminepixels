(ns illuminepixels.markdown
  (:require [missing.core :as miss]
            [markdown.core :as md]
            [clojure.walk :as walk]
            [hickory.core :as hick]
            [clojure.string :as string]
            [glow.core :as glow]))

(defn unescape-html [s]
  (let [replacements {"&amp;" "&" "&lt;" "<" "&gt;" ">" "&quot;" "\""}]
    (reduce-kv string/replace s replacements)))

(defn unescape-hiccup [hiccup]
  (walk/postwalk (fn [form] (if (string? form) (unescape-html form) form)) hiccup))

(defn counting-removal [s replace]
  (loop [value s count 0]
    (let [replaced (string/replace-first value replace "")]
      (if (not= value replaced)
        (recur replaced (inc count))
        [count replaced]))))

(defn whitespace->tags [whitespace]
  (let [[breaks without] (counting-removal whitespace "\n")
        indent [:span {:dangerouslySetInnerHTML {:__html (string/join "" (repeat (count without) "&nbsp;"))}}]]
    (vec (concat [:span] (repeat breaks [:br]) [indent]))))

(defn expand-whitespace [hiccup]
  (walk/postwalk
    (fn [form]
      (if (and (string? form)
               (string/blank? form)
               (string/includes? form "\n"))
        (whitespace->tags form)
        form))
    hiccup))

(defn html->hiccup [html]
  (->> (vec (concat [:div] (drop 2 (-> html hick/parse hick/as-hiccup first (nth 3)))))
       (unescape-hiccup)))

(defn inner [form]
  (if (map? (second form))
    (nth form 2)
    (second form)))

(defn tag? [tag form]
  (and (vector? form) (= tag (first form))))

(defn code-block? [hiccup]
  (and (vector? hiccup)
       (= :pre (first hiccup))
       (= :code (first (inner hiccup)))))

(defn extract-code [hiccup]
  (inner (miss/dfs (partial tag? :code) hiccup)))

(defn sexp? [form]
  (and (tag? :span form)
       (= "s-exp" (some-> form second :class))))

(defn sexpr-start? [form]
  (and (sexp? form) (#{"(" "[" "{"} (nth form 2))))

(defn sexpr-close? [form]
  (and (sexp? form) (#{")" "]" "}"} (nth form 2))))

(defn with-rainbow-class [form depth]
  (update-in form [1 :class] #(str % " rainbow-" depth)))

(defn rainbow-parens [hiccup]
  (let [stack (atom -1)]
    (walk/postwalk
      (fn [form]
        (cond
          (sexpr-start? form)
          (let [[_ new] (swap-vals! stack inc)]
            (with-rainbow-class form (inc (mod new 8))))
          (sexpr-close? form)
          (let [[old _] (swap-vals! stack dec)]
            (with-rainbow-class form (inc (mod old 8))))
          :else
          form))
      hiccup)))

(defn reformat-code [html]
  (walk/postwalk
    (fn [form]
      (if (code-block? form)
        (-> form
            (extract-code)
            (glow/highlight-html)
            (html->hiccup)
            (expand-whitespace))
        form))
    html))

(defn markdown->data [markdown]
  (let [{:keys [metadata html]}
        (md/md-to-html-string-with-meta markdown
          :heading-anchors true
          :reference-links? true
          :footnotes? true)]
    {:metadata
     (->>
       (or metadata {})
       (miss/filter-vals not-empty)
       (miss/map-vals first))
     :html
     (-> (html->hiccup html)
         (reformat-code)
         (rainbow-parens))}))