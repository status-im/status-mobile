(ns status-im2.contexts.wallet.account.tabs.view
  (:require
    [quo.theme]
    [react-native.core :as rn]
    [status-im2.common.resources :as resources]
    [status-im2.contexts.wallet.account.tabs.about.view :as about]
    [status-im2.contexts.wallet.account.tabs.dapps.view :as dapps]
    [status-im2.contexts.wallet.common.activity-tab.view :as activity]
    [status-im2.contexts.wallet.common.collectibles-tab.view :as collectibles]
    [status-im2.contexts.wallet.common.empty-tab.view :as empty-tab]
    [status-im2.contexts.wallet.common.temp :as temp]
    [status-im2.contexts.wallet.common.token-value.view :as token-value]
    [utils.i18n :as i18n]))

(defn- view-internal
  [{:keys [selected-tab theme]}]
  (case selected-tab
    :assets       [rn/flat-list
                   {:render-fn               token-value/view
                    :data                    temp/tokens
                    :content-container-style {:padding-horizontal 8}}]
    :collectibles [collectibles/view]
    :activity     [activity/view]
    :permissions  [empty-tab/view
                   {:title       (i18n/label :t/no-permissions)
                    :description (i18n/label :t/no-collectibles-description)
                    :image       (resources/get-image
                                  (quo.theme/theme-value :no-permissions-light
                                                         :no-permissions-dark
                                                         theme))}]
    :dapps        [dapps/view]
    [about/view]))

(def view (quo.theme/with-theme view-internal))
