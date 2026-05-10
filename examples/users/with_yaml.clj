;; Templates can require any bundled namespace now that we eval directly.
;; Add a Clojars dep at runtime with:
;;   (babashka.deps/add-deps '{:deps {medley/medley {:mvn/version "1.4.0"}}})
(require '[clojure.set :as set]
         '[clojure.string :as str]
         '[httpee :refer [bearer v]])

(let [extra-tags (set/union #{"v1"} #{"public"})]
  {:title   "Users — Show (with extra tags)"
   :method  :GET
   :uri     (str "https://httpbin.org/anything/" (v :org) "/users/" (v :id))
   :headers {"x-tags"        (str/join "," (sort extra-tags))
             "authorization" (bearer "demo-token")}})
