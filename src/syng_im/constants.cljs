(ns syng-im.constants)

(def ethereum-rpc-url "http://localhost:8545")

(def server-address "http://rpc0.syng.im:20000/")
;; (def server-address "http://10.0.3.2:3000/")

(def text-content-type "text/plain")
(def content-type-command "command")
(def content-type-command-request "command-request")
(def content-type-status "status")
(def group-chat-colors [{:background "#AB7967", :text "#FFFFFF"}
                        {:background "#B48EAD", :text "#FFFFFF"}
                        {:background "#8FA1B3", :text "#FFFFFF"}
                        {:background "#96B5B4", :text "#FFFFFF"}
                        {:background "#A3BE8C", :text "#FFFFFF"}
                        {:background "#EBCB8B", :text "#FFFFFF"}
                        {:background "#D08770", :text "#FFFFFF"}
                        {:background "#BF616A", :text "#FFFFFF"}
                        {:background "#EFF1F5", :text "#000000"}
                        {:background "#DFE1E8", :text "#000000"}
                        {:background "#C0C5CE", :text "#000000"}
                        {:background "#A7ADBA", :text "#000000"}
                        {:background "#65737E", :text "#FFFFFF"}
                        {:background "#4F5B66", :text "#FFFFFF"}
                        {:background "#343D46", :text "#FFFFFF"}
                        {:background "#2B303B", :text "#FFFFFF"}])

(comment

  (map (fn [c]
         {:background c
          :foreground c}) group-chat-colors)
  (reverse group-chat-colors)

  )
