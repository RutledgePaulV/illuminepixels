(ns illuminepixels.utils)


(defn dissoc-in
  [m [k & ks]]
  (if ks
    (if (map? (get m k))
      (update m k #(dissoc-in % ks))
      m)
    (dissoc m k)))