(ns illuminepixels.utils)


(defn dissoc-in
  [m [k & ks]]
  (if ks
    (if (map? (get m k))
      (update m k #(dissoc-in % ks))
      m)
    (dissoc m k)))

(defn index-by [f coll]
  (into {} (map (juxt f identity) coll)))

(defn parse-date [s]
  (js/Date. (.parse js/Date s)))

(defn format-date [date]
  (cond
    (number? date) (format-date (js/Date. (* 1000 date)))
    (string? date) (format-date (parse-date date))
    (instance? js/Date date) (.toLocaleDateString date)
    :otherwise (str date)))