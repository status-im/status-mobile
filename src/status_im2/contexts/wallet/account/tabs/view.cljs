(ns status-im2.contexts.wallet.account.tabs.view
  (:require
    [quo2.core :as quo]
    [react-native.core :as rn]
    [status-im2.contexts.wallet.account.tabs.style :as style]
    [status-im2.contexts.wallet.common.temp :as temp]
    [utils.i18n :as i18n]
<<<<<<< HEAD
    [status-im2.contexts.wallet.account.tabs.about.view :as about]))
=======
    [status-im2.contexts.wallet.account.tabs.dapps.view :as dapps]))
>>>>>>> b39b6334c (feat: dapps tab)

(defn view
  [{:keys [selected-tab]}]
  (case selected-tab
    :assets       [rn/flat-list
                   {:render-fn               quo/token-value
                    :data                    temp/tokens
                    :content-container-style {:padding-horizontal 8}}]
    :collectibles [quo/empty-state
                   {:title           (i18n/label :t/no-collectibles)
                    :description     (i18n/label :t/no-collectibles-description)
                    :placeholder?    true
                    :container-style style/empty-container-style}]
    :activity     [quo/empty-state
                   {:title           (i18n/label :t/no-activity)
                    :description     (i18n/label :t/empty-tab-description)
                    :placeholder?    true
                    :container-style style/empty-container-style}]
    :permissions  [quo/empty-state
                   {:title           (i18n/label :t/no-permissions)
                    :description     (i18n/label :t/no-collectibles-description)
                    :placeholder?    true
                    :container-style style/empty-container-style}]
<<<<<<< HEAD
    :dapps        [quo/empty-state
                   {:title           (i18n/label :t/no-dapps)
                    :description     (i18n/label :t/no-collectibles-description)
                    :placeholder?    true
                    :container-style style/empty-container-style}]
    [about/view]))
=======
    :dapps        [dapps/view]
    [rn/view {:style style/wip}
     [quo/text "[WIP]"]]))
>>>>>>> b39b6334c (feat: dapps tab)
