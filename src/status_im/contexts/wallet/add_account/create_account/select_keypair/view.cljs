(ns status-im.contexts.wallet.add-account.create-account.select-keypair.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.add-account.create-account.select-keypair.style :as style]
    [status-im.feature-flags :as ff]
    [utils.address :as utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- backup-recovery-phrase-success
  [masked-seed-phrase]
  (rf/dispatch [:navigate-to :screen/confirm-backup
                {:masked-seed-phrase masked-seed-phrase
                 :on-success         #(rf/dispatch [:navigate-to :screen/wallet.keypair-name
                                                    {:workflow :new-keypair}])}]))

(defn- keypair-options
  []
  [quo/action-drawer
   [[{:icon                :i/add
      :accessibility-label :generate-new-keypair
      :label               (i18n/label :t/generate-new-keypair)
      :on-press            #(rf/dispatch [:navigate-to :screen/backup-recovery-phrase
                                          {:on-success backup-recovery-phrase-success}])}
     {:icon                :i/seed
      :accessibility-label :import-using-phrase
      :label               (i18n/label :t/import-using-phrase)
      :add-divider?        true
      :on-press            (fn []
                             (rf/dispatch [:navigate-to :screen/use-recovery-phrase
                                           {:on-success (fn [{:keys [key-uid phrase on-error]}]
                                                          (rf/dispatch [:wallet/seed-phrase-validated
                                                                        phrase key-uid on-error]))}]))}
     (when (ff/enabled? ::ff/wallet.import-private-key)
       {:icon                :i/key
        :accessibility-label :import-private-key
        :label               (i18n/label :t/import-private-key)
        :on-press            #(rf/dispatch [:navigate-to :screen/wallet.import-private-key])})]]])

(defn- parse-accounts
  [given-accounts]
  (->> given-accounts
       (filter (fn [{:keys [path]}]
                 (not (string/starts-with? path constants/path-eip1581))))
       (map (fn [{:keys [color emoji name address]}]
              {:account-props {:customization-color color
                               :size                32
                               :emoji               emoji
                               :type                :default
                               :name                name
                               :address             address}
               :networks      [{:network-name :ethereum :short-name "eth"}
                               {:network-name :optimism :short-name "oeth"}
                               {:network-name :arbitrum :short-name "arb1"}]
               :state         :default
               :action        :none}))))

(defn- keypair
  [item _ _
   {:keys [profile-picture compressed-key selected-key-uid set-selected-key-uid customization-color]}]
  (let [profile-keypair? (= (:type item) :profile)
        keycard?         (boolean (seq (:keycards item)))
        accounts         (parse-accounts (:accounts item))]
    [quo/keypair
     {:customization-color customization-color
      :profile-picture     (when profile-keypair? profile-picture)
      :status-indicator    false
      :type                (if profile-keypair? :default-keypair :other)
      :stored              (if keycard? :on-keycard :on-device)
      :on-options-press    #(js/alert "Options pressed")
      :action              :selector
      :blur?               false
      :details             {:full-name (:name item)
                            :address   (when profile-keypair?
                                         (utils/get-shortened-compressed-key compressed-key))}
      :on-press            #(set-selected-key-uid (:key-uid item))
      :accounts            accounts
      :selected?           (= selected-key-uid (:key-uid item))
      :container-style     {:margin-horizontal 20
                            :margin-vertical   8}}]))
(defn view
  []
  (let [compressed-key                          (rf/sub [:profile/compressed-key])
        customization-color                     (rf/sub [:profile/customization-color])
        keypairs                                (rf/sub [:wallet/fully-operable-keypairs-list])
        selected-keypair                        (rf/sub [:wallet/selected-keypair-uid])
        profile-picture                         (rf/sub [:profile/image])
        [selected-key-uid set-selected-key-uid] (rn/use-state selected-keypair)]
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
       :render-data             {:profile-picture      profile-picture
                                 :compressed-key       compressed-key
                                 :selected-key-uid     selected-key-uid
                                 :set-selected-key-uid set-selected-key-uid
                                 :customization-color  customization-color}
       :initial-num-to-render   1
       :content-container-style {:padding-bottom 60}}]
     [quo/bottom-actions
      {:actions          :one-action
       :button-one-label (i18n/label :t/confirm-account-origin)
       :button-one-props {:customization-color customization-color
                          :on-press            #(rf/dispatch [:wallet/confirm-account-origin
                                                              selected-key-uid])}
       :container-style  style/bottom-action-container}]]))
