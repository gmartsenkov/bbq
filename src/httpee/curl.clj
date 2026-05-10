(ns httpee.curl
  "Render a request map as a multi-line curl one-liner using POSIX-safe
  single-quote escaping."
  (:require [clojure.string :as str]))

(defn- shell-quote [s]
  (str "'" (str/replace s "'" "'\\''") "'"))

(defn render [req]
  (let [lines (atom ["curl"
                     (str "--request " (-> req :method name str/upper-case))
                     (str "--url " (shell-quote (:uri req)))])]
    (doseq [[k v] (sort-by key (or (:headers req) {}))]
      (swap! lines conj (str "--header " (shell-quote (str k ": " v)))))
    (let [body (:body req)]
      (when (and (string? body) (seq body))
        (swap! lines conj (str "--data-raw " (shell-quote body)))))
    (str/join " \\\n  " @lines)))
