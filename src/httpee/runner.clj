(ns httpee.runner
  "Template loading + execution. Reads `httpee.edn`, resolves template files
  relative to cwd, evaluates them with `httpee/*vars*` bound to merged
  config + overrides, and fires the HTTP request via babashka.http-client.

  Loading this namespace also wires `httpee/*run-template*` so that
  `(httpee/json-request \"...\")` from inside a template can call back into
  the runner for nested fetches."
  (:require [babashka.fs :as fs]
            [babashka.http-client :as http]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [httpee]))

(def ^:private running (atom #{}))

(defn read-config []
  (if (fs/exists? "httpee.edn")
    (edn/read-string (slurp "httpee.edn"))
    {:variables {}}))

(defn read-template-forms [path]
  ;; Read all top-level forms, so a template can have a leading `require`
  ;; / `add-deps` plus the request map itself.
  (read-string (str "[" (slurp path) "]")))

(defn discover-templates [dirs]
  (->> dirs
       (mapcat (fn [dir]
                 (when (fs/exists? dir)
                   (for [p (fs/glob dir "**.clj")]
                     (-> p str (str/replace #"\.clj$" ""))))))
       sort))

(defn build-request [name overrides]
  (when (@running name)
    (throw (ex-info (str "cycle detected on " name) {})))
  (let [path (str name ".clj")]
    (when-not (fs/exists? path)
      (throw (ex-info (str "template not found: " name) {})))
    (swap! running conj name)
    (try
      (let [forms (read-template-forms path)
            vars  (merge (:variables (read-config)) overrides)]
        (binding [httpee/*vars* vars]
          (eval (cons 'do forms))))
      (finally (swap! running disj name)))))

(defn run-template [name overrides]
  (let [request  (build-request name overrides)
        start    (System/currentTimeMillis)
        response (http/request request)]
    (assoc response
           :elapsed-ms (- (System/currentTimeMillis) start)
           :request    request)))

;; Wire httpee/json-request so it can call back into the runner once any
;; consumer has required this namespace.
(alter-var-root #'httpee/*run-template* (constantly run-template))
