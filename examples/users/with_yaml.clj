(require '[clojure.set :as set]
         '[clojure.string :as str]
         '[bbq :refer [bearer v]])

(let [extra-tags (set/union #{"v1"} #{"public"})]
  {:title   "Users — Show (with extra tags)"
   :method  :GET
   :uri     (str "https://httpbin.org/anything/" (v :org) "/users/" (v :id))
   :headers {"x-tags"        (str/join "," (sort extra-tags))
             "authorization" (bearer "demo-token")}})
