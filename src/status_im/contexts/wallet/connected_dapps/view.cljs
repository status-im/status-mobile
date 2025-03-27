(ns status-im.contexts.wallet.connected-dapps.view
  (:require
    [quo.context]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.plus-button.view :as plus-button]
    [status-im.common.resources :as resources]
    [status-im.contexts.wallet.connected-dapps.disconnect-dapp.view :as disconnect-dapp]
    [status-im.contexts.wallet.connected-dapps.style :as style]
    [status-im.contexts.wallet.wallet-connect.utils.data-store :as data-store]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.string]))

(defn- on-disconnect
  [wallet-account {:keys [name topic]}]
  (rf/dispatch [:hide-bottom-sheet])
  (rf/dispatch
   [:wallet-connect/disconnect-dapp
    {:topic      topic
     :on-success (fn []
                   (rf/dispatch [:toasts/upsert
                                 {:id   :dapp-disconnect-success
                                  :type :positive
                                  :text (i18n/label :t/disconnect-dapp-success
                                                    {:dapp    name
                                                     :account (:name wallet-account)})}]))
     :on-fail    (fn []
                   (rf/dispatch [:toasts/upsert
                                 {:id   :dapp-disconnect-failure
                                  :type :negative
                                  :text (i18n/label :t/disconnect-dapp-fail
                                                    {:dapp    name
                                                     :account (:name wallet-account)})}]))}]))

(defn- on-dapp-disconnect-press
  [wallet-account dapp]
  (rf/dispatch [:show-bottom-sheet
                {:content (fn [] [disconnect-dapp/view
                                  {:customization-color (:color wallet-account)
                                   :dapp                dapp
                                   :on-disconnect       #(on-disconnect wallet-account dapp)}])}]))

(defn- account-details
  [{:keys [name emoji color]}]
  (let [theme (quo.context/use-theme)]
    [rn/view {:style style/account-details-wrapper}
     [quo/context-tag
      {:theme               theme
       :type                :account
       :size                24
       :account-name        name
       :emoji               emoji
       :customization-color color}]]))

(defn- header
  [{:keys [title wallet-account on-close on-add]}]
  (let [{:keys [color]} wallet-account]
    [:<>
     [rn/view {:style style/header-container}
      [quo/button
       {:icon-only?          true
        :type                :grey
        :background          :blur
        :size                32
        :accessibility-label :connected-dapps-close
        :on-press            on-close}
       :i/close]]
     [rn/view {:style style/header-wrapper}
      [quo/standard-title
       {:title               title
        :accessibility-label :connected-dapps
        :customization-color color
        :right               [plus-button/plus-button
                              {:on-press            on-add
                               :accessibility-label :connected-dapps-add
                               :customization-color color}]}]
      [rn/view {:flex 1}
       [account-details wallet-account]]]]))

(defn view
  []
  (let [{:keys [bottom]}                   (safe-area/get-insets)
        {:keys [color] :as wallet-account} (rf/sub [:wallet/current-viewing-account])
        customization-color                (rf/sub [:profile/customization-color])
        sessions                           (rf/sub
                                            [:wallet-connect/sessions-for-current-account-and-networks])
        theme                              (quo.context/use-theme)]
    [rn/view {:flex 1}
     [header
      {:title          (i18n/label :t/connected-dapps)
       :wallet-account wallet-account
       :on-close       #(rf/dispatch [:navigate-back])
       :on-add         #(rf/dispatch [:navigate-to :screen/wallet.scan-dapp])}]
     (if (empty? sessions)
       [quo/empty-state
        {:title           (i18n/label :t/no-dapps)
         :description     (i18n/label :t/no-dapps-description)
         :image           (resources/get-themed-image :no-dapps theme)
         :container-style style/empty-container-style}]
       [rn/view (style/dapps-container bottom)
        [rn/flat-list
         {:data                    sessions
          :always-bounce-vertical  false
          :content-container-style (style/dapps-list theme)
          :render-fn               (fn [{:keys [topic pairingTopic name url iconUrl]}]
                                     [quo/dapp
                                      {:dapp                {:avatar
                                                             (data-store/compute-dapp-icon-path
                                                              iconUrl
                                                              url)
                                                             :name
                                                             (data-store/compute-dapp-name
                                                              name
                                                              url)
                                                             :value url
                                                             :topic topic
                                                             :pairing-topic pairingTopic
                                                             :customization-color customization-color}
                                       :accessibility-label (str "dapp-" topic)
                                       :state               :default
                                       :action              :icon
                                       :blur?               false
                                       :customization-color color
                                       :right-component     (fn [dapp]
                                                              [rn/pressable
                                                               {:on-press (fn []
                                                                            (on-dapp-disconnect-press
                                                                             wallet-account
                                                                             dapp))}
                                                               [quo/icon :i/disconnect
                                                                {:color (colors/theme-colors
                                                                         colors/neutral-50
                                                                         colors/neutral-40
                                                                         theme)
                                                                 :accessibility-label :icon}]])}])
          :separator               [rn/view {:style (style/separator theme)}]}]])]))
