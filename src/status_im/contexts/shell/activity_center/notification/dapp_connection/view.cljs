(ns status-im.contexts.shell.activity-center.notification.dapp-connection.view
  (:require
    [quo.core :as quo]
    [quo.foundations.resources :as quo.resources]
    [status-im.contexts.shell.activity-center.notification.common.view :as common]
    [utils.datetime :as datetime]
    [utils.i18n :as i18n]
    utils.url))

(defn view
  [{:keys [customization-color notification extra-fn]}]
  (let [{:keys [timestamp read dapp-url dapp-icon-url]} notification]
    [common/swipeable
     {:left-button    common/swipe-button-read-or-unread
      :left-on-press  common/swipe-on-press-toggle-read
      :right-button   common/swipe-button-delete
      :right-on-press common/swipe-on-press-delete
      :extra-fn       extra-fn}
     [quo/activity-log
      {:title               (i18n/label :t/connected-to-dapp)
       :customization-color customization-color
       :icon                :i/dapps
       :timestamp           (datetime/timestamp->relative timestamp)
       :unread?             (not read)
       :context             [[quo/context-tag
                              {:type      :dapp
                               :size      24
                               :blur?     true
                               :dapp-logo dapp-icon-url
                               :dapp-name (utils.url/url-host dapp-url)}]
                             (i18n/label :t/via)
                             [quo/context-tag
                              {:type      :dapp
                               :size      24
                               :blur?     true
                               :dapp-logo (quo.resources/get-dapp :wallet-connect)
                               :dapp-name "WalletConnect"}]]}]]))
