(ns status-im.common.new-device-sheet.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.common.events-helper :as events-helper]
    [status-im.common.new-device-sheet.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- pair-and-sync
  [installation-id]
  (rf/dispatch [:pairing/pair-and-sync installation-id])
  (events-helper/hide-bottom-sheet))

(defn installation-request-receiver-view
  [installation-id]
  (rn/use-mount events-helper/dismiss-keyboard)
  [:<>
   [quo/text
    {:weight              :semi-bold
     :size                :heading-2
     :accessibility-label :new-device-sheet-heading
     :style               style/heading}
    (i18n/label :t/pair-new-device-and-sync)]
   [quo/text
    {:weight              :regular
     :size                :paragraph-1
     :accessibility-label :new-device-sheet-message
     :style               style/message}
    (i18n/label :t/new-device-detected-recovered-device-message)]
   [quo/text
    {:weight              :semi-bold
     :size                :heading-2
     :accessibility-label :new-device-installation-id
     :style               style/heading}
    installation-id]
   [quo/bottom-actions
    {:actions          :two-actions
     :blur?            true
     :container-style  {:margin-top 12}
     :button-two-label (i18n/label :t/cancel)
     :button-two-props {:type     :grey
                        :on-press events-helper/hide-bottom-sheet}
     :button-one-label (i18n/label :t/pair-and-sync)
     :button-one-props {:on-press #(pair-and-sync installation-id)}}]])

(defn installation-request-creator-view
  [installation-id]
  (rn/use-mount events-helper/dismiss-keyboard)
  [:<>
   [quo/text
    {:weight              :semi-bold
     :size                :heading-2
     :accessibility-label :new-device-sheet-heading
     :style               style/heading}
    (i18n/label :t/pair-this-device-and-sync)]
   [quo/text
    {:weight              :regular
     :size                :paragraph-1
     :accessibility-label :new-device-sheet-message
     :style               style/message}
    (i18n/label :t/new-device-detected-other-device-message)]
   [quo/text
    {:weight              :semi-bold
     :size                :heading-2
     :accessibility-label :new-device-installation-id
     :style               style/heading}
    installation-id]
   [quo/bottom-actions
    {:actions          :one-action
     :blur?            true
     :container-style  {:margin-top 12}
     :button-one-label (i18n/label :t/close)
     :button-one-props {:type     :grey
                        :on-press events-helper/hide-bottom-sheet}}]])
