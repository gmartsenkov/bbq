#!/usr/bin/env bb

;; httpee.bb — Babashka entry point. The actual logic lives under
;; `src/httpee/`; this file just parses CLI args and dispatches.

(require '[babashka.cli :as cli]
         '[httpee.cli :as cli-helpers]
         '[httpee.curl :as curl]
         '[httpee.listing :as listing]
         '[httpee.output :as output]
         '[httpee.runner :as runner])

(when (= *file* (System/getProperty "babashka.file"))
  (let [{:keys [opts args]} (cli/parse-args *command-line-args* cli-helpers/spec)
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
        (output/print-response (runner/run-template template-name overrides))
        (catch Exception e (println "✗" (.getMessage e)) (System/exit 1))))))
