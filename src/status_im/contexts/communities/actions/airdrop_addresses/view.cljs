(ns status-im.contexts.communities.actions.airdrop-addresses.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.common.not-implemented :as not-implemented]
    [status-im.contexts.communities.actions.airdrop-addresses.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- render-item
  [item]
  [quo/account-item
   {:account-props item
    :emoji         (:emoji item)}])

(defn- accounts-list
  [{:keys [accounts]}]
  [rn/view {:style style/account-list-container}
   (when (seq accounts)
     [rn/flat-list
      {:data      accounts
       :render-fn render-item
       :key-fn    :address}])])

(defn view
  []
  (let [{id :community-id}          (rf/sub [:get-screen-params])
        {:keys [name images color]} (rf/sub [:communities/community id])
        logo-uri                    (get-in images [:thumbnail :uri])
        accounts                    (rf/sub [:wallet/accounts-with-customization-color])]
    [:<>
     [quo/drawer-top
      {:type                :context-tag
       :title               (i18n/label :t/airdrop-addresses)
       :community-name      name
       :button-icon         :i/info
       :on-button-press     not-implemented/alert
       :community-logo      (get-in images [:thumbnail :uri])
       :customization-color color}]
     [accounts-list
      {:accounts       accounts
       :logo-uri       logo-uri
       :community-name name}]]))
