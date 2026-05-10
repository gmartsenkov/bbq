#!/usr/bin/env bb

;; Tests for httpee.bb. Loaded via `bb test` from bb.edn.
;; The guard in httpee.bb keeps its CLI block from firing when we load it.

(require '[clojure.test :refer [deftest is run-tests testing]])

(load-file "httpee.bb")

(deftest parse-overrides-test
  (is (= {}                     (parse-overrides [])))
  (is (= {'id "42"}              (parse-overrides ["id=42"])))
  (is (= {'id "42" 'token "abc"} (parse-overrides ["id=42" "token=abc"])))
  (testing "value containing ="
    (is (= {'q "x=y"} (parse-overrides ["q=x=y"])))))

(deftest bearer-test
  (is (= "Bearer abc" (bearer "abc"))))

(deftest basic-test
  (is (= "Basic YWxpY2U6c2VjcmV0" (basic "alice" "secret"))))

(deftest discover-templates-test
  (let [out (discover-templates ["auth" "users"])]
    (is (some #{"auth/login"}    out))
    (is (some #{"users/show"}    out))
    (is (= out (sort out)) "results should be sorted")))

(deftest run-template-test
  (let [captured  (atom nil)
        fake-resp {:status 200 :headers {} :body "{\"ok\": true}"}]
    (with-redefs [http/request (fn [req]
                                 (reset! captured req)
                                 fake-resp)
                  env          (fn [_] "stub")]

      (testing "config vars + CLI overrides flow into the rendered request"
        (let [resp (run-template "users/show" {'id "42"})]
          (is (= 200 (:status resp)))
          (is (= :GET (:method @captured)))
          (is (= "https://httpbin.org/anything/acme/users/42" (:uri @captured)))
          (is (= "Bearer demo-token"
                 (get-in @captured [:headers "authorization"])))))

      (testing "overrides win over config variables"
        (run-template "users/show" {'id "1" 'org "beta"})
        (is (= "https://httpbin.org/anything/beta/users/1" (:uri @captured))))

      (testing "missing template name throws"
        (is (thrown? Exception (run-template "users/nope" {})))))))

(deftest request->curl-test
  (let [req {:method  :POST
             :uri     "https://api.example.com/orgs/acme/users"
             :headers {"authorization" "Bearer abc"
                       "content-type"  "application/json"}
             :body    "{\"name\": \"O'Brien\"}"}]
    (is (= (str "curl \\\n"
                "  --request POST \\\n"
                "  --url 'https://api.example.com/orgs/acme/users' \\\n"
                "  --header 'authorization: Bearer abc' \\\n"
                "  --header 'content-type: application/json' \\\n"
                "  --data-raw '{\"name\": \"O'\\''Brien\"}'")
           (request->curl req)))))

(let [{:keys [fail error]} (run-tests)]
  (System/exit (if (zero? (+ fail error)) 0 1)))
