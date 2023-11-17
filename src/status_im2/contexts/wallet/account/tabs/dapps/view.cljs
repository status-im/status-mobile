(ns status-im2.contexts.wallet.account.tabs.dapps.view
  (:require
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [status-im2.common.resources :as resources]
    [status-im2.contexts.wallet.account.tabs.dapps.style :as style]
    [status-im2.contexts.wallet.common.empty-tab.view :as empty-tab]
    [status-im2.contexts.wallet.common.temp :as temp]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn dapp-options
  []
  [quo/action-drawer
   [[{:icon                :i/browser
      :accessibility-label :visit-dapp
      :label               (i18n/label :t/visit-dapp)}
     {:icon                :i/disconnect
      :accessibility-label :disconnect-dapp
      :label               (i18n/label :t/disconnect-dapp)
      :add-divider?        true
      :danger?             true}]]])

(defn- view-internal
  [{:keys [theme]}]
  (let [dapps-list []]
    (if (empty? dapps-list)
      [empty-tab/view
       {:title       (i18n/label :t/no-dapps)
        :description (i18n/label :t/no-collectibles-description)
        :image       (resources/get-themed-image :no-dapps theme)}]
      [rn/view {:style style/dapps-container}
       [rn/flat-list
        {:data      dapps-list
         :style     (style/dapps-list theme)
         :render-fn (fn [item] [quo/dapp item])
         :separator [rn/view {:style (style/separator theme)}]}]])))

(def view (quo.theme/with-theme view-internal))
