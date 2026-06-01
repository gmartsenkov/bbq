(ns bbq.output
  (:require [babashka.fs :as fs]
            [babashka.process :as p]
            [cheshire.core :as json]
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

(defn- pretty-json [s]
  (try
    (json/generate-string (json/parse-string s) {:pretty true})
    (catch Exception _ s)))

(defn- highlight-body! [body headers]
  ;; Skip bat when stdout isn't a TTY (piped) or bat is missing — fall
  ;; back to raw bytes so downstream tools don't see ANSI escapes.
  ;; When bat is used, inherit stdout so its auto theme detection
  ;; sees the real terminal instead of falling back to the dark default.
  (let [lang (content-type-lang headers)
        body (if (= lang "json") (pretty-json body) body)]
    (if (and lang (System/console) (fs/which "bat"))
      (do @(p/process {:in body :out :inherit :err :inherit}
                      "bat" "--paging=never" "--style=plain"
                      "--language" lang)
          nil)
      (println body))))

(defn- bat-args [lang]
  (cond-> ["bat" "--color=always" "--paging=always" "--style=plain"]
    lang (into ["--language" lang])))

(defn- pager-args [pager lang]
  (case pager
    "auto" (cond
             (fs/which "bat")  (bat-args lang)
             (fs/which "less") ["less" "-R"])
    "bat"  (bat-args lang)
    "less" ["less" "-R"]
    (cond-> [pager]
      (and lang (#{"vim" "nvim"} pager)) (into ["-c" (str "set ft=" lang)])
      :always (conj "-"))))

(defn- write-preamble [{:keys [request elapsed-ms status headers]}]
  (let [method (-> request :method name str/upper-case)]
    (println "→" method (:uri request))
    (println "✓" status (str elapsed-ms "ms"))
    (println)
    (section-rule "headers")
    (doseq [[k v] (sort-by key (dissoc headers ":status"))]
      (println (str k ": " v)))
    (println)
    (section-rule "body")))

(defn- pretty-body [body headers]
  (let [lang (content-type-lang headers)]
    (if (= lang "json") (pretty-json body) body)))

(defn print-response
  ([resp] (print-response resp {}))
  ([resp {:keys [pager]}]
   (if pager
     (let [text (with-out-str
                  (write-preamble resp)
                  (println (pretty-body (:body resp) (:headers resp))))
           args (pager-args pager (content-type-lang (:headers resp)))]
       (if args
         @(p/process args {:in text :out :inherit :err :inherit})
         (do (println "✗ pager 'auto' found neither bat nor less on PATH")
             (print text))))
     (do (write-preamble resp)
         (highlight-body! (:body resp) (:headers resp))))))
