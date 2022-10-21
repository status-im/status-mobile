(ns quo2.components.list-items.received-contact-request
  (:require [quo.react-native :as rn]
            [quo2.foundations.colors :as colors]
            [quo2.components.counter.counter :as quo2.counter]
            [quo2.components.icon :as quo2.icons]
            [quo2.components.avatars.channel-avatar :as channel-avatar]
            [quo2.components.markdown.text :as quo2.text]
            [quo.theme :as theme]))


(defn list-item [item]
  (println "itexs" item)
  [rn/view
   [rn/text "SOME ITEM"]])
