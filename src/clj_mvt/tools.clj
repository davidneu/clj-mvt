(ns clj-mvt.tools
  (:require
   [clojure.string :as str]
   [clojure.tools.namespace.repl :as ctnr]
   [clojure.java.io :as io]
   [clojure.tools.deps.alpha.script.generate-manifest]
   [java-time :as jt]
   [eftest.runner :as eftest]
   [clj-mvt.error]))

;; (clojure.tools.namespace.repl/disable-unload!)

(defn refresh [& options]
  (clojure.tools.namespace.repl/set-refresh-dirs "src" "test")
  ;; (clojure.tools.namespace.repl/set-refresh-dirs "dev" "src" "test")
  (let [r (apply clojure.tools.namespace.repl/refresh options)]
    (if (instance? Exception r)
      (clj-mvt.error/print-error r)
      r)))

(defn dev []
  (refresh)
  (in-ns 'dev)
  :ok)

(defn reset
  [ns]
  (let [reset-fn (ns-resolve ns 'reset)]
    (if (nil? reset-fn)
      (do
        (println (format "\n***** %s/reset not found - running clj-mvt.tools/refresh *****\n" ns))
        (refresh))
      (reset-fn))))

(defn testit []
  (binding [clojure.test/*test-out* *out*]
    (eftest/run-tests (eftest/find-tests "test"))))

(defn touch [fs]
  (doseq [f fs]
    (let [af (clojure.java.io/file f)]
      (if (.exists af)
        (.setLastModified
         af
         (jt/to-millis-from-epoch (jt/instant)))
        (throw (Exception. (format "File %s not found" (.getCanonicalPath af))))))))

(defn pom [version]
  (let [pf "pom.xml"]
    (when (.isFile (io/file pf))
      (.delete (io/file pf)))
    (clojure.tools.deps.alpha.script.generate-manifest/-main
     "--config-files" "deps.edn"
     "--gen" "pom")
    (spit
     pf
     (str/replace-first
      (slurp pf)
      #"<version>\S+</version>"
      (format "<version>%s</version>" version))
     :append false)))

(defn -main [command & args]
  (cond
    (and (= command "--pom") (= (count args) 1))
    (pom (first args))
    :else
    (throw
     (ex-info (format "Unknown command: %s or argss: %s" command args)))))

