(ns status-im.contexts.wallet.create-account.edit-derivation-path.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.create-account.edit-derivation-path.path-format-sheet.view :as
     path-format-sheet]
    [status-im.contexts.wallet.create-account.edit-derivation-path.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(defn- view-internal
  "States:
    default(filled)
    | -> (reveal-action) -> show
    | -> (clear-action) -> empty -> (derive-action) -> choose -> (choose-action) -> show"
  [{:keys [on-reset]}]
  (let [top                (safe-area/get-top)
        bottom             (safe-area/get-bottom)
        input-focused?     (reagent/atom false)
        number-of-accounts (count (rf/sub [:wallet/accounts-without-watched-accounts]))
        path-value         (reagent/atom (utils/get-formatted-derivation-path number-of-accounts))
        handle-path-change (fn [v]
                             (reset! path-value v)
                             (when (empty? v)))
        reset-path-value   (fn [_]
                             (reset! path-value "")
                             (when on-reset
                               (on-reset)))
        input-ref          (reagent/atom nil)]
    (fn [{:keys [theme]}]
      (let [{:keys [public-key address]} (rf/sub [:profile/profile])
            {:keys [password]}           (rf/sub [:get-screen-params])
            primary-name                 (first (rf/sub [:contacts/contact-two-names-by-identity
                                                         public-key]))
            profile-picture              (rf/sub [:profile/image])
            show-path-format-sheet       #(rf/dispatch [:show-bottom-sheet
                                                        {:content path-format-sheet/view}])
            derivation-path              (rf/sub [:wallet/derivation-path])
            state                        (rf/sub [:wallet/derivation-path-state])
            navigate-back-handler        #(if @input-focused?
                                            (do
                                              (.blur ^js @input-ref)
                                              true)
                                            (rf/dispatch [:navigate-to :screen/wallet.create-account]))
            on-change-text               #(rf/dispatch
                                           [:wallet/get-derived-addresses
                                            {:password     (security/safe-unmask-data password)
                                             :paths        [(string/replace @path-value #"\s" "")]
                                             :derived-from address}])]
        (rn/use-mount (fn []
                        (rf/dispatch [:wallet/get-derived-addresses
                                      {:password     (security/safe-unmask-data password)
                                       :paths        [(string/replace @path-value #"\s" "")]
                                       :derived-from address}])
                        (rn/hw-back-add-listener navigate-back-handler)
                        #(rn/hw-back-remove-listener navigate-back-handler)))
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
          {:ref                      #(reset! input-ref %)
           :container-style          style/input-container
           :value                    @path-value
           :on-focus                 #(reset! input-focused? true)
           :on-blur                  #(reset! input-focused? false)
           :show-soft-input-on-focus false
           :editable                 true
           :label                    (i18n/label :t/derivation-path)
           :placeholder              (utils/get-formatted-derivation-path 3)
           :button                   {:on-press reset-path-value
                                      :text     (i18n/label :t/reset)}
           :on-change-text           handle-path-change}]
         [rn/view {:style style/revealed-address-container}
          [rn/view {:style (style/revealed-address state theme)}
           [quo/text
            {:weight :monospace}
            (:address derivation-path)]]
          [quo/info-message
           {:type  (case state
                     :has-activity :success
                     :no-activity  :warning
                     :default)
            :icon  (if (= state :scanning) :i/scanning :i/done)
            :style style/info}
           (i18n/label (case state
                         :has-activity :t/address-activity
                         :no-activity  :t/address-no-activity
                         :t/scanning))]]
         [rn/view {:style (style/save-button-container bottom)}
          [quo/bottom-actions
           {:actions          :one-action
            :button-one-label (i18n/label :t/save)
            :button-one-props {:type      :primary
                               :on-press  #(js/alert "Save!")
                               :disabled? true}}]]
         (when @input-focused?
           [quo/numbered-keyboard
            {:left-action     :dot
             :delete-key?     true
             :container-style (style/keyboard bottom)
             :on-press        (fn [value]
                                (reset! path-value (str @path-value value))
                                (on-change-text))
             :on-delete       (fn []
                                (reset! path-value (subs @path-value 0 (dec (count @path-value))))
                                (on-change-text))}])]))))

(def view (quo.theme/with-theme view-internal))
