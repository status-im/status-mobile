(ns status-im2.contexts.chat.messages.composer.mentions.view
  (:require [utils.re-frame :as rf]
            [react-native.core :as rn]
            [react-native.reanimated :as reanimated]
            [status-im2.common.contact-list-item.view :as contact-list-item]))

(defn mention-item
  [user _ _ text-input-ref]
  [contact-list-item/contact-list-item
   {:on-press #(rf/dispatch [:chat.ui/select-mention text-input-ref user])} user])

(defn mentions
  [{:keys [refs suggestions max-y]} insets]
  [:f>
   (fn []
     (let [translate-y (reanimated/use-shared-value 0)]
       (rn/use-effect
        (fn []
          (reanimated/set-shared-value translate-y
                                       (reanimated/with-timing (if (seq suggestions) 0 200)))))
       [reanimated/view
        {:style (reanimated/apply-animations-to-style
                 {:transform [{:translateY translate-y}]}
                 {:bottom     (or (:bottom insets) 0)
                  :position   :absolute
                  :left       0
                  :right      0
                  :z-index    5
                  :elevation  5
                  :max-height (/ max-y 2)})}
        [rn/flat-list
         {:keyboardShouldPersistTaps :always
          :data                      (vals suggestions)
          :key-fn                    :key
          :render-fn                 mention-item
          :render-data               (:text-input-ref refs)
          :accessibility-label       :mentions-list}]]))])
