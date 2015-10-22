(ns biji.core
  (:require [biji.irc :as irc]))


(defn -main [& args]
  (let [freenode {:name "irc.freenode.net"
                  :port 6667}
        localhost {:name "127.0.0.1"
                   :port 6667}
        conn (irc/connect localhost)]

    (defn reply [msg]
      (irc/send-msg conn
                    (or (:chan msg) (:user msg))
                    (str "echoing: " (:text msg))))

    (irc/set-on-msg conn reply)
    (irc/login conn {:name "Biji the Wise"
                     :nick "biji"})
    (irc/write conn "JOIN #biji-test")
    ;(write conn "QUIT")
    ))
