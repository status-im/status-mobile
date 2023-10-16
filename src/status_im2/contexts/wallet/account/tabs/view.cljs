(ns status-im2.contexts.wallet.account.tabs.view
  (:require
    [quo2.core :as quo]
    [react-native.core :as rn]
    [status-im2.contexts.wallet.account.tabs.about.view :as about]
    [status-im2.contexts.wallet.account.tabs.dapps.view :as dapps]
<<<<<<< HEAD
<<<<<<< HEAD
    [status-im2.contexts.wallet.common.empty-tab.view :as empty-tab]
    [status-im2.contexts.wallet.common.temp :as temp]
    [utils.i18n :as i18n]))
=======
    [status-im2.contexts.wallet.account.tabs.activity.view :as activity]
=======
    [status-im2.contexts.wallet.common.collectibles.view :as collectibles]
    [status-im2.contexts.wallet.common.activity.view :as activity]
>>>>>>> fc2057fa5 (wallet: acitivty tab)
    [status-im2.contexts.wallet.common.empty-tab.view :as empty-tab]))
>>>>>>> d118af92d (updates)

(defn view
  [{:keys [selected-tab]}]
  (case selected-tab
    :assets       [rn/flat-list
                   {:render-fn               quo/token-value
                    :data                    temp/tokens
                    :content-container-style {:padding-horizontal 8}}]
    :collectibles [collectibles/view]
    :activity     [activity/view]
    :permissions  [empty-tab/view
                   {:title        (i18n/label :t/no-permissions)
                    :description  (i18n/label :t/no-collectibles-description)
                    :placeholder? true}]
    :dapps        [dapps/view]
    [about/view]))
