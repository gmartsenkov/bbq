(ns httpee.runner-test
  (:require [babashka.http-client :as http]
            [clojure.test :refer [deftest is testing]]
            [httpee]
            [httpee.runner :as runner]))

(deftest discover-templates-test
  (let [out (runner/discover-templates ["auth" "users"])]
    (is (some #{"auth/login"} out))
    (is (some #{"users/show"} out))
    (is (= out (sort out)) "results should be sorted")))

(deftest run-template-test
  (let [captured  (atom nil)
        fake-resp {:status 200 :headers {} :body "{\"ok\": true}"}]
    (with-redefs [http/request (fn [req]
                                 (reset! captured req)
                                 fake-resp)
                  httpee/env   (fn [_] "stub")]

      (testing "config vars + CLI overrides flow into the rendered request"
        (let [resp (runner/run-template "users/show" {:id "42"})]
          (is (= 200 (:status resp)))
          (is (= :GET (:method @captured)))
          (is (= "https://httpbin.org/anything/acme/users/42" (:uri @captured)))
          (is (= "Bearer demo-token"
                 (get-in @captured [:headers "authorization"])))))

      (testing "overrides win over config variables"
        (runner/run-template "users/show" {:id "1" :org "beta"})
        (is (= "https://httpbin.org/anything/beta/users/1" (:uri @captured))))

      (testing "missing template name throws"
        (is (thrown? Exception (runner/run-template "users/nope" {})))))))
