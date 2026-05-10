;; Simple template — uses `org` from bbq.edn variables and `id` from CLI overrides.
(require '[bbq :refer [env bearer json-encode v]])

{:title   "Users — Show"
 :method  :GET
 :uri     (str "https://httpbin.org/anything/" (v :org) "/users/" (v :id))
 :body    (json-encode {:org (env "BOB") :who "alice"})
 :headers {"authorization" (bearer "demo-token")}}
