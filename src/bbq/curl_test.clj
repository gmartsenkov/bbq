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
