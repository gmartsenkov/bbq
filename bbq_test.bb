#!/usr/bin/env bb

;; Invoked via `bb test`, which runs from examples/ so cwd-relative file
;; lookups in the runner tests resolve to the demo templates.

(require '[bbq-test]
         '[bbq.cli-test]
         '[bbq.curl-test]
         '[bbq.runner-test]
         '[clojure.test :as test])

(let [{:keys [fail error]} (test/run-tests 'bbq-test
                                           'bbq.cli-test
                                           'bbq.curl-test
                                           'bbq.runner-test)]
  (System/exit (if (zero? (+ fail error)) 0 1)))
