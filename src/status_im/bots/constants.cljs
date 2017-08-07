(ns status-im.bots.constants)

(def mailman-bot "mailman")
(defn mailman-bot? [bot-name]
  (= mailman-bot bot-name))
