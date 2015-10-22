(ns biji.irc
  "IRC wrapper"
  (:require [clojure.string :as s]
            [clojure.tools.logging :as log])
  (:import (java.net Socket)
           (java.io PrintWriter
                    InputStreamReader
                    BufferedReader)))


(declare write)


(defn parse-msg
  "Parse a raw message to a map, example message:
  :edne!~edne@poul.org PRIVMSG #biji-test :hi!"
  [msg]
  (let [user (-> (re-find #"^:\S*" msg)
                 (s/split #":") second
                 (s/split #"!") first)

        addr (-> (re-find #"^:\S*" msg)
                 (s/split #":")
                 second
                 (s/split #"!")
                 second)

        dest (-> msg
                 (s/split #":")
                 second
                 (s/split #" ")
                 last)

        chan (re-find #"#.*" dest)

        text (-> msg
                 (s/split #":")
                 (->> (drop 2)
                      (s/join ":")))]
    {:user user
     :addr addr
     :dest dest
     :chan chan
     :text text}))


(defn send-msg
  "Send message to a destination (user or channel"
  [conn dest text]
  (let [raw-msg (str "PRIVMSG " dest " :" text)]
    (log/info "sending: " raw-msg)
    (write conn raw-msg)))


(defn connect
  "Connect to a server
  (connect {:name \"irc.freenode.net\" :port 6667})"
  [server]
  (let [socket (Socket. (:name server)
                        (:port server))
        in   (BufferedReader. (InputStreamReader. (.getInputStream socket)))
        out  (PrintWriter. (.getOutputStream socket))
        conn (atom {:in  in
                    :out out
                    :cb  (fn [msg])})]

    (defn conn-handler
      "While the connection is alive:
      - call the on-message callback
      - handle errors
      - respond to pings"
      [conn]
      (while (nil? (:exit @conn))
        (let [msg (.readLine (:in @conn))]
          (log/info msg)
          (cond
            (re-find #"^:\S* PRIVMSG" msg)
            ((:cb @conn) (parse-msg msg))

            (re-find #"^ERROR :Closing Link:" msg)
            (dosync (alter conn merge {:exit true}))

            (re-find #"^PING" msg)
            (write conn (str "PONG " (re-find #":.*" msg)))))))

    (doto (Thread. #(conn-handler conn))
      (.start))

    conn))


(defn set-on-msg
  "Set the on-message callback"
  [conn cb]
  (swap! conn assoc :cb cb))


(defn write
  "Write on the socket
  (write conn (str \"NICK \" nick))"
  [conn msg]
  (doto (:out @conn)
    (.println (str msg "\r"))
    (.flush)))


(defn login
  "Login a user
  (login conn {:name \"John Antani\" :nick \"antani\"})"
  [conn user]
  (let [nick      (:nick user)
        real-name (:name user)]
    (write conn (str "NICK " nick))
    (write conn (str "USER " nick " 0 * :" real-name))))
