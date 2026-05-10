(ns httpee-test
  (:require [clojure.test :refer [deftest is]]
            [httpee]))

(deftest bearer-test
  (is (= "Bearer abc" (httpee/bearer "abc"))))

(deftest basic-test
  (is (= "Basic YWxpY2U6c2VjcmV0" (httpee/basic "alice" "secret"))))
