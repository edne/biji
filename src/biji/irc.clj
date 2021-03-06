(ns biji.irc
  "IRC wrapper"
  (:require [clojure.string :as s]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log])
  (:import java.net.Socket
           javax.net.ssl.SSLSocketFactory))


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
     :text text
     :raw  msg}))


(defn send-msg
  "Send message to a destination (user or channel"
  [conn dest text]
  (let [raw-msg (str "PRIVMSG " dest " :" text)]
    (log/info "sending: " raw-msg)
    (write conn raw-msg)))


(defn connect
  "Connect to a server
  (connect {:host \"irc.freenode.net\" :port 6667})"
  [server]
  (let [host (:host server)
        port (:port server)
        ;socket (Socket. host port)
        socket (if (:ssl? server)
                 (.createSocket (SSLSocketFactory/getDefault) host port)
                 (Socket. host port))
        conn (atom {:in  (io/reader socket)
                    :out (io/writer socket)
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
  (binding [*out* (:out @conn)]
    (println msg)
    (flush)))


(defn login
  "Login a user
  (login conn {:name \"John Antani\" :nick \"antani\"})"
  [conn user]
  (let [nick      (:nick user)
        real-name (:name user)]
    (write conn (str "NICK " nick))
    (write conn (str "USER " nick " 0 * :" real-name))))
