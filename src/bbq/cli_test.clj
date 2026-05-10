(ns bbq.cli-test
  (:require [bbq.cli :as cli]
            [clojure.test :refer [deftest is testing]]))

(deftest parse-overrides-test
  (is (= {}                       (cli/parse-overrides [])))
  (is (= {:id "42"}                (cli/parse-overrides ["id=42"])))
  (is (= {:id "42" :token "abc"}   (cli/parse-overrides ["id=42" "token=abc"])))
  (testing "value containing ="
    (is (= {:q "x=y"} (cli/parse-overrides ["q=x=y"])))))
