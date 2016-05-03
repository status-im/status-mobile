(ns syng-im.constants)

(def ethereum-rpc-url "http://localhost:8545")

(def server-address "http://rpc0.syng.im:20000/")
;; (def server-address "http://10.0.3.2:3000/")

(def text-content-type "text/plain")
(def content-type-command "command")
(def content-type-command-request "command-request")
(def content-type-status "status")


(comment

  (map (fn [c]
         {:background c
          :foreground c}) group-chat-colors)
  (reverse group-chat-colors)

  )
