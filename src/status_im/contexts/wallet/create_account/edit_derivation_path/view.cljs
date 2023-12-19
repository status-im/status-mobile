(ns status-im.contexts.wallet.create-account.edit-derivation-path.view
  (:require
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.contexts.wallet.common.temp :as temp]
    [status-im.contexts.wallet.common.utils :as utils]
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
        derive-action      #(reset! state :choose)
        choose-action      #(reset! state :show)
        path-value         (reagent/atom (utils/get-formatted-derivation-path 3))
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
      [rn/view
       {:style (style/screen top)}
       [quo/page-nav
        {:background :blur
         :icon-name  :i/close
         :on-press   #(rf/dispatch [:navigate-back])}]
       [quo/text
        {:size   :heading-1
         :weight :semi-bold
         :style  style/header}
        (i18n/label :t/edit-derivation-path)]
       [rn/view {:style style/tag}
        [quo/context-tag
         {:type    :icon
          :size    24
          :icon    :i/placeholder
          :style   style/tag
          :context "Alisher Card"}]]
       [rn/view {:style style/temporal-placeholder}
        [quo/text "Dropdown input will be here"]
        [quo/text (i18n/label :t/path-format)]]
       [quo/input
        {:container-style style/input-container
         :value           @path-value
         :label           (i18n/label :t/derivation-path)
         :placeholder     (utils/get-formatted-derivation-path 3)
         :button          {:on-press reset-path-value
                           :text     (i18n/label :t/reset)}
         :on-change-text  handle-path-change}]

       (case @state
         :default
         [quo/bottom-actions
          {:theme            theme
           :actions          :1-action
           :button-one-label (i18n/label :t/reveal-address)
           :button-one-props {:type      :outline
                              :icon-left :i/keycard-card
                              :on-press  reveal-action}}]

         :empty
         [quo/bottom-actions
          {:theme            theme
           :actions          :1-action
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
                        (choose-action))} "Choose"]]
         nil)

       [rn/view {:style (style/save-button-container bottom)}
        [quo/bottom-actions
         {:theme            theme
          :actions          :1-action
          :button-one-label (i18n/label :t/save)
          :button-one-props {:type      :primary
                             :on-press  #(js/alert "Save!")
                             :disabled? true}}]]])))

(def view (quo.theme/with-theme view-internal))
