(ns status-im.contexts.wallet.add-account.create-account.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.common.emoji-picker.utils :as emoji-picker.utils]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.common.standard-authentication.core :as standard-auth]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.add-account.create-account.style :as style]
    [status-im.contexts.wallet.common.utils :as common.utils]
    [status-im.contexts.wallet.sheets.account-origin.view :as account-origin]
    [status-im.feature-flags :as ff]
    [status-im.setup.hot-reload :as hot-reload]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.responsiveness :as responsiveness]
    [utils.string]))

(defn- get-keypair-data
  [{:keys [title primary-keypair? new-keypair? derivation-path customization-color keycard?]}]
  [{:title             title
    :image             (if primary-keypair? :avatar :icon)
    :image-props       (if primary-keypair?
                         {:full-name           (utils.string/get-initials title 1)
                          :size                :xxs
                          :customization-color customization-color}
                         :i/seed)
    :action            (when-not new-keypair? :button)
    :action-props      {:on-press    #(rf/dispatch [:navigate-to :screen/wallet.select-keypair])
                        :button-text (i18n/label :t/edit)
                        :alignment   :flex-start}
    :description       :text
    :description-props {:text (i18n/label (if keycard? :on-keycard :on-device))}}
   (when-not (string/blank? derivation-path)
     (let [formatted-path  (string/replace derivation-path #"/" " / ")
           on-auth-success (fn [password]
                             (rf/dispatch [:navigate-to
                                           :screen/wallet.edit-derivation-path
                                           {:password                password
                                            :current-derivation-path formatted-path}]))]
       {:title             (i18n/label :t/derivation-path)
        :image             :icon
        :image-props       :i/derivated-path
        :action            (if (ff/enabled? ::ff/wallet.edit-derivation-path) :button :none)
        :action-props      {:on-press    #(rf/dispatch [:standard-auth/authorize
                                                        {:on-auth-success   on-auth-success
                                                         :auth-button-label (i18n/label :t/continue)}])
                            :button-text (i18n/label :t/edit)
                            :icon-left   :i/face-id
                            :alignment   :flex-start}
        :description       :text
        :description-props {:text formatted-path}}))])

(defn- avatar
  [{:keys [account-color emoji on-select-emoji]}]
  [rn/view {:style style/account-avatar-container}
   [quo/account-avatar
    {:customization-color account-color
     :size                80
     :emoji               emoji
     :type                :default}]
   [quo/button
    {:size            32
     :type            :grey
     :background      :photo
     :icon-only?      true
     :on-press        #(rf/dispatch [:emoji-picker/open {:on-select on-select-emoji}])
     :container-style style/reaction-button-container}
    :i/edit]])

(defn- input
  [_]
  (let [placeholder (i18n/label :t/default-account-placeholder)]
    (fn [{:keys [account-color account-name on-change-text error]}]
      [rn/view
       [quo/title-input
        {:customization-color account-color
         :placeholder         placeholder
         :on-change-text      on-change-text
         :max-length          constants/wallet-account-name-max-length
         :blur?               true
         :disabled?           false
         :default-value       account-name
         :container-style     (style/title-input-container error)}]
       (when error
         [quo/info-message
          {:status          :error
           :size            :default
           :icon            :i/info
           :container-style {:margin-left   20
                             :margin-bottom 16}}
          (case error
            :emoji             (i18n/label :t/key-name-error-emoji)
            :special-character (i18n/label :t/key-name-error-special-char)
            :existing-name     (i18n/label :t/name-must-differ-error)
            :emoji-and-color   (i18n/label :t/emoji-and-colors-unique-error)
            nil)])])))

(defn- color-picker
  [_]
  (let [{window-width :width} (rn/get-window)
        color-picker-style    {:padding-vertical 12
                               :padding-left     (responsiveness/iphone-11-Pro-20-pixel-from-width
                                                  window-width)}]
    (fn [{:keys [account-color set-account-color]}]
      [:<>
       [quo/divider-line]
       (let [theme (quo.theme/use-theme)]
         [rn/view {:style style/color-picker-container}
          [quo/text
           {:size   :paragraph-2
            :weight :medium
            :style  (style/color-label theme)}
           (i18n/label :t/colour)]
          [quo/color-picker
           {:default-selected account-color
            :on-change        set-account-color
            :container-style  color-picker-style}]])])))

(defn- new-account-origin
  [{:keys [keypair-title derivation-path customization-color keycard?]}]
  (let [{keypair-name :name} (rf/sub [:wallet/selected-keypair])
        primary?             (rf/sub [:wallet/selected-primary-keypair?])
        keypair-name         (or keypair-title
                                 (if primary?
                                   (i18n/label :t/keypair-title {:name keypair-name})
                                   keypair-name))]
    [:<>
     [quo/divider-line]
     [quo/category
      {:list-type :settings
       :label     (i18n/label :t/origin)
       :data      (get-keypair-data {:keycard?            keycard?
                                     :primary-keypair?    primary?
                                     :title               keypair-name
                                     :derivation-path     derivation-path
                                     :customization-color customization-color})}]]))

(defn- floating-button
  [_ & _]
  (let [top    (safe-area/get-top)
        bottom (safe-area/get-bottom)
        header [quo/page-nav
                {:type       :no-title
                 :background :blur
                 :right-side [{:icon-name :i/info
                               :on-press  #(rf/dispatch [:show-bottom-sheet
                                                         {:content account-origin/view}])}]
                 :icon-name  :i/close
                 :on-press   #(rf/dispatch [:navigate-back])}]]
    (fn [{:keys [slide-button-props account-color]} & children]
      (into
       [floating-button-page/view
        {:gradient-cover?          true
         :footer-container-padding 0
         :header-container-style   {:padding-top top}
         :customization-color      account-color
         :header                   header
         :footer                   [standard-auth/slide-button
                                    (assoc slide-button-props
                                           :size                :size-48
                                           :track-text          (i18n/label :t/slide-to-create-account)
                                           :customization-color account-color
                                           :auth-button-label   (i18n/label :t/confirm)
                                           :container-style     (style/slide-button-container bottom))]}]
       children))))

(defn- on-auth-success-mnemonic
  [password account-preferences]
  (rf/dispatch
   [:wallet/import-mnemonic-and-create-keypair-with-account
    {:password            password
     :account-preferences account-preferences}]))

(defn- on-auth-success-import-private-key
  [password account-preferences]
  (rf/dispatch
   [:wallet/import-private-key-and-create-keypair-with-account
    {:password            password
     :account-preferences account-preferences}]))

(defn add-new-keypair-variant
  [{{:keys [account-name account-color emoji]} :state}]
  (fn [{:keys [on-change-text set-account-color set-emoji customization-color keypair-name error]}]
    (let [{:keys [new-account-data workflow]} (rf/sub [:wallet/create-account-new-keypair])
          derivation-path                     (when (not= workflow
                                                          :workflow/new-keypair.import-private-key)
                                                constants/path-default-wallet)]
      [floating-button
       {:account-color      @account-color
        :slide-button-props {:on-auth-success (fn on-auth-success [password]
                                                (let [account-preferences {:account-name @account-name
                                                                           :color        @account-color
                                                                           :emoji        @emoji}]
                                                  (if (= workflow
                                                         :workflow/new-keypair.import-private-key)
                                                    (on-auth-success-import-private-key
                                                     password
                                                     account-preferences)
                                                    (on-auth-success-mnemonic
                                                     password
                                                     account-preferences))))
                             :disabled?       (empty? @account-name)
                             :dependencies    [new-account-data]}}
       [avatar
        {:account-color   @account-color
         :emoji           @emoji
         :on-select-emoji set-emoji}]
       [input
        {:account-color  @account-color
         :account-name   @account-name
         :on-change-text on-change-text
         :error          error}]
       [color-picker
        {:account-color     @account-color
         :set-account-color set-account-color}]
       [new-account-origin
        {:derivation-path     derivation-path
         :customization-color customization-color
         :keypair-title       keypair-name}]])))

(defn derive-account-variant
  [{{:keys [account-name account-color emoji]} :state}]
  (let [derivation-path     (reagent/atom "")
        set-derivation-path #(reset! derivation-path %)]
    (fn [{:keys [on-change-text set-account-color set-emoji customization-color error]}]
      (let [{:keys [derived-from
                    key-uid keycards]} (rf/sub [:wallet/selected-keypair])
            keycard?                   (boolean (seq keycards))
            on-auth-success            (rn/use-callback
                                        (fn [password]
                                          (let [preferences {:account-name @account-name
                                                             :color        @account-color
                                                             :emoji        @emoji}]
                                            (rf/dispatch
                                             [:wallet/derive-address-and-add-account
                                              {:password             password
                                               :derived-from-address derived-from
                                               :key-uid              key-uid
                                               :derivation-path      @derivation-path
                                               :account-preferences  preferences}])))
                                        [derived-from])
            on-complete                (rn/use-callback
                                        (fn []
                                          (let [preferences {:account-name @account-name
                                                             :color        @account-color
                                                             :emoji        @emoji}]
                                            (rf/dispatch
                                             [:standard-auth/authorize-with-keycard
                                              {:on-complete
                                               #(rf/dispatch
                                                 [:keycard/connect-derive-address-and-add-account
                                                  {:pin                  %
                                                   :derived-from-address derived-from
                                                   :key-uid              key-uid
                                                   :derivation-path      @derivation-path
                                                   :account-preferences  preferences}])}])))
                                        [derived-from])]
        (rn/use-effect
         #(rf/dispatch
           [:wallet/next-derivation-path
            {:on-success  set-derivation-path
             :keypair-uid key-uid}])
         [key-uid])

        [floating-button
         {:account-color      @account-color
          :slide-button-props {:on-auth-success (when-not keycard? on-auth-success)
                               :on-complete
                               (when keycard? on-complete)
                               :disabled?
                               (or (empty? @account-name)
                                   (string/blank? @derivation-path)
                                   (some? error))}}
         [avatar
          {:account-color   @account-color
           :emoji           @emoji
           :on-select-emoji set-emoji}]
         [input
          {:account-color  @account-color
           :account-name   @account-name
           :on-change-text on-change-text
           :error          error}]
         [color-picker
          {:account-color     @account-color
           :set-account-color set-account-color}]
         [new-account-origin
          {:keycard?            keycard?
           :derivation-path     @derivation-path
           :customization-color customization-color}]]))))

(defn view
  []
  (let [account-name           (reagent/atom "")
        account-color          (reagent/atom (rand-nth colors/account-colors))
        emoji                  (reagent/atom (emoji-picker.utils/random-emoji))
        account-name-error     (reagent/atom nil)
        emoji-and-color-error? (reagent/atom false)
        state                  {:account-name           account-name
                                :account-color          account-color
                                :emoji                  emoji
                                :account-name-error     account-name-error
                                :emoji-and-color-error? emoji-and-color-error?}]
    (fn []
      (let [customization-color             (rf/sub [:profile/customization-color])
            ;; Having a keypair means the user is importing it or creating it.
            {:keys [keypair-name workflow]} (rf/sub [:wallet/create-account-new-keypair])
            accounts-names                  (rf/sub [:wallet/accounts-names])
            accounts-emojis-and-colors      (rf/sub [:wallet/accounts-emojis-and-colors])
            on-change-text                  (rn/use-callback
                                             (fn [new-text]
                                               (reset! account-name new-text)
                                               (reset! account-name-error
                                                 (common.utils/get-account-name-error new-text
                                                                                      accounts-names)))
                                             [accounts-names accounts-emojis-and-colors])
            check-emoji-and-color-error     (fn [emoji color]
                                              (let [repeated? (accounts-emojis-and-colors [emoji color])]
                                                (reset! emoji-and-color-error?
                                                  (when repeated? :emoji-and-color))))
            set-account-color               (rn/use-callback
                                             (fn [new-color]
                                               (reset! account-color new-color)
                                               (check-emoji-and-color-error @emoji new-color))
                                             [accounts-emojis-and-colors @emoji])
            set-emoji                       (rn/use-callback
                                             (fn [new-emoji]
                                               (reset! emoji new-emoji)
                                               (check-emoji-and-color-error new-emoji @account-color))
                                             [accounts-emojis-and-colors @account-color])
            error                           (or @account-name-error @emoji-and-color-error?)]

        (rn/use-mount #(check-emoji-and-color-error @emoji @account-color))
        (hot-reload/use-safe-unmount #(rf/dispatch [:wallet/clear-create-account]))

        (if keypair-name
          [add-new-keypair-variant
           {:state               state
            :customization-color customization-color
            :on-change-text      on-change-text
            :set-account-color   set-account-color
            :set-emoji           set-emoji

            :keypair-name        keypair-name
            :error               error
            :workflow            workflow}]
          [derive-account-variant
           {:state               state
            :customization-color customization-color
            :on-change-text      on-change-text
            :set-account-color   set-account-color
            :set-emoji           set-emoji
            :error               error}])))))
