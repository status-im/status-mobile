(ns status-im2.contexts.wallet.create-account.view
  (:require
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im2.common.standard-authentication.standard-auth.view :as standard-auth]
    [status-im2.contexts.wallet.common.utils :as utils]
    [status-im2.contexts.wallet.create-account.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def diamond-emoji "\uD83D\uDC8E")

(defn keypair-string
  [full-name]
  (let [first-name (utils/get-first-name full-name)]
    (i18n/label :t/keypair-title {:name first-name})))

(defn get-keypair-data
  [name derivation-path]
  [{:title             (keypair-string name)
    :button-props      {:title (i18n/label :t/edit)}
    :left-icon         :i/placeholder
    :description       :text
    :description-props {:text (i18n/label :t/on-device)}}
   {:title             (i18n/label :t/derivation-path)
    :button-props      {:title (i18n/label :t/edit)}
    :left-icon         :i/derivated-path
    :description       :text
    :description-props {:text derivation-path}}])

(defn- view-internal
  []
  (let [top                  (safe-area/get-top)
        bottom               (safe-area/get-bottom)
        account-color        (reagent/atom :blue)
        emoji                (reagent/atom diamond-emoji)
        number-of-accounts   (count (rf/sub [:profile/wallet-accounts]))
        account-name         (reagent/atom (i18n/label :t/default-account-name
                                                       {:number (inc number-of-accounts)}))
        derivation-path      (reagent/atom (utils/get-derivation-path number-of-accounts))
        {:keys [public-key]} (rf/sub [:profile/profile])
        on-change-text       #(reset! account-name %)
        display-name         (first (rf/sub [:contacts/contact-two-names-by-identity public-key]))]
    (fn [{:keys [theme]}]
      [rn/view
       {:style {:flex       1
                :margin-top top}}
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
         :max-length          24
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
          :container-style  {:padding-horizontal 12
                             :padding-vertical   12}}]]
       [quo/divider-line]
       [quo/category
        {:list-type :settings
         :label     (i18n/label :t/origin)
         :data      (get-keypair-data display-name @derivation-path)}]
       [standard-auth/view
        {:size                :size-48
         :track-text          (i18n/label :t/slide-to-create-account)
         :customization-color @account-color
         :on-enter-password   (fn [entered-password]
                                (rf/dispatch [:wallet/derive-address-and-add-account
                                              entered-password
                                              {:emoji        @emoji
                                               :color        @account-color
                                               :path         @derivation-path
                                               :account-name @account-name}]))
         :biometric-auth?     false
         :auth-button-label   (i18n/label :t/confirm)
         :container-style     (style/slide-button-container bottom)}]])))

(def view (quo.theme/with-theme view-internal))
