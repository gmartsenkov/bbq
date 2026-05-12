(ns bbq-test
  (:require [bbq]
            [clojure.test :refer [deftest is]]))

(deftest bearer-test
  (is (= "Bearer abc" (bbq/bearer "abc"))))

(deftest basic-test
  (is (= "Basic YWxpY2U6c2VjcmV0" (bbq/basic "alice" "secret"))))

(deftest base64-encode-test
  (is (= "YWxpY2U6c2VjcmV0" (bbq/base64-encode "alice:secret"))))
