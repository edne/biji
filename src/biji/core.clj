(ns biji.core
  (:require [biji.irc :as irc]))


(defn -main [& args]
  (let [conn (irc/connect {:name "irc.freenode.net"
                           :port 6667})]
    (irc/set-on-msg conn #(println %))
    (irc/login conn {:name "Biji the Wise"
                     :nick "biji"})
    (irc/write conn "JOIN #biji")
    ;(write conn "QUIT")
    ))
