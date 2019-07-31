(ns clj-mvt.repl
  (:require
   [clj-mvt.error :refer [print-error]]))

(defn init []
  (require 'complete.core)
  (set! *print-level* 10)
  (set! *print-length* 10)
  (println "***** Type (dev) to get started. *****\n"))

(defn repl
  "REPL with customized :init and :caught options."
  []
  (clojure.main/repl
   :init
   (fn []
     (init))
   :caught
   (fn [e]
     (print-error e))))

(defn socket-repl
  "REPL with predefined hooks for attachable socket server and
  customized :init and :caught options."
  []
  (clojure.main/repl
   :init
   (fn []
     (clojure.core.server/repl-init)
     (init))
   :read clojure.core.server/repl-read
   :caught
   (fn [e]
     (print-error e))))

