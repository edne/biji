(def server
  (let [freenode     {:host "irc.freenode.net"   :port 6667}
        freenode-ssl {:host "irc.freenode.net"   :port 6697 :ssl? true}
        azzurra      {:host "nexlab.azzurra.org" :port 6667}
        azzurra-ssl  {:host "nexlab.azzurra.org" :port 443  :ssl? true}
        localhost    {:host "127.0.0.1"          :port 6667}]

    localhost))

(def channel "#biji-test")
(def memory-file (str (System/getenv "HOME")
                      "/.biji-memory.txt"))
