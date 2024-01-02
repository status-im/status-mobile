(ns status-im.contexts.wallet.create-account.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.common.emoji-picker.utils :as emoji-picker.utils]
    [status-im.common.standard-authentication.core :as standard-auth]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.create-account.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.responsiveness :refer [iphone-11-Pro-20-pixel-from-width]]
    [utils.string]))

(defn keypair-string
  [full-name]
  (let [first-name (utils/get-first-name full-name)]
    (i18n/label :t/keypair-title {:name first-name})))

(defn get-keypair-data
  [name derivation-path account-color]
  [{:title             (keypair-string name)
    :image             :avatar
    :image-props       {:full-name           (utils.string/get-initials name 1)
                        :size                :xxs
                        :customization-color account-color}
    :action            :button
    :action-props      {:on-press    #(rf/dispatch [:navigate-to :wallet-select-keypair])
                        :button-text (i18n/label :t/edit)
                        :alignment   :flex-start}
    :description       :text
    :description-props {:text (i18n/label :t/on-device)}}
   {:title             (i18n/label :t/derivation-path)
    :image             :icon
    :image-props       :i/derivated-path
    :action            :button
    :action-props      {:on-press    #(js/alert "Button pressed!")
                        :button-text (i18n/label :t/edit)
                        :icon-left   :i/placeholder
                        :alignment   :flex-start}
    :description       :text
    :description-props {:text (string/replace derivation-path #"/" " / ")}}])

(defn- view-internal
  []
  (let [top                   (safe-area/get-top)
        bottom                (safe-area/get-bottom)
        account-color         (reagent/atom (rand-nth colors/account-colors))
        emoji                 (reagent/atom (emoji-picker.utils/random-emoji))
        number-of-accounts    (count (rf/sub [:wallet/accounts]))
        account-name          (reagent/atom (i18n/label :t/default-account-name
                                                        {:number (inc number-of-accounts)}))
        derivation-path       (reagent/atom (utils/get-derivation-path number-of-accounts))
        {:keys [public-key]}  (rf/sub [:profile/profile])
        on-change-text        #(reset! account-name %)
        [primary-name _]      (first (rf/sub [:contacts/contact-two-names-by-identity public-key]))
        {window-width :width} (rn/get-window)]
    (fn [{:keys [theme]}]
      [rn/view {:style {:flex 1}}
       [quo/page-nav
        {:type       :no-title
         :background :blur
         :right-side [{:icon-name :i/info}]
         :icon-name  :i/close
         :on-press   #(rf/dispatch [:navigate-back])}]
       [quo/gradient-cover
        {:customization-color @account-color
         :container-style     (style/gradient-cover-container top)}]
       [rn/view
        {:style style/account-avatar-container}
        [quo/account-avatar
         {:customization-color @account-color
          :size                80
          :emoji               @emoji
          :type                :default}]
        [quo/button
         {:size            32
          :type            :grey
          :background      :photo
          :icon-only?      true
          :on-press        #(rf/dispatch [:emoji-picker/open
                                          {:on-select (fn [selected-emoji]
                                                        (reset! emoji selected-emoji))}])
          :container-style style/reaction-button-container} :i/reaction]]
       [quo/title-input
        {:customization-color @account-color
         :placeholder         "Type something here"
         :on-change-text      on-change-text
         :max-length          constants/wallet-account-name-max-length
         :blur?               true
         :disabled?           false
         :default-value       @account-name
         :container-style     style/title-input-container}]
       [quo/divider-line]
       [rn/view
        {:style style/color-picker-container}
        [quo/text
         {:size   :paragraph-2
          :weight :medium
          :style  (style/color-label theme)}
         (i18n/label :t/colour)]
        [quo/color-picker
         {:default-selected @account-color
          :on-change        #(reset! account-color %)
          :container-style  {:padding-vertical 12
                             :padding-left     (iphone-11-Pro-20-pixel-from-width window-width)}}]]
       [quo/divider-line]
       [quo/category
        {:list-type :settings
         :label     (i18n/label :t/origin)
         :data      (get-keypair-data primary-name @derivation-path @account-color)}]
       [standard-auth/slide-button
        {:size                :size-48
         :track-text          (i18n/label :t/slide-to-create-account)
         :customization-color @account-color
         :on-auth-success     (fn [entered-password]
                                (prn entered-password)
                                (rf/dispatch [:wallet/derive-address-and-add-account
                                              {:sha3-pwd     entered-password
                                               :emoji        @emoji
                                               :color        @account-color
                                               :path         @derivation-path
                                               :account-name @account-name}]))
         :auth-button-label   (i18n/label :t/confirm)
         :container-style     (style/slide-button-container bottom)}]])))

(def view (quo.theme/with-theme view-internal))
