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
         {:style (merge
                  {:padding-horizontal 15
                   :padding-vertical   8}
                  (when @input/recording-audio?
                    {:position :absolute
                     :top      12
                     :left     0
                     :right    0
                     ;;When recording an audio and replying at the same time,
                     ;;text input is overlapped by the reply component but
                     ;;text input still have priority over touches, so we need
                     ;;to force the reply component to receive the touches in this
                     ;;scenario, thus we increase its z-index
                     :z-index  1}))}
         [reply/reply-message reply true false
          (and @input/recording-audio? (not @input/reviewing-audio?))]]))))
