(ns status-im.contexts.wallet.account.tabs.about.view
  (:require
    [quo.core :as quo]
    [react-native.clipboard :as clipboard]
    [react-native.core :as rn]
    [status-im.contexts.profile.utils :as profile.utils]
    [status-im.contexts.shell.constants :as constants]
    [status-im.contexts.wallet.account.tabs.about.style :as style]
    [status-im.contexts.wallet.add-account.create-account.utils :as create-account.utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn chain-explorer-options
  [address network-details]
  (map (fn [{:keys [chain-id block-explorer-name]}]
         {:icon                :i/link
          :accessibility-label :view-on-block-explorer
          :label               (i18n/label :t/view-on-block-explorer
                                           {:block-explorer-name block-explorer-name})
          :right-icon          :i/external
          :on-press            #(rf/dispatch
                                 [:wallet/navigate-to-chain-explorer chain-id address])})
       network-details))

(defn about-options
  []
  (let [{:keys [address] :as account} (rf/sub [:wallet/current-viewing-account])
        network-details               (rf/sub [:wallet/network-details])
        share-title                   (str (:name account) " " (i18n/label :t/address))]
    [quo/action-drawer
     [(concat (chain-explorer-options address network-details)
              [{:icon                :i/copy
                :accessibility-label :copy-address
                :label               (i18n/label :t/copy-address)
                :on-press            (fn []
                                       (clipboard/set-string address)
                                       (rf/dispatch [:toasts/upsert
                                                     {:type :positive
                                                      :text (i18n/label :t/address-copied)}]))}
               {:icon                :i/qr-code
                :accessibility-label :show-address-qr
                :label               (i18n/label :t/show-address-qr)
                :on-press            #(rf/dispatch [:open-modal :screen/wallet.share-address
                                                    {:status :share}])}
               {:icon                :i/share
                :accessibility-label :share-address
                :label               (i18n/label :t/share-address)
                :on-press            (fn []
                                       (rf/dispatch [:hide-bottom-sheet])
                                       (js/setTimeout
                                        #(rf/dispatch [:wallet/share-account
                                                       {:title share-title :content address}])
                                        600))}])]]))

(defn view
  []
  (let [{:keys [customization-color] :as profile} (rf/sub [:profile/profile-with-image])
        {:keys [address path watch-only?]}        (rf/sub [:wallet/current-viewing-account])
        {keypair-name :name
         keypair-type :type
         keycards     :keycards}                  (rf/sub [:wallet/current-viewing-account-keypair])
        keypair-keycard?                          (boolean (seq keycards))
        origin-type                               (case keypair-type
                                                    :seed
                                                    :recovery-phrase

                                                    :key
                                                    :private-key

                                                    :default-keypair)]
    [rn/scroll-view
     {:style                   style/about-tab
      :content-container-style {:padding-bottom (+ constants/floating-shell-button-height 8)}}
     [quo/data-item
      {:subtitle-type   :default
       :right-icon      :i/options
       :card?           true
       :status          :default
       :size            :default
       :title           (if watch-only? (i18n/label :t/watched-address) (i18n/label :t/address))
       :custom-subtitle (fn [] [quo/address-text
                                {:address address
                                 :format  :long}])
       :container-style {:margin-bottom 12}
       :on-press        #(rf/dispatch [:show-bottom-sheet {:content about-options}])}]
     (when (not watch-only?)
       [quo/account-origin
        {:type                origin-type
         :stored              (if keypair-keycard? :on-keycard :on-device)
         :profile-picture     (profile.utils/photo profile)
         :customization-color customization-color
         :derivation-path     (create-account.utils/normalize-path path)
         :keypair-name        keypair-name}])]))
