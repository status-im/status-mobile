(ns status-im.navigation2.screens
  (:require [status-im.ui2.screens.chat.view :as chat]
            [status-im.switcher.home-stack :as home-stack]
            [status-im.navigation2.stack-with-switcher :as stack-with-switcher]))

;; We have to use the home screen name :chat-stack for now, for compatibility with navigation.cljs
(def screens [{:name      :chat-stack                       ;; TODO(parvesh) - rename to home-stack
               :insets    {:top false}
               :component home-stack/home}])

;; These screens will overwrite navigation/screens.cljs screens on enabling new UI toggle
(def screen-overwrites
  [{:name      :chat
    :options   {:topBar {:visible false}}
    :component #(stack-with-switcher/overlap-stack chat/chat :chat)}])
