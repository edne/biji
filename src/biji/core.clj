(ns biji.core
  (:require [clojure.tools.logging :as log]
            [biji.irc :as irc]
            [biji.memory :as memory]))

(load-file "config.clj")


(defn -main [& args]
  (try
    (let [conn (irc/connect server)
          mem  (memory/create memory-file)]

      (defn on-msg [msg]
        (if (re-find #"biji" (:text msg))
          (irc/send-msg conn
                        (or (:chan msg) (:user msg))
                        (memory/pick mem))
          (memory/append mem msg)))

      (irc/set-on-msg conn on-msg)
      (irc/login conn {:name "Biji the Wise"
                       :nick "biji"})
      (irc/write conn (str "JOIN " channel))
      ;(write conn "QUIT")
      )
    (catch java.net.UnknownHostException e
      (log/error "Unknow host"))
    (catch java.net.ConnectException e
      (log/error "Connection timed out"))))
