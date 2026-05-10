(ns httpee.listing
  "List discovered templates with their `:title` (extracted statically from
  the parsed template forms — no eval, no network)."
  (:require [babashka.fs :as fs]
            [httpee.runner :as runner]))

(defn- template-title [name]
  ;; Walk parsed forms and return the first string `:title` we find on any
  ;; map literal. Avoids eval'ing the template (which could require unbound
  ;; vars or fire `json-request`).
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
      (println "no templates discovered (configure :dirs in httpee.edn)"))))
