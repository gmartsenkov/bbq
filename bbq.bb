#!/usr/bin/env bb

(require '[babashka.cli :as cli]
         '[bbq.cli :as cli-helpers]
         '[bbq.curl :as curl]
         '[bbq.listing :as listing]
         '[bbq.output :as output]
         '[bbq.runner :as runner])

(when (= *file* (System/getProperty "babashka.file"))
  (let [normalized (map #(if (= % "--pager") "--pager=auto" %) *command-line-args*)
        {:keys [opts args]} (cli/parse-args normalized cli-helpers/spec)
        [template-name & override-args] args
        overrides (cli-helpers/parse-overrides override-args)]
    (cond
      (:complete opts)     (doseq [t (runner/discover-templates
                                       (:dirs (runner/read-config)))]
                             (println t))
      (:help opts)         (cli-helpers/print-help)
      (nil? template-name) (listing/list-templates)
      (:as opts)
      (try
        (let [request (runner/build-request template-name overrides)]
          (case (:as opts)
            :curl (println (curl/render request))
            (do (println "✗ unknown format:" (name (:as opts)))
                (System/exit 1))))
        (catch Exception e (println "✗" (.getMessage e)) (System/exit 1)))
      :else
      (try
        (output/print-response (runner/run-template template-name overrides)
                               {:pager (:pager opts)})
        (catch Exception e (println "✗" (.getMessage e)) (System/exit 1))))))
