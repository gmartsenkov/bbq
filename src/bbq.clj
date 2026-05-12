(ns bbq
  "Helpers exposed to bbq templates. Require with
   `(:require [bbq :refer [env bearer basic base64-encode json-encode json-request v]])`."
  (:require [cheshire.core :as json]))

(def ^:dynamic *run-template*
  (fn [_ _] (throw (ex-info "bbq runner not initialised" {}))))

(def ^:dynamic *vars* {})

(defn v
  "Look up a template variable by keyword. `(v :id)` throws if not set;
   `(v :id \"99\")` returns the default."
  ([k]
   (if (contains? *vars* k)
     (get *vars* k)
     (throw (ex-info (str "variable " k " not set "
                          "(define in bbq.edn :variables or pass as an override)")
                     {:variable k}))))
  ([k default] (get *vars* k default)))

(def ^:private cache (atom {}))

(defn env [name]
  (or (System/getenv name)
      (throw (ex-info (str "environment variable '" name "' is not set") {}))))

(defn base64-encode [s]
  (.encodeToString (java.util.Base64/getEncoder) (.getBytes (str s))))

(defn bearer [token]
  (str "Bearer " token))

(defn basic [user pass]
  (str "Basic " (base64-encode (str user ":" pass))))

(def json-encode json/generate-string)

(defn json-request [name]
  (or (@cache name)
      (let [body (json/parse-string (:body (*run-template* name {})) true)]
        (swap! cache assoc name body)
        body)))
