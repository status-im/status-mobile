(ns quo2.components.info.nfc-prompt
  (:require [quo.react-native :as rn]
            [quo.theme :as theme]
            [quo2.foundations.colors :as colors]
            [status-im.ui.components.icons.icons :as icons]))

(defn nfc-prompt
  [{:keys [prompt-status on-press]}]
  (let [dark? (theme/dark?)
        ready? (= :ready prompt-status)
        connected? (= :connected prompt-status)
        success? (= :success prompt-status)
        ready-or-connected? (or ready? connected?)]
    [rn/view {:style {:height "100%"
                      :flex-direction "column-reverse"}}
     [rn/view {:style {:background-color (if dark?
                                           colors/dark-prompt-bg
                                           colors/white)
                       :width "100%"
                       :justify-content :center
                       :align-items :center
                       :height 363}}
      [rn/view {:style (cond-> {:flex-direction :row
                                :justify-content :center
                                :align-items :center}
                         success? (assoc :display :none))}
       [rn/text {:style {:font-size 26
                         :font-weight "400"
                         :color (if dark?
                                  colors/dark-prompt-title
                                  colors/light-prompt-title)
                         :line-height 31
                         :text-align :center}}
        "Ready to Scan"]]
      [rn/view {:style {:justify-content :center
                        :align-items :center
                        :margin-top 29}}
       [icons/icon (if success?
                     :main-icons/nfc-prompt-success
                     (when ready-or-connected?
                       :main-icons/nfc-prompt))
        {:width 114
         :color "nil"
         :height 114}]
       [rn/view {:style {:width 291
                         :height 95
                         :justify-content :center
                         :align-items :center}}
        [rn/text {:style {:font-size 16
                          :font-weight "400"
                          :color (if dark?
                                   colors/white
                                   colors/neutral-95)
                          :line-height 20}}
         (if success?
           "Success"
           (if (= :ready prompt-status)
             "Hold your iPhone near a Status Keycard"
             "Connected. Don’t move your card."))]]
       [rn/touchable-opacity {:style (cond-> {:padding-vertical 18
                                              :padding-horizontal 10
                                              :align-items :center
                                              :justify-content :center
                                              :width 291
                                              :border-radius 10
                                              :background-color (if dark?
                                                                  colors/dark-prompt-button
                                                                  colors/light-prompt-button)
                                              :height 52}
                                       success? (assoc :display :none))
                              :on-press (fn [] on-press)}
        [rn/text {:style {:color (if dark?
                                   colors/white
                                   colors/neutral-95)}}
         "Cancel"]]]]]))