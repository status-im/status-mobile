(ns quo2.components.list-items.channel
  (:require [quo2.components.avatars.channel-avatar.view :as channel-avatar]
            [quo2.components.common.unread-grey-dot.view :as unread-grey-dot]
            [quo2.components.counter.counter.view :as counter]
            [quo2.components.icon :as quo2.icons]
            [quo2.components.markdown.text :as quo2.text]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]))

(def ^:private custom-props
  [:name :locked? :mentions-count :unread-messages?
   :muted? :is-active-channel? :emoji :channel-color])

(defn list-item
  [{:keys [locked? mentions-count unread-messages?
           muted? is-active-channel? emoji channel-color
           default-color]
    :as   props}]
  (let [channel-color  (or channel-color default-color)
        standard-props (apply dissoc props custom-props)
        name-text      (:name props)]
    [rn/touchable-opacity standard-props
     [rn/view
      {:style (cond-> {:height          48
                       :border-radius   12
                       :flex-direction  :row
                       :justify-content :space-between
                       :align-items     :center
                       :width           "100%"
                       :padding-left    12
                       :padding-right   12}
                is-active-channel? (assoc :background-color
                                          (colors/theme-alpha channel-color 0.05 0.05)))}
      [rn/view
       {:style               {:flex-direction  :row
                              :justify-content :flex-start
                              :align-items     :center}
        :accessibility-label :chat-name-text}
       [channel-avatar/view
        {:size                :size/l
         :locked?             locked?
         :full-name           (:name props)
         :customization-color channel-color
         :emoji               emoji}]
       [quo2.text/text
        {:style  (cond-> {:margin-left 12}
                   (and (not locked?) muted?)
                   (assoc :color (colors/theme-colors colors/neutral-40 colors/neutral-60)))
         :weight :medium
         :size   :paragraph-1}
        (str "# " name-text)]]
      (when-not locked?
        [rn/view {:style {:height 20 :justify-content :center}}
         (cond
           muted?
           [quo2.icons/icon :i/muted
            {:size            20
             :color           colors/neutral-40
             :container-style {:margin-right 1 :margin-top 2}}]

           (pos? (int mentions-count))
           [rn/view {:style {:margin-right 2 :margin-top 2}}
            [counter/view {:customization-color channel-color}
             mentions-count]]

           unread-messages?
           [unread-grey-dot/unread-grey-dot :unviewed-messages-public])])]]))
