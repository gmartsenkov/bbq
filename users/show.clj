;; Simple template — uses `org` from httpee.edn variables and `id` from CLI overrides.
{:method  :GET
:uri     (str "https://httpbin.org/anything/" org "/users/" id)
:body    (json-encode {:org (env "BOB") :who "alice"})
:headers {"authorization" (bearer "demo-token")}}
