(ns status-im.contexts.wallet.create-account.select-keypair.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.constants :as constants]
    [status-im.contexts.profile.utils :as profile.utils]
    [status-im.contexts.wallet.create-account.select-keypair.style :as style]
    [utils.address :as utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- keypair-options
  []
  [quo/action-drawer
   [[{:icon                :i/add
      :accessibility-label :generate-new-keypair
      :label               (i18n/label :t/generate-new-keypair)
      :on-press            #(rf/dispatch [:navigate-to :screen/wallet.backup-recovery-phrase])}
     {:icon                :i/seed
      :accessibility-label :import-using-phrase
      :label               (i18n/label :t/import-using-phrase)
      :add-divider?        true}
     {:icon                :i/keycard-card
      :accessibility-label :import-from-keycard
      :label               (i18n/label :t/import-from-keycard)}
     {:icon                :i/key
      :accessibility-label :import-private-key
      :label               (i18n/label :t/import-private-key)}]]])

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
               :networks      [{:network-name :ethereum :short-name "eth"}
                               {:network-name :optimism :short-name "opt"}
                               {:network-name :arbitrum :short-name "arb1"}]
               :state         :default
               :action        :none}))))

(defn- keypair
  [item index _ {:keys [profile-picture compressed-key]}]
  (let [main-account (first (:accounts item))
        color        (:customization-color main-account)
        accounts     (parse-accounts (:accounts item))]
    [quo/keypair
     {:customization-color color
      :profile-picture     (when (zero? index) profile-picture)
      :status-indicator    false
      :type                (if (zero? index) :default-keypair :other)
      :stored              :on-device
      :on-options-press    #(js/alert "Options pressed")
      :action              :selector
      :blur?               false
      :details             {:full-name (:name item)
                            :address   (when (zero? index)
                                         (utils/get-shortened-compressed-key compressed-key))}
      :accounts            accounts
      :default-selected?   (zero? index)
      :container-style     {:margin-horizontal 20
                            :margin-vertical   8}}]))
(defn view
  []
  (let [{:keys [compressed-key customization-color]} (rf/sub [:profile/profile])
        profile-with-image                           (rf/sub [:profile/profile-with-image])
        keypairs                                     (rf/sub [:wallet/keypairs])
        profile-picture                              (profile.utils/photo profile-with-image)]
    [rn/view {:style {:flex 1}}
     [quo/page-nav
      {:icon-name           :i/close
       :on-press            #(rf/dispatch [:navigate-back])
       :accessibility-label :top-bar}]
     [quo/page-top
      {:container-style   style/header-container
       :title             (i18n/label :t/keypairs)
       :title-right       :action
       :title-right-props {:icon                :i/add
                           :customization-color customization-color
                           :on-press            #(rf/dispatch
                                                  [:show-bottom-sheet {:content keypair-options}])}
       :description       :text
       :description-text  (i18n/label :t/keypairs-description)}]
     [rn/flat-list
      {:data                    keypairs
       :render-fn               keypair
       :render-data             {:profile-picture profile-picture
                                 :compressed-key  compressed-key}
       :content-container-style {:padding-bottom 60}}]
     [quo/bottom-actions
      {:actions          :one-action
       :button-one-label (i18n/label :t/confirm-account-origin)
       :button-one-props {:disabled?           true
                          :customization-color customization-color}
       :container-style  style/bottom-action-container}]]))
