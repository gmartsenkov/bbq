(require '[bbq :refer [json-encode v]])

{:title   "Auth — Login"
 :method  :POST
 :uri     "https://httpbin.org/anything"
 :headers {"content-type" "application/json"}
 :body    (json-encode {:org (v :org) :who "alice"})}
