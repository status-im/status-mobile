(ns status-im.ui2.screens.chat.composer.reply
  (:require [quo.react-native :as rn]
            [status-im.ui2.screens.chat.components.reply.view :as reply]
            [status-im.ui2.screens.chat.composer.input :as input]))

(defn focus-input-on-reply
  [reply had-reply text-input-ref]
  ;;when we show reply we focus input
  (when-not (= reply @had-reply)
    (reset! had-reply reply)
    (when reply
      ;; A setTimeout of 0 is necessary to ensure the statement is enqueued and will get executed ASAP.
      (js/setTimeout #(input/input-focus text-input-ref) 0))))

(defn reply-message-auto-focus-wrapper
  [text-input-ref _]
  (let [had-reply (atom nil)]
    (fn [_ reply]
      (focus-input-on-reply reply had-reply text-input-ref)
      (when reply
        [rn/view
         {:style {:padding-horizontal 15
                  :padding-vertical   8}}
         [reply/reply-message reply true]]))))
