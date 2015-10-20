(ns biji.irc
  "IRC wrapper"
  (:import (java.net Socket)
           (java.io PrintWriter
                    InputStreamReader
                    BufferedReader)))

(declare connect)
(declare write)
(declare login)


(defn connect
  "Connect to a server
  (connect {:name \"irc.freenode.net\" :port 6667})
  "
  [server]
  (let [socket (Socket. (:name server)
                        (:port server))
        in   (BufferedReader. (InputStreamReader. (.getInputStream socket)))
        out  (PrintWriter. (.getOutputStream socket))
        conn (ref {:in in :out out})]

    (defn conn-handler [conn]
      (while (nil? (:exit @conn))
        (let [msg (.readLine (:in @conn))]
          (println msg)
          (cond
            (re-find #"^ERROR :Closing Link:" msg)
            (dosync (alter conn merge {:exit true}))

            (re-find #"^PING" msg)
            (write conn (str "PONG "  (re-find #":.*" msg)))))))

    (doto (Thread. #(conn-handler conn))
      (.start))

    conn))


(defn write
  "Write on the socket
  (write conn (str \"NICK \" nick))
  "
  [conn msg]
  (doto (:out @conn)
    (.println (str msg "\r"))
    (.flush)))


(defn login
  "Login a user
  (login conn {:name \"Mario Antani\" :nick \"antani\"})
  "
  [conn user]
  (let [nick      (:nick user)
        real-name (:name user)]
    (write conn (str "NICK " nick))
    (write conn (str "USER " nick " 0 * :" real-name))))
