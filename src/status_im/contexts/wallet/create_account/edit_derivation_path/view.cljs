(ns status-im.contexts.wallet.create-account.edit-derivation-path.view
  (:require
    [native-module.core :as native-module]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.contexts.wallet.common.temp :as temp]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.create-account.edit-derivation-path.path-format-sheet.view :as
     path-format-sheet]
    [status-im.contexts.wallet.create-account.edit-derivation-path.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- view-internal
  "States:
    default(filled)
    | -> (reveal-action) -> show
    | -> (clear-action) -> empty -> (derive-action) -> choose -> (choose-action) -> show"
  [{:keys [on-reset on-reveal]}]
  (let [top                (safe-area/get-top)
        bottom             (safe-area/get-bottom)
        state              (reagent/atom :default)
        reveal-action      (fn [_]
                             (reset! state :show)
                             (when on-reveal
                               (on-reveal)))
        clear-action       #(reset! state :empty)
        derive-action      #(js/alert "To be implemented")
        choose-action      #(reset! state :show)
        number-of-accounts (count (rf/sub [:wallet/accounts-without-watched-accounts]))
        asdf (rf/sub [:wallet/accounts-without-watched-accounts])
        path-value         (reagent/atom (utils/get-formatted-derivation-path number-of-accounts))
        handle-path-change (fn [v]
                             (reset! path-value v)
                             (when (empty? v)
                               (clear-action)))
        reset-path-value   (fn [_]
                             (reset! path-value "")
                             (clear-action)
                             (when on-reset
                               (on-reset)))]
    (fn [{:keys [theme]}]
      (let [{:keys [public-key address wallet-root-address] :as profile} (rf/sub [:profile/profile])
            account          (rf/sub [:wallet/current-viewing-account-address])
            primary-name           (first (rf/sub [:contacts/contact-two-names-by-identity
                                                   public-key]))
            profile-picture        (rf/sub [:profile/image])
            show-path-format-sheet #(rf/dispatch [:show-bottom-sheet {:content path-format-sheet/view}])]
        (println "ppp" asdf)
        [rn/view
         {:style (style/screen top)}
         [quo/page-nav
          {:background :blur
           :icon-name  :i/close
           :on-press   #(rf/dispatch [:navigate-back])}]
         [rn/view {:style {:padding-bottom 20}}
          [quo/text
           {:size   :heading-1
            :weight :semi-bold
            :style  style/header}
           (i18n/label :t/edit-derivation-path)]
          [rn/view {:style style/tag}
           [quo/context-tag
            {:size            24
             :profile-picture profile-picture
             :style           style/tag
             :full-name       primary-name}]]]
         [rn/pressable {:on-press show-path-format-sheet}
          [quo/input
           {:small?          true
            :editable        false
            :placeholder     (i18n/label :t/search-assets)
            :right-icon      {:on-press  show-path-format-sheet
                              :icon-name :i/dropdown
                              :style-fn  (fn []
                                           {:color   (colors/theme-colors colors/neutral-20
                                                                          colors/neutral-80
                                                                          theme)
                                            :color-2 (colors/theme-colors colors/neutral-100
                                                                          colors/white
                                                                          theme)})}
            :label           (i18n/label :t/path-format)
            :value           (i18n/label :t/default-format)
            :container-style {:margin-horizontal 20}}]]
         [quo/input
          {:container-style          style/input-container
           :value                    @path-value
           :on-focus                 #(reset! state :default)
           :show-soft-input-on-focus false
           :editable                 true
           :label                    (i18n/label :t/derivation-path)
           :placeholder              (utils/get-formatted-derivation-path 3)
           :button                   {:on-press reset-path-value
                                      :text     (i18n/label :t/reset)}
           :on-change-text           handle-path-change}]

         (case @state
           :default
           [quo/bottom-actions
            {:actions          :one-action
             :button-one-label (i18n/label :t/reveal-address)
             :button-one-props {:type      :outline
                                :icon-left :i/keycard-card
                                :on-press  reveal-action}}]

           :empty
           [quo/bottom-actions
            {:actions          :one-action
             :button-one-label (i18n/label :t/derive-addresses)
             :button-one-props {:type      :outline
                                :icon-left :i/keycard-card
                                :on-press  derive-action}}]

           :show
           [rn/view {:style style/revealed-address-container}
            [rn/view {:style (style/revealed-address theme)}
             [quo/text
              {:weight :monospace}
              temp/address]]
            [quo/info-message
             {:type  :success
              :icon  :i/done
              :style style/info}
             (i18n/label :t/address-activity)]]

           :choose
           [rn/view {:style style/temporal-placeholder}
            [quo/text "Dropdown input will be here"]
            [quo/button
             {:on-press (fn [_]
                          (reset! path-value (utils/get-formatted-derivation-path 1))
                          (choose-action))}
             "Choose"]]
           nil)

         [rn/view {:style (style/save-button-container bottom)}
          [quo/bottom-actions
           {:actions          :one-action
            :button-one-label (i18n/label :t/save)
            :button-one-props {:type      :primary
                               :on-press  #(js/alert "Save!")
                               :disabled? true}}]]
         (when-not (= @state :show)
           [quo/numbered-keyboard
            {:left-action :dot
             :delete-key? true
             :on-press    (fn [value]
                            (reset! path-value (str @path-value value))
                            )
             :on-delete   #(reset! path-value (subs @path-value 0 (dec (count @path-value))))}])]))))

(def view (quo.theme/with-theme view-internal))

;; 1. Need to ask how to check for address activity on status-go
;; 2. what does this list of addresses mean, for example I see selecting 2 addresses with activity what does that mean
