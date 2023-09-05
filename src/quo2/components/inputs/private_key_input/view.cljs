(ns quo2.components.inputs.private-key-input.view
  (:require
    [quo2.components.inputs.private-key-input.style :as style]
    [quo2.components.markdown.text :as quo-text]
    [react-native.core :as rn]
    [quo2.components.icon :as quo-icon]
    [quo2.components.buttons.button.view :as quo-button]
    [quo2.foundations.colors :as colors]
    [reagent.core :as reagent]
    [utils.i18n :as i18n]
    [clojure.string :as string]
    [react-native.clipboard :as clipboard]))

(defn private-key-input
  [{:keys [input-placeholder
           title-text]}]
  (let [text-input-value (reagent/atom "")]
    (fn []
      (let [invalid?           false
            show-paste-button? (string/blank? @text-input-value)]
        [:<>
         [rn/view
          {:style style/label-container}
          [quo-text/text
           {:style  style/label-pairing
            :weight :medium
            :size   :paragraph-2}
           title-text]]
         [rn/view {:style style/container-text-input}
          [rn/view {:style (style/text-input-container invalid?)}
           [rn/text-input
            {:style                  (style/text-input)
             :value                  @text-input-value
             :placeholder            input-placeholder
             :on-change-text         (fn [updated-text]
                                       (reset! text-input-value updated-text)
                                       (reagent/flush))
             :blur-on-submit         true
             :return-key-type        :done
             :accessibility-label    :enter-sync-code-input
             :auto-capitalize        :none
             :placeholder-text-color colors/white-opa-40
             :multiline              true}]
           (if show-paste-button?
             [quo-button/button
              {:on-press        (fn [_]
                                  (clipboard/get-string #(reset! text-input-value %)))
               :type            :outline
               :container-style style/button-paste
               :size            24}
              (i18n/label :t/paste)]

             [rn/pressable
              {:accessibility-label :input-right-icon
               :style               style/right-icon-touchable-area
               :on-press            (fn [_]
                                      (reset! text-input-value nil))}
              [quo-icon/icon :i/clear style/clear-icon]])]]]))))
