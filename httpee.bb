#!/usr/bin/env bb

;; httpee.bb — Babashka port. Templates are full Clojure forms evaluated
;; with helpers + per-call variables in scope. Templates may `require`
;; bundled namespaces or pull Clojars deps via `babashka.deps/add-deps`.

(require '[babashka.cli :as cli]
         '[babashka.fs :as fs]
         '[babashka.http-client :as http]
         '[babashka.process :as p]
         '[cheshire.core :as json]
         '[clojure.edn :as edn]
         '[clojure.string :as str])

;; ─── helpers exposed to templates ────────────────────────────────────
;; Defs in this namespace are visible inside `eval`'d templates.

(def ^:private cache   (atom {}))
(def ^:private running (atom #{}))

(declare run-template)

(defn env [name]
  (or (System/getenv name)
      (throw (ex-info (str "environment variable '" name "' is not set") {}))))

(defn bearer [token] (str "Bearer " token))

(defn basic [user pass]
  (str "Basic " (.encodeToString (java.util.Base64/getEncoder)
                                 (.getBytes (str user ":" pass)))))

(defn json-request [name]
  (or (@cache name)
      (let [body (json/parse-string (:body (run-template name {})) true)]
        (swap! cache assoc name body)
        body)))

(def json-encode json/generate-string)

;; ─── template loading + running ──────────────────────────────────────

(defn- read-config []
  (if (fs/exists? "httpee.edn")
    (edn/read-string (slurp "httpee.edn"))
    {:variables {}}))

(defn- read-template-forms [path]
  ;; Read all top-level forms, so a template can have a leading `require`
  ;; / `add-deps` plus the request map itself.
  (read-string (str "[" (slurp path) "]")))

(defn- discover-templates [dirs]
  (->> dirs
       (mapcat (fn [dir]
                 (when (fs/exists? dir)
                   (for [p (fs/glob dir "**.clj")]
                     (-> p str (str/replace #"\.clj$" ""))))))
       sort))

(defn- build-request [name overrides]
  (when (@running name)
    (throw (ex-info (str "cycle detected on " name) {})))
  (let [path (str name ".clj")]
    (when-not (fs/exists? path)
      (throw (ex-info (str "template not found: " name) {})))
    (swap! running conj name)
    (try
      (let [forms        (read-template-forms path)
            bindings     (merge (:variables (read-config)) overrides)
            let-bindings (vec (mapcat (fn [[sym val]] [sym val]) bindings))
            wrapped      (list 'let let-bindings (cons 'do forms))]
        (eval wrapped))
      (finally (swap! running disj name)))))

(defn- run-template [name overrides]
  (let [request  (build-request name overrides)
        start    (System/currentTimeMillis)
        response (http/request request)]
    (assoc response
           :elapsed-ms (- (System/currentTimeMillis) start)
           :request    request)))

(defn- shell-quote [s]
  (str "'" (str/replace s "'" "'\\''") "'"))

(defn- request->curl [req]
  (let [lines (atom ["curl"
                     (str "--request " (-> req :method name str/upper-case))
                     (str "--url " (shell-quote (:uri req)))])]
    (doseq [[k v] (sort-by key (or (:headers req) {}))]
      (swap! lines conj (str "--header " (shell-quote (str k ": " v)))))
    (let [body (:body req)]
      (when (and (string? body) (seq body))
        (swap! lines conj (str "--data-raw " (shell-quote body)))))
    (str/join " \\\n  " @lines)))

;; ─── CLI ─────────────────────────────────────────────────────────────

(def cli-spec
  {:spec {:help     {:coerce :boolean :alias :h :desc "show this help"}
          :complete {:coerce :boolean :desc "list template names for shell completion"}
          :as       {:coerce :keyword :desc "render as a snippet (curl)"}}})

(defn- parse-overrides [args]
  (into {} (for [a args :let [[k v] (str/split a #"=" 2)]]
             [(symbol k) v])))

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
  (let [lang (content-type-lang headers)]
    (if (and lang (System/console) (fs/which "bat"))
      (:out (p/sh {:in body :out :string}
                  "bat" "--color=always" "--paging=never"
                  "--style=plain" "--language" lang))
      body)))

(defn- print-response [resp]
  (let [{:keys [request elapsed-ms status headers body]} resp
        method (-> request :method name str/upper-case)]
    (println "→" method (:uri request))
    (println "✓" status (str elapsed-ms "ms"))
    (println)
    (section-rule "headers")
    (doseq [[k v] (sort-by key (dissoc headers ":status"))] (println (str k ": " v)))
    (println)
    (section-rule "body")
    (println (highlight-body body headers))))

(defn- print-help []
  (println "usage: bb httpee.bb <template> [k=v ...]")
  (println)
  (println (cli/format-opts cli-spec)))

(defn- list-templates []
  (let [templates (discover-templates (:dirs (read-config)))]
    (if (seq templates)
      (doseq [t templates] (println "❯" t))
      (println "no templates discovered (configure :dirs in httpee.edn)"))))

(when (= *file* (System/getProperty "babashka.file"))
  (let [{:keys [opts args]} (cli/parse-args *command-line-args* cli-spec)
        [template-name & override-args] args
        overrides (parse-overrides override-args)]
    (cond
      (:complete opts)     (doseq [t (discover-templates (:dirs (read-config)))]
                             (println t))
      (:help opts)         (print-help)
      (nil? template-name) (list-templates)
      (:as opts)
      (try
        (let [request (build-request template-name overrides)]
          (case (:as opts)
            :curl (println (request->curl request))
            (do (println "✗ unknown format:" (name (:as opts)))
                (System/exit 1))))
        (catch Exception e
          (println "✗" (.getMessage e))
          (System/exit 1)))
      :else
      (try
        (print-response (run-template template-name overrides))
        (catch Exception e
          (println "✗" (.getMessage e))
          (System/exit 1))))))
