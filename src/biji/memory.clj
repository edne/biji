(ns biji.memory
  "Momory handling"
  (:require [clojure.tools.logging :as log]))


(defn create
  "Create a new memory atom"
  []
  (atom []))


(defn append
  "Append a new message to memory"
  [mem msg]
  (swap! mem conj msg))


(defn show [mem]
  "Printble memory"
  (str @mem)) 


(defn pick [mem]
  (if (empty? @mem)
    "nope"
    (:text (rand-nth @mem))))
