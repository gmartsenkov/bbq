(ns bbq.cli
  (:require [babashka.cli :as cli]
            [clojure.string :as str]))

(def spec
  {:spec {:help     {:coerce :boolean :alias :h :desc "show this help"}
          :complete {:coerce :boolean :desc "list template names for shell completion"}
          :as       {:coerce :keyword :desc "render as a snippet (curl)"}
          :pager    {:coerce :string :desc "open the response body in a viewer (--pager defaults to =auto; or =bat, =less, =nvim, …)"}}})

(defn parse-overrides [args]
  (into {} (for [a args :let [[k val] (str/split a #"=" 2)]]
             [(keyword k) val])))

(defn print-help []
  (println "usage: bb bbq.bb <template> [k=v ...]")
  (println)
  (println (cli/format-opts spec)))
