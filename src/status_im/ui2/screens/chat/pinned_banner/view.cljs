(ns status-im.ui2.screens.chat.pinned-banner.view
  (:require
   [quo2.core :as quo]
   [quo2.foundations.colors :as colors]
   [react-native.core :as rn]))

;; TODO (flexsurfer) this should be a banner component in quo2
;; https://github.com/status-im/status-mobile/issues/14528
(defn pinned-banner
  [{:keys [latest-pin-text pins-count on-press]}]
  [rn/touchable-opacity
   {:accessibility-label :pinned-banner
    :style               {:height             50
                          :background-color   colors/primary-50-opa-20
                          :flex-direction     :row
                          :align-items        :center
                          :padding-horizontal 20
                          :padding-vertical   10}
    :active-opacity      1
    :on-press            on-press}
   [quo/icon :i/pin {:size 20}]
   [quo/text
    {:number-of-lines 1
     :size            :paragraph-2
     :style           {:margin-left 10 :margin-right 50}}
    latest-pin-text]
   [rn/view
    {:accessibility-label :pins-count
     :style               {:position         :absolute
                           :right            22
                           :height           20
                           :width            20
                           :border-radius    8
                           :justify-content  :center
                           :align-items      :center
                           :background-color colors/neutral-80-opa-5}}
    [quo/text {:size :label :weight :medium} pins-count]]])
