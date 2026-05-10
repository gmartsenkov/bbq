;; A "login" endpoint. Returns a JSON body that other templates can dig into.
;; httpbin.org/anything echoes the request as JSON, which is handy for testing.
{:method  :POST
 :uri     "https://httpbin.org/anything"
 :headers {"content-type" "application/json"}
 :body    (json-encode {:org org :who "alice"})}
