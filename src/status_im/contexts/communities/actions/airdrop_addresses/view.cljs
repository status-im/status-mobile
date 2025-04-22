(ns status-im.contexts.communities.actions.airdrop-addresses.view
  (:require
    [quo.context]
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [status-im.common.not-implemented :as not-implemented]
    [status-im.config :as config]
    [status-im.contexts.communities.actions.airdrop-addresses.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- account-item
  [{:keys [address emoji] :as account}
   _ _
   [community-id airdrop-address can-edit-addresses?]]
  (let [airdrop-address? (= address airdrop-address)
        on-press
        (when-not airdrop-address?
          (fn []
            (if can-edit-addresses?
              (rf/dispatch
               [:communities/sign-shared-addresses
                {:community-id    community-id
                 :airdrop-address address
                 :on-success      (fn []
                                    (rf/dispatch
                                     [:dismiss-modal
                                      :screen/address-for-airdrop])
                                    (rf/dispatch
                                     [:hide-bottom-sheet]))}])
              (do
                (rf/dispatch [:communities/set-airdrop-address community-id address])
                (rf/dispatch [:hide-bottom-sheet])))))]
    [quo/account-item
     {:account-props account
      :emoji         emoji
      :state         (if airdrop-address? :selected :default)
      :on-press      on-press}]))

(defn view
  []
  (let [{id :community-id}        (quo.context/use-screen-params)
        {:keys [name logo color]} (rf/sub [:communities/for-context-tag id])
        accounts                  (rf/sub [:communities/accounts-to-reveal id])
        airdrop-address           (rf/sub [:communities/airdrop-address id])
        can-edit-addresses?       (rf/sub [:communities/can-edit-shared-addresses? id])
        go-back                   (rn/use-callback #(rf/dispatch [:dismiss-modal
                                                                  :screen/address-for-airdrop]))]
    [:<>
     (when can-edit-addresses?
       [quo/page-nav
        {:type      :no-title
         :icon-name :i/arrow-left
         :on-press  go-back}])

     (if can-edit-addresses?
       [quo/page-top
        {:title                     (i18n/label :t/airdrop-addresses)
         :title-accessibility-label :title-label
         :description               :context-tag
         :context-tag               {:type           :community
                                     :community-logo logo
                                     :community-name name}}]
       (when config/show-not-implemented-features?
         [quo/drawer-top
          {:type                :context-tag
           :context-tag-type    :community
           :title               (i18n/label :t/airdrop-addresses)
           :community-name      name
           :button-icon         :i/info
           :button-type         :grey
           :on-button-press     not-implemented/alert
           :community-logo      logo
           :customization-color color}]))

     [gesture/flat-list
      {:data                    accounts
       :render-fn               account-item
       :render-data             [id airdrop-address can-edit-addresses?]
       :content-container-style style/account-list-container
       :key-fn                  :address}]]))
