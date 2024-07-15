(ns status-im.common.new-device-sheet.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.common.new-device-sheet.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- dismiss-keyboard
  []
  (rf/dispatch [:dismiss-keyboard]))

(defn- hide-bottom-sheet
  []
  (rf/dispatch [:hide-bottom-sheet]))

(defn- pair-and-sync [installation-id]
  (rf/dispatch [:pairing/pair-and-sync installation-id])
  (hide-bottom-sheet))

(defn view
  [installation-id]
  (dismiss-keyboard)
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
    (i18n/label :t/new-device-detected)]
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
     :button-two-props {:type      :grey
                        :on-press hide-bottom-sheet}
     :button-one-label (i18n/label :t/pair-and-sync)
     :button-one-props {:on-press #(pair-and-sync installation-id)}}]])

(defn view-2
  []
  (let [installation-id (rf/sub [:profile/installation-id])]
    (dismiss-keyboard)
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
      (i18n/label :t/check-new-device)]
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
       :button-one-props {:type      :grey
                          :on-press hide-bottom-sheet}}]]))
