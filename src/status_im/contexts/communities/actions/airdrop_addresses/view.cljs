(ns status-im.contexts.communities.actions.airdrop-addresses.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.common.not-implemented :as not-implemented]
    [status-im.contexts.communities.actions.airdrop-addresses.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- render-item
  [item _ _ [airdrop-address community-id]]
  [quo/account-item
   {:account-props item
    :state         (when (= airdrop-address (:address item)) :selected)
    :on-press      (fn []
                     (rf/dispatch [:communities/update-airdrop-address-neww
                                   {:community-id community-id :address (:address item)}])
                     (rf/dispatch [:navigate-back]))
    :emoji         (:emoji item)}])

(defn view
  []
  (let [{id :community-id}          (rf/sub [:get-screen-params])
        {:keys [name images color]} (rf/sub [:communities/community id])
        accounts-to-reveal          (rf/sub [:communities/accounts-to-reveal-by-community-neww id])
        airdrop-address             (rf/sub [:communities/airdrop-address-by-community-neww id])]
    [:<>
     [quo/drawer-top
      {:type                :context-tag
       :context-tag-type    :community
       :title               (i18n/label :t/airdrop-addresses)
       :community-name      name
       :button-icon         :i/info
       :on-button-press     not-implemented/alert
       :community-logo      (get-in images [:thumbnail :uri])
       :customization-color color}]
     [rn/flat-list
      {:data                    accounts-to-reveal
       :render-fn               render-item
       :render-data             [airdrop-address id]
       :content-container-style style/account-list-container
       :key-fn                  :address}]]))
