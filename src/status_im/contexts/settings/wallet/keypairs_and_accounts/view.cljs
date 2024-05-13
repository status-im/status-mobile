(ns status-im.contexts.settings.wallet.keypairs-and-accounts.view
  (:require [quo.core :as quo]
            [quo.theme]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im.contexts.settings.wallet.keypairs-and-accounts.actions.view :as actions]
            [status-im.contexts.settings.wallet.keypairs-and-accounts.style :as style]
            [utils.address :as utils]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn navigate-back
  []
  (rf/dispatch [:navigate-back]))

(defn on-options-press
  [{:keys [theme]
    :as   props} keypair]
  (rf/dispatch [:show-bottom-sheet
                {:content (fn [] [actions/view props keypair])
                 :theme   theme}]))

(defn- keypair
  [{keypair-type :type
    :keys        [accounts name]
    :as          item}
   _ _
   {:keys [profile-picture compressed-key customization-color]}]
  (let [theme            (quo.theme/use-theme)
        default-keypair? (= keypair-type :profile)
        shortened-key    (when default-keypair?
                           (utils/get-shortened-compressed-key compressed-key))
        on-press         (rn/use-callback
                          (fn []
                            (on-options-press
                             (cond-> {:theme theme
                                      :blur? true
                                      :title name}
                               default-keypair?
                               (assoc :type                :default-keypair
                                      :description         shortened-key
                                      :customization-color customization-color
                                      :profile-picture     profile-picture)
                               (not default-keypair?)
                               (assoc :type        :keypair
                                      :icon-avatar :i/seed))
                             item))
                          [customization-color default-keypair? name
                           profile-picture shortened-key theme])]
    [quo/keypair
     {:blur?               false
      :status-indicator    false
      :stored              :on-device
      :action              :options
      :accounts            accounts
      :customization-color customization-color
      :container-style     style/keypair-container-style
      :profile-picture     (when default-keypair? profile-picture)
      :type                (if default-keypair? :default-keypair :other)
      :on-options-press    on-press
      :details             {:full-name name
                            :address   shortened-key}}]))

(defn view
  []
  (let [insets                (safe-area/get-insets)
        compressed-key        (rf/sub [:profile/compressed-key])
        profile-picture       (rf/sub [:profile/image])
        customization-color   (rf/sub [:profile/customization-color])
        quo-keypairs-accounts (rf/sub [:wallet/settings-keypairs-accounts])]
    [quo/overlay
     {:type            :shell
      :container-style (style/page-wrapper (:top insets))}
     [quo/page-nav
      {:key        :header
       :background :blur
       :icon-name  :i/arrow-left
       :on-press   navigate-back}]
     [rn/view {:style style/title-container}
      [quo/standard-title
       {:title               (i18n/label :t/keypairs-and-accounts)
        :accessibility-label :keypairs-and-accounts-header
        :customization-color customization-color}]]
     [rn/view {:style {:flex 1}}
      [rn/flat-list
       {:data                    quo-keypairs-accounts
        :render-fn               keypair
        :render-data             {:profile-picture     profile-picture
                                  :compressed-key      compressed-key
                                  :customization-color customization-color}
        :content-container-style (style/list-container (:bottom insets))}]]]))
