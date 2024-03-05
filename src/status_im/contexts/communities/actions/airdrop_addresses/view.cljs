(ns status-im.contexts.communities.actions.airdrop-addresses.view
  (:require
    [quo.core :as quo]
    [react-native.gesture :as gesture]
    [status-im.common.not-implemented :as not-implemented]
    [status-im.contexts.communities.actions.airdrop-addresses.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- render-item
  [item _ _ [airdrop-address community-id]]
  [quo/account-item
   {:account-props item
    :state         (if (= airdrop-address (:address item))
                     :selected
                     :default)
    :on-press      (fn []
                     (rf/dispatch [:communities/set-airdrop-address (:address item) community-id])
                     (rf/dispatch [:hide-bottom-sheet]))
    :emoji         (:emoji item)}])

(defn view
  []
  (let [{id :community-id}          (rf/sub [:get-screen-params])
        {:keys [name images color]} (rf/sub [:communities/community id])
        selected-accounts           (rf/sub [:communities/selected-permission-accounts id])
        airdrop-address             (rf/sub [:communities/airdrop-address id])]
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
     [gesture/flat-list
      {:data                    selected-accounts
       :render-fn               render-item
       :render-data             [airdrop-address id]
       :content-container-style style/account-list-container
       :key-fn                  :address}]]))
