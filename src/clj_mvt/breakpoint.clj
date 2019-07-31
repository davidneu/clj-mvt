(ns clj-mvt.breakpoint
  (:require
   [clojure.main]))

(def ^:dynamic *level* 0)

(def ^:dynamic *locals*)

(def break? (atom false))

(defn break-on []
  (reset! break? true))

(defn break-off []
  (reset! break? false))

(defn toggle-break []
  (swap! break? (fn [b] (not b))))

(defmacro local-bindings []
  "Produces a map of the names of local bindings to their values."
  (let [symbols (keys &env)]
    (zipmap (map (fn [sym] `(quote ~sym)) symbols) symbols)))

(defn eval-with-locals [locals form]
  "Evals a form with given locals. The locals should be a map of symbols to values."
  (binding [*locals* locals]
    (eval
     `(let ~(vec (mapcat #(list % `(*locals* '~%)) (keys locals)))
        ~form))))

(defn readr [prompt exit-code]
  (let [input (clojure.main/repl-read prompt exit-code)]
    (cond (= input :exit)
          exit-code
          (= input :quit)
          (do
            (reset! break? false)
            exit-code)
          :else
          input)))

;; Usage: (break) or (break 'namespace-to-switch-to.core),
;; i.e. optionally pass in the namespace to switch to as a symbol.
(defmacro break
  ([] `(break (ns-name *ns*)))
  ([new-namespace-name]
   `(when @break?
      (let [namespace-name# (ns-name *ns*)]
        (when (not= ~new-namespace-name namespace-name#)
          (in-ns ~new-namespace-name))
        (let [eval-fn# (partial eval-with-locals (local-bindings))]
          (binding
              [*level* (inc *level*)]
              (clojure.main/repl
               :prompt #(print
                         (if (= *level* 1)
                           (format "%s:debug=> " (str *ns*))
                           (format "%s:debug-%d=> " (str *ns*) *level*)))
               :read readr
               :eval eval-fn#)))
        (when (not= ~new-namespace-name namespace-name#)
          (in-ns namespace-name#))))))

