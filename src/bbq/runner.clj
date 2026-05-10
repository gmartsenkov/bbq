(ns bbq.runner
  (:require [babashka.fs :as fs]
            [babashka.http-client :as http]
            [bbq]
            [clojure.edn :as edn]
            [clojure.string :as str]))

(def ^:private running (atom #{}))

(defn read-config []
  (if (fs/exists? "bbq.edn")
    (edn/read-string (slurp "bbq.edn"))
    {:variables {}}))

(defn read-template-forms [path]
  ;; Wrap in [...] so a template can have a leading `require` / `add-deps`
  ;; alongside the request map; read-string only reads one form otherwise.
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
        (binding [bbq/*vars* vars]
          (eval (cons 'do forms))))
      (finally (swap! running disj name)))))

(defn run-template [name overrides]
  (let [request  (build-request name overrides)
        start    (System/currentTimeMillis)
        response (http/request request)]
    (assoc response
           :elapsed-ms (- (System/currentTimeMillis) start)
           :request    request)))

;; Loading this ns wires bbq/json-request to call back into run-template.
(alter-var-root #'bbq/*run-template* (constantly run-template))
