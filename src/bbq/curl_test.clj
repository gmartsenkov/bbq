(ns bbq.curl-test
  (:require [bbq.curl :as curl]
            [clojure.test :refer [deftest is]]))

(deftest render-test
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
           (curl/render req)))))

(deftest render-query-params-test
  (is (= (str "curl \\\n"
              "  --request GET \\\n"
              "  --url 'https://api.example.com/search?q=hello+world&limit=10'")
         (curl/render {:method       :GET
                       :uri          "https://api.example.com/search"
                       :query-params {:q "hello world" :limit 10}})))
  (is (= (str "curl \\\n"
              "  --request GET \\\n"
              "  --url 'https://api.example.com/items?existing=1&new=2'")
         (curl/render {:method       :GET
                       :uri          "https://api.example.com/items?existing=1"
                       :query-params {:new 2}}))))
