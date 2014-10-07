(ns leiningen.quickie
  (:require [leiningen.core.eval :as eval]))

(defn paths [parameters project]
  (assoc parameters :paths (vec (concat (:source-paths project) (:test-paths project)))))

(defn default-pattern [project]
  (let [name (or (:name project)
                 (:group project))]
    (re-pattern (str name ".*test"))))

(defn test-matcher [project args]
  (cond
   (> (count args) 0)      (re-pattern (first args))
   (:test-matcher project) (:test-matcher project)
   :else                   (default-pattern project)))

(defn run-parallel [project & args]
  (eval/eval-in-project 
    (update-in project [:dependencies] conj ['quickie "0.3.0"])
    (let [parameters (-> {}
                         (paths project)
                         (assoc :test-matcher (test-matcher project args)))]
      `(quickie.runner/run-parallel ~parameters))
    `(require 'quickie.runner)))

(defn quickie
  "Automatically run tests when clj files change"
  [project & args]
  (eval/eval-in-project 
    (update-in project [:dependencies] conj ['quickie "0.3.0"])
    (let [parameters (-> {}
                         (paths project)
                         (assoc :test-matcher (test-matcher project args)))]
      `(quickie.autotest/run ~parameters))
    `(require 'quickie.autotest)))
