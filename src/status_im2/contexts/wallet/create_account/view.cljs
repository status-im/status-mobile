(ns status-im2.contexts.wallet.create-account.view
  (:require
    [quo2.theme :as quo.theme]
    [react-native.core :as rn]
    [quo2.core :as quo]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im2.contexts.wallet.common.temp :as temp]
    [utils.i18n :as i18n]
    [status-im2.contexts.wallet.create-account.style :as style]
    [utils.re-frame :as rf]))

(def diamond-emoji "\uD83D\uDC8E")

(defn- view-internal
  []
  (let [top                  (safe-area/get-top)
        bottom               (safe-area/get-bottom)
        account-color        (reagent/atom :blue)
        emoji                (reagent/atom diamond-emoji)
        {:keys [public-key]} (rf/sub [:profile/profile])
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
        {:color           :red
         :placeholder     "Type something here"
         :max-length      24
         :blur?           true
         :disabled?       false
         :default-value   "Account 2"
         :container-style style/title-input-container}]
       [rn/view
        {:style style/color-picker-container}
        [quo/text
         {:size   :paragraph-2
          :weight :medium
          :style  (style/color-label theme)}
         (i18n/label :t/colour)]
        [quo/color-picker
         {:selected  @account-color
          :on-change #(reset! account-color %)}]]
       [rn/view {:style (style/divider-line theme)}]
       [quo/category
        {:list-type :settings
         :label     (i18n/label :t/origin)
         :data      (temp/create-account-state display-name)}]
       [quo/slide-button
        {:track-text          (i18n/label :t/slide-create)
         :track-icon          :face-id
         :customization-color @account-color
         :on-complete         (fn []
                                (js/alert "Functionality not implemented"))
         :container-style     (style/slide-button-container bottom)}]])))

(def view (quo.theme/with-theme view-internal))
