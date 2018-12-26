(ns illuminepixels.components
  (:require [re-frame.core :as rf]
            [clojure.walk :as walk]
            [clojure.string :as string]))

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

(defn simple-div [contents]
  [:div (walk/postwalk
          (fn [form]
            (if (and (string? form) (string/includes? form "\n"))
              (whitespace->tags form)
              form))
          contents)])

(defn code-snippet [markup]
  (let [{:keys [html css]} @(rf/subscribe [:illuminepixels.subs/subscribe {:kind :highlight :html markup}])
        style-node (atom nil)]
    [(with-meta
       simple-div
       {:component-did-mount
        (fn [component]
          (let [head  (.-head js/document)
                style (.createElement js/document "style")
                _     (set! (.-type style) "text/css")
                node  (.createTextNode js/document css)]
            (.appendChild style node)
            (.appendChild head style)
            (reset! style-node style)))
        :component-will-unmount
        (fn [component]
          (when-some [style (deref style-node)]
            (let [head (.-head js/document)]
              (.removeChild head style))))}) html]))


(defn walk-code-snippets [markup]
  (walk/postwalk
    (fn [form]
      (if (and (vector? form) (= :pre (first form)))
        [code-snippet form]
        form))
    markup))