(ns bbq.output
  (:require [babashka.fs :as fs]
            [babashka.process :as p]
            [clojure.string :as str]))

(def ^:private rule-width 50)

(defn- section-rule [label]
  (let [prefix (str "── " label " ")
        dashes (apply str (repeat (max 0 (- rule-width (count prefix))) "─"))]
    (println (str prefix dashes))))

(defn- content-type-lang [headers]
  (let [ct (get headers "content-type" "")]
    (cond
      (str/includes? ct "json") "json"
      (str/includes? ct "html") "html"
      (str/includes? ct "xml")  "xml"
      :else                     nil)))

(defn- highlight-body [body headers]
  ;; Skip bat when stdout isn't a TTY (piped) or bat is missing — fall
  ;; back to raw bytes so downstream tools don't see ANSI escapes.
  (let [lang (content-type-lang headers)]
    (if (and lang (System/console) (fs/which "bat"))
      (:out (p/sh {:in body :out :string}
                  "bat" "--color=always" "--paging=never"
                  "--style=plain" "--language" lang))
      body)))

(defn print-response [resp]
  (let [{:keys [request elapsed-ms status headers body]} resp
        method (-> request :method name str/upper-case)]
    (println "→" method (:uri request))
    (println "✓" status (str elapsed-ms "ms"))
    (println)
    (section-rule "headers")
    (doseq [[k v] (sort-by key (dissoc headers ":status"))]
      (println (str k ": " v)))
    (println)
    (section-rule "body")
    (println (highlight-body body headers))))
