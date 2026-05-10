(ns httpee
  "Helpers exposed to httpee templates.

  Templates require this namespace to bring helpers into scope:

    (ns user
      (:require [httpee :refer [env bearer basic json-encode json-request v]]))

  The actual runner (httpee.bb) wires `*run-template*` to its build-and-fetch
  function so `json-request` can call back into the template machinery."
  (:require [cheshire.core :as json]))

;; Set by httpee.bb at startup so json-request can call back.
(def ^:dynamic *run-template*
  (fn [_ _] (throw (ex-info "httpee runner not initialised" {}))))

;; Bound by the runner per-eval; merged config :variables + CLI overrides.
(def ^:dynamic *vars* {})

(defn v
  "Look up a template variable by keyword. The runner binds `*vars*` to a
  merge of httpee.edn `:variables` and CLI overrides before eval'ing the
  template. Two-arity:
    (v :id)         ; throws if not set
    (v :id \"99\")    ; returns default if not set"
  ([k]
   (if (contains? *vars* k)
     (get *vars* k)
     (throw (ex-info (str "variable " k " not set "
                          "(define in httpee.edn :variables or pass as an override)")
                     {:variable k}))))
  ([k default] (get *vars* k default)))

;; Per-process memo so the same json-request call doesn't fire repeatedly.
(def ^:private cache (atom {}))

(defn env [name]
  (or (System/getenv name)
      (throw (ex-info (str "environment variable '" name "' is not set") {}))))

(defn bearer [token]
  (str "Bearer " token))

(defn basic [user pass]
  (str "Basic " (.encodeToString (java.util.Base64/getEncoder)
                                 (.getBytes (str user ":" pass)))))

(def json-encode json/generate-string)

(defn json-request [name]
  (or (@cache name)
      (let [body (json/parse-string (:body (*run-template* name {})) true)]
        (swap! cache assoc name body)
        body)))
