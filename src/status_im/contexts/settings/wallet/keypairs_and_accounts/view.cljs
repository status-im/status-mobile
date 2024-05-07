(ns status-im.contexts.settings.wallet.keypairs-and-accounts.view
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [quo.theme]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im.constants :as constants]
            [status-im.contexts.settings.wallet.keypairs-and-accounts.actions.view :as actions]
            [status-im.contexts.settings.wallet.keypairs-and-accounts.style :as style]
            [utils.address :as utils]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn navigate-back
  []
  (rf/dispatch [:navigate-back]))

(defn- parse-accounts
  [given-accounts]
  (->> given-accounts
       (filter (fn [{:keys [path]}]
                 (not (string/starts-with? path constants/path-eip1581))))
       (map (fn [{:keys [customization-color emoji name address]}]
              {:account-props {:customization-color customization-color
                               :size                32
                               :emoji               emoji
                               :type                :default
                               :name                name
                               :address             address}
               :networks      []
               :state         :default
               :action        :none}))))

(defn on-options-press
  [{:keys [theme]
    :as   props}]
  (rf/dispatch [:show-bottom-sheet
                {:content (fn [] [actions/view props])
                 :theme   theme}]))

(defn- keypair
  [item index _
   {:keys [profile-picture compressed-key customization-color]}]
  (let [theme            (quo.theme/use-theme)
        accounts         (parse-accounts (:accounts item))
        default-keypair? (zero? index)
        details          {:full-name (:name item)
                          :address   (when default-keypair?
                                       (utils/get-shortened-compressed-key compressed-key))}
        on-press         (rn/use-callback
                          (fn []
                            (on-options-press
                             (merge {:theme theme
                                     :title (:full-name details)}
                                    (if default-keypair?
                                      {:type                :default-keypair
                                       :description         (:address details)
                                       :customization-color customization-color
                                       :profile-picture     profile-picture}
                                      {:type        :keypair
                                       :icon-avatar :i/seed}))))
                          [customization-color default-keypair? details profile-picture theme])]
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
      :details             details}]))

(defn view
  []
  (let [inset-top           (safe-area/get-top)
        compressed-key      (rf/sub [:profile/compressed-key])
        profile-picture     (rf/sub [:profile/image])
        customization-color (rf/sub [:profile/customization-color])
        keypairs            (rf/sub [:wallet/keypairs])]
    [quo/overlay
     {:type            :shell
      :container-style (style/page-wrapper inset-top)}
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
       {:data                    keypairs
        :render-fn               keypair
        :render-data             {:profile-picture     profile-picture
                                  :compressed-key      compressed-key
                                  :customization-color customization-color}
        :initial-num-to-render   1
        :content-container-style {:padding-bottom 60}}]]]))
