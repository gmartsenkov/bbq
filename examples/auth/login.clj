;; A "login" endpoint. Returns a JSON body that other templates can dig into.
;; httpbin.org/anything echoes the request as JSON, which is handy for testing.
(require '[httpee :refer [json-encode v]])

{:title   "Auth — Login"
 :method  :POST
 :uri     "https://httpbin.org/anything"
 :headers {"content-type" "application/json"}
 :body    (json-encode {:org (v :org) :who "alice"})}
