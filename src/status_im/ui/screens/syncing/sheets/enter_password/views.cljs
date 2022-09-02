(ns status-im.ui.screens.syncing.sheets.enter-password.views
  (:require [clojure.string :as string]
            [quo.react-native :as rn]
            [status-im.ui.screens.syncing.sheets.enter-password.styles :as styles]
            [quo.core :as quo]
            [re-frame.core :as re-frame]
            [status-im.i18n.i18n :as i18n]
            [status-im.utils.security :as security]
            [status-im.ui.components.react :as react]
            [quo2.components.buttons.button :as quo2]
            [status-im.ui.components.toolbar :as toolbar]
            [taoensso.timbre :as log]
            [status-im.react-native.resources :as resources]))


(defn preperations-for-connection-string [^js password-text-input]
  (log/debug "entered password is "  password-text-input)
  )

(defn views []
  (let [window-width @(re-frame/subscribe [:dimensions/window-width])
        entered-password (atom "")]
    [:<>
     [rn/view {:style styles/body-container}
      [rn/text {:style styles/header-text} "Enter your password"]
      [react/view {:flex 1}
       [quo/text-input
        {:placeholder         (i18n/label :t/enter-your-password)
         :auto-focus          true
         :accessibility-label :password-input
         :show-cancel         false
         :on-change-text      #(reset! entered-password %)
         :secure-text-entry   true}]]
      [toolbar/toolbar
       {:size :large
        :center
        [react/view {:padding-horizontal 8}
         [quo/button
          {:on-press #(preperations-for-connection-string @entered-password)}
          "Generate Scan Sync Code"]]}]

      ]]))
