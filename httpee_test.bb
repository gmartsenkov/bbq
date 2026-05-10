#!/usr/bin/env bb

;; Test runner. Loads each per-module test namespace and runs them all.
;; Invoked via `bb test` (which runs from examples/ so cwd-relative file
;; lookups in the runner tests resolve to the demo templates).

(require '[clojure.test :as test]
         '[httpee-test]
         '[httpee.cli-test]
         '[httpee.curl-test]
         '[httpee.runner-test])

(let [{:keys [fail error]} (test/run-tests 'httpee-test
                                           'httpee.cli-test
                                           'httpee.curl-test
                                           'httpee.runner-test)]
  (System/exit (if (zero? (+ fail error)) 0 1)))
