(ns status-im2.contexts.syncing.sheets.sync-device-notice.view
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [status-im2.contexts.syncing.sheets.enter-password.view :as enter-password]
            [status-im2.contexts.syncing.sheets.sync-device-notice.styles :as styles]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [status-im2.common.resources :as resources]))

(defn sheet
  []
  [:<>
   [rn/view {:style styles/sync-devices-header}
    [rn/image
     {:source (resources/get-image :sync-device)
      :style  styles/sync-devices-header-image}]]
   [rn/view {:style styles/sync-devices-body-container}
    [quo/text
     {:accessibility-label :privacy-policy
      :weight              :bold
      :size                :heading-1
      :style               styles/header-text}
     (i18n/label :t/sync-new-device)]

    [quo/text
     {:accessibility-label :privacy-policy
      :weight              :regular
      :size                :paragraph-1
      :style               styles/instructions-text}
     (i18n/label :t/sync-instructions-text)]

    [quo/text
     {:accessibility-label :privacy-policy
      :weight              :regular
      :size                :paragraph-2
      :style               styles/list-item-text}
     (i18n/label :t/sync-instruction-step-1)]

    [quo/text
     {:accessibility-label :privacy-policy
      :weight              :regular
      :size                :paragraph-2
      :style               styles/list-item-text}
     (i18n/label :t/sync-instruction-step-2)]

    [quo/text
     {:accessibility-label :privacy-policy
      :weight              :regular
      :size                :paragraph-2
      :style               styles/list-item-text}
     (i18n/label :t/sync-instruction-step-3)]

    [quo/button
     {:type     :secondary
      :size     40
      :style    styles/setup-syncing-button
      :before   :i/face-id20
      :on-press #(rf/dispatch [:show-bottom-sheet
                               ;; this should be a modal screen
                               {:content (fn []
                                           [enter-password/sheet])}])}
     (i18n/label :t/setup-syncing)]]])
