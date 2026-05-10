(ns bbq.listing
  (:require [babashka.fs :as fs]
            [bbq.runner :as runner]))

(defn- template-title [name]
  ;; Walk the parsed forms instead of eval'ing — eval could require unbound
  ;; vars or fire `json-request`, both of which we don't want for `bbq` list.
  (let [path (str name ".clj")]
    (when (fs/exists? path)
      (try
        (->> (runner/read-template-forms path)
             (tree-seq coll? seq)
             (some (fn [x]
                     (when (and (map? x) (string? (:title x)))
                       (:title x)))))
        (catch Exception _ nil)))))

(defn list-templates []
  (let [templates (runner/discover-templates (:dirs (runner/read-config)))]
    (if (seq templates)
      (let [titled (for [t templates] [t (template-title t)])
            width  (apply max 0 (map (comp count first) titled))]
        (doseq [[n t] titled]
          (let [cell (format (str "%-" width "s") n)]
            (if t
              (println "❯" cell "  " t)
              (println "❯" cell)))))
      (println "no templates discovered (configure :dirs in bbq.edn)"))))
