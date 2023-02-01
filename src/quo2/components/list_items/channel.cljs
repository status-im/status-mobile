(ns quo2.components.list-items.channel
  (:require [quo2.components.avatars.channel-avatar :as channel-avatar]
            [quo2.components.counter.counter :as quo2.counter]
            [quo2.components.icon :as quo2.icons]
            [quo2.components.markdown.text :as quo2.text]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]
            [react-native.core :as rn]))

(defn list-item
  [{:keys [name locked? mentions-count unread-messages?
           muted? is-active-channel? emoji channel-color on-press]
    :or   {channel-color colors/primary-50}}]
  [rn/touchable-opacity {:on-press on-press}
   [rn/view
    {:style (merge {:height          48
                    :display         :flex
                    :border-radius   12
                    :flex-direction  :row
                    :justify-content :space-between
                    :align-items     :center
                    :width           "100%"
                    :padding-left    12
                    :padding-right   12}
                   (when is-active-channel?
                     {:background-color (colors/theme-alpha channel-color 0.05 0.05)}))}
    [rn/view
     {:display             :flex
      :flex-direction      :row
      :justify-content     :flex-start
      :align-items         :center
      :accessible          true
      :accessibility-label :chat-name-text}
     [channel-avatar/channel-avatar
      {:big?                   true
       :locked?                locked?
       :emoji-background-color (colors/theme-alpha channel-color 0.1 0.1)
       :emoji                  emoji}]
     [quo2.text/text
      {:style  (merge {:margin-left 12}
                      (when (and (not locked?) muted?)
                        {:color (if (theme/dark?) colors/neutral-60 colors/neutral-40)}))
       :weight :medium
       :size   :paragraph-1} (str "# " name)]]
    [rn/view
     {:style {:height          20
              :justify-content :center}}
     (when (and (not locked?)
                muted?)
       [quo2.icons/icon :i/muted
        {:size     20
         :no-color true}])
     (when (and (not locked?)
                (not muted?)
                (pos? (int mentions-count)))
       [rn/view
        {:style {:margin-right 2
                 :margin-top   2}}
        [quo2.counter/counter {:override-bg-color channel-color} mentions-count]])
     (when (and (not locked?)
                (not muted?)
                (not (pos? (int mentions-count)))
                unread-messages?)
       [rn/view
        {:style {:width            8
                 :height           8
                 :border-radius    4
                 :background-color (colors/theme-colors
                                    colors/neutral-40
                                    colors/neutral-60)}}])]]])
