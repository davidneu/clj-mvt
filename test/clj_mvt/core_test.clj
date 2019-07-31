(ns clj-mvt.core-test
  (:require
   [clojure.test :refer :all]
   [clj-mvt.breakpoint :refer [break]]))

(deftest pass-test
  (is (= 1 1)))

(deftest fail-test
  (is (= 0 1)))

(defn stacktrace-example []
  (/ 1 0))

(defn break-example []
  ;; d is randomly set to 0 or 1.
  (let [n 5
        d (rand-int 2)]
    (break)
    (/ n d)))

(defn break-loop-example []
  (dotimes [n 5]
    ;; d is randomly set to 0 or 1.
    (let [d (rand-int 2)]
      (break)
      (/ n d))))

(comment

  ;; (defn compile-syntax-check-phase-error-example []
  ;;     "Unable to resolve symbol"
  ;;     (let [x 1
  ;;           y z]
  ;;       (println x)))

  ;; (defn macro-syntax-check-phase-error-example []
  ;;     "Even number of forms"
  ;;     (let [x 1
  ;;           y]
  ;;       (println x)))
  
  )

