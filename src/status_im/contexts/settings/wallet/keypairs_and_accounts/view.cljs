(ns status-im.contexts.settings.wallet.keypairs-and-accounts.view
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im.common.not-implemented :as not-implemented]
            [status-im.constants :as constants]
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

(def keypair-container-style
  {:margin-horizontal 20
   :margin-vertical   8})

(defn- keypair
  [item index _
   {:keys [profile-picture compressed-key customization-color]}]
  (let [accounts (parse-accounts (:accounts item))]
    [quo/keypair
     {:blur?               false
      :status-indicator    false
      :stored              :on-device
      :action              :options
      :accounts            accounts
      :customization-color customization-color
      :container-style     keypair-container-style
      :profile-picture     (when (zero? index) profile-picture)
      :type                (if (zero? index) :default-keypair :other)
      :on-options-press    #(not-implemented/alert)
      :details             {:full-name (:name item)
                            :address   (when (zero? index)
                                         (utils/get-shortened-compressed-key compressed-key))}}]))

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
