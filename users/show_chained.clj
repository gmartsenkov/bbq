;; Chained-auth template — calls auth/login, plucks a field, uses it in this request.
;; Demonstrates `let` + `json-request` + `->` for navigation.
(let [login (json-request "auth/login")
      who   (-> login :json :who)]
  {:title   "Users — Show (chained auth)"
   :method  :GET
   :uri     (str "https://httpbin.org/anything/" org "/users/" id)
   :headers {"x-logged-in-as" who
             "authorization"  (bearer (-> login :json :org))}})
