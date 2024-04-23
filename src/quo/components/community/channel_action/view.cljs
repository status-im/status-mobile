(ns quo.components.community.channel-action.view
  (:require
    [quo.components.community.channel-action.style :as style]
    [quo.components.counter.counter.view :as counter]
    [quo.components.icon :as icons]
    [quo.components.markdown.text :as text]
    [quo.theme]
    [react-native.core :as rn]))

(defn view
  [{:keys [big? customization-color label counter-value icon on-press accessibility-label disabled?]}]
  (let [theme                  (quo.theme/use-theme)
        [pressed? set-pressed] (rn/use-state false)
        on-press-in            (rn/use-callback #(set-pressed true))
        on-press-out           (rn/use-callback #(set-pressed false))]
    [rn/view
     {:accessibility-label :channel-action
      :style               (style/channel-action-container
                            {:big?      big?
                             :disabled? disabled?})}
     [rn/pressable
      (cond-> {:style               (style/channel-action
                                     {:big?      big?
                                      :color     customization-color
                                      :pressed?  pressed?
                                      :theme     theme
                                      :disabled? disabled?})
               :accessibility-label accessibility-label}
        (not disabled?) (assoc :on-press     on-press
                               :on-press-in  on-press-in
                               :on-press-out on-press-out))
      [rn/view {:style style/channel-action-row}
       [icons/icon icon]
       (when counter-value
         [counter/view {:type :secondary} counter-value])]
      [text/text
       {:size            :paragraph-1
        :weight          :medium
        :number-of-lines 2}
       label]]]))
