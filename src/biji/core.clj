(ns biji.core
  (:require [clojure.tools.logging :as log]
            [biji.irc :as irc]))


(defn -main [& args]
  (try
    (let [freenode     {:host "irc.freenode.net"   :port 6667}
          freenode-ssl {:host "irc.freenode.net"   :port 6697 :ssl? true}
          azzurra      {:host "nexlab.azzurra.org" :port 6667}
          azzurra-ssl  {:host "nexlab.azzurra.org" :port 443  :ssl? true}
          localhost    {:host "127.0.0.1"          :port 6667}
          conn (irc/connect azzurra-ssl)]

      (defn reply [msg]
        (irc/send-msg conn
                      (or (:chan msg) (:user msg))
                      (str "echoing: " (:text msg))))

      (irc/set-on-msg conn reply)
      (irc/login conn {:name "Biji the Wise"
                       :nick "biji"})
      (irc/write conn "JOIN #biji-test")
      ;(write conn "QUIT")
      )
    (catch java.net.UnknownHostException e
      (log/error "Unknow host"))
    (catch java.net.ConnectException e
      (log/error "Connection timed out"))))
