(ns bbq.curl
  (:require [clojure.string :as str])
  (:import [java.net URLEncoder]))

(defn- shell-quote [s]
  (str "'" (str/replace s "'" "'\\''") "'"))

(defn- url-encode [s]
  (URLEncoder/encode (str s) "UTF-8"))

(defn- query-string [params]
  (str/join "&"
            (for [[k v] params
                  v (if (sequential? v) v [v])]
              (str (url-encode (name k)) "=" (url-encode v)))))

(defn- with-query [uri params]
  (if (seq params)
    (str uri (if (str/includes? uri "?") "&" "?") (query-string params))
    uri))

(defn render [req]
  (let [uri   (with-query (:uri req) (:query-params req))
        lines (atom ["curl"
                     (str "--request " (-> req :method name str/upper-case))
                     (str "--url " (shell-quote uri))])]
    (doseq [[k v] (sort-by key (or (:headers req) {}))]
      (swap! lines conj (str "--header " (shell-quote (str k ": " v)))))
    (let [body (:body req)]
      (when (and (string? body) (seq body))
        (swap! lines conj (str "--data-raw " (shell-quote body)))))
    (str/join " \\\n  " @lines)))
