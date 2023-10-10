(ns status-im2.contexts.wallet.account.tabs.view
  (:require
    [quo2.core :as quo]
    [react-native.core :as rn]
    [status-im2.contexts.wallet.common.temp :as temp]
    [utils.i18n :as i18n]
    [status-im2.contexts.wallet.account.tabs.about.view :as about]
    [status-im2.contexts.wallet.account.tabs.dapps.view :as dapps]
    [status-im2.contexts.wallet.common.empty-tab.view :as empty-tab]))

(defn view
  [{:keys [selected-tab]}]
  (case selected-tab
    :assets       [rn/flat-list
                   {:render-fn               quo/token-value
                    :data                    temp/tokens
                    :content-container-style {:padding-horizontal 8}}]
    :collectibles [empty-tab/view
                   {:title        (i18n/label :t/no-collectibles)
                    :description  (i18n/label :t/no-collectibles-description)
                    :placeholder? true}]
    :activity     [empty-tab/view
                   {:title        (i18n/label :t/no-activity)
                    :description  (i18n/label :t/empty-tab-description)
                    :placeholder? true}]
    :permissions  [empty-tab/view
                   {:title        (i18n/label :t/no-permissions)
                    :description  (i18n/label :t/no-collectibles-description)
                    :placeholder? true}]
    :dapps        [dapps/view]
    [about/view]))


