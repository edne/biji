(ns biji.memory
  "Momory handling"
  (:require [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [biji.irc :as irc]))


(defn create
  "Create a new memory atom"
  [file-name]
  (let [file (io/as-file file-name)
        msg-list (if (.exists file)
                   (map irc/parse-msg
                        (with-open [rdr (io/reader file-name)]
                          (-> rdr line-seq doall)))
                   [])]

    (atom {:msg-list  msg-list
           :file-name file-name})))


(defn append
  "Append a new message to memory"
  [mem msg]

  (with-open [w (-> @mem :file-name
                    (io/writer :append true))]
    (binding [*out* w]
      (-> msg :raw println)))

  (swap! mem update-in [:msg-list] conj msg))


(defn show [mem]
  "Printble memory"
  (str @mem)) 


(defn pick [mem]
  "Take a random element"
  (if (-> @mem empty? :msg-list)
    "nope"
    (-> @mem :msg-list rand-nth :text)))
