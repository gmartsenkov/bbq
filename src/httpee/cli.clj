(ns httpee.cli
  "CLI surface: the babashka.cli spec, override-arg parsing, and the
  --help printer. The dispatch itself stays in httpee.bb."
  (:require [babashka.cli :as cli]
            [clojure.string :as str]))

(def spec
  {:spec {:help     {:coerce :boolean :alias :h :desc "show this help"}
          :complete {:coerce :boolean :desc "list template names for shell completion"}
          :as       {:coerce :keyword :desc "render as a snippet (curl)"}}})

(defn parse-overrides [args]
  (into {} (for [a args :let [[k val] (str/split a #"=" 2)]]
             [(keyword k) val])))

(defn print-help []
  (println "usage: bb httpee.bb <template> [k=v ...]")
  (println)
  (println (cli/format-opts spec)))
