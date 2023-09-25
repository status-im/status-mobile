(ns quo2.components.inputs.address-input.view
  (:require [react-native.core :as rn]
            [react-native.clipboard :as clipboard]
            [quo2.theme :as quo.theme]
            [quo2.foundations.colors :as colors]
            [quo2.components.icon :as icon]
            [quo2.components.buttons.button.view :as button]
            [quo2.components.inputs.address-input.style :as style]
            [utils.i18n :as i18n]
            [reagent.core :as reagent]
            [react-native.platform :as platform]))

(defn- icon-color
  [blur? theme]
  (if blur?
    (colors/theme-colors colors/neutral-80-opa-30 colors/white-opa-10 theme)
    (colors/theme-colors colors/neutral-40 colors/neutral-60 theme)))

(defn- clear-button
  [{:keys [on-press blur? theme]}]
  [rn/touchable-opacity
   {:accessibility-label :clear-button
    :style               style/clear-icon-container
    :on-press            on-press}
   [icon/icon :i/clear
    {:color (icon-color blur? theme)
     :size  20}]])

(defn- loading-icon
  [blur? theme]
  [rn/view {:style style/clear-icon-container}
   [icon/icon :i/loading
    {:color (icon-color blur? theme)
     :size  20}]])

(defn- positive-state-icon
  [theme]
  [rn/view {:style style/clear-icon-container}
   [icon/icon :i/positive-state
    {:color (colors/theme-colors (colors/custom-color :success 50)
                                 (colors/custom-color :success 60)
                                 theme)
     :size  20}]])

(defn- get-placeholder-text-color
  [status theme blur?]
  (cond
    (and (= status :default) blur?)
    (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-30 theme)
    (and (= status :default) (not blur?))
    (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)
    (and (not= status :default) blur?)
    (colors/theme-colors colors/neutral-80-opa-20 colors/white-opa-20 theme)
    (and (not= status :default) (not blur?))
    (colors/theme-colors colors/neutral-30 colors/neutral-60 theme)))

(defn- f-address-input-internal
  []
  (let [status   (reagent/atom :default)
        value    (reagent/atom "")
        focused? (atom false)]
    (fn [{:keys [scanned-value theme blur? on-change-text on-blur on-focus on-clear on-scan on-detect-ens
                 ens-regex
                 valid-ens?]}]
      (let [on-change              (fn [text]
                                     (let [ens? (boolean (re-matches ens-regex text))]
                                       (if (> (count text) 0)
                                         (reset! status :typing)
                                         (reset! status :active))
                                       (reset! value text)
                                       (when on-change-text
                                         (on-change-text text))
                                       (when (and ens? on-detect-ens)
                                         (reset! status :loading)
                                         (on-detect-ens text))))
            on-paste               (fn []
                                     (clipboard/get-string
                                      (fn [clipboard]
                                        (when-not (empty? clipboard)
                                          (on-change clipboard)
                                          (reset! value clipboard)))))
            on-clear               (fn []
                                     (reset! value "")
                                     (reset! status (if @focused? :active :default))
                                     (when on-clear
                                       (on-clear)))
            on-scan                #(when on-scan
                                      (on-scan))
            on-focus               (fn []
                                     (when (= (count @value) 0)
                                       (reset! status :active))
                                     (reset! focused? true)
                                     (when on-focus (on-focus)))
            on-blur                (fn []
                                     (when (= @status :active)
                                       (reset! status :default))
                                     (reset! focused? false)
                                     (when on-blur (on-blur)))
            placeholder-text-color (get-placeholder-text-color @status theme blur?)]
        (rn/use-effect (fn []
                         (when-not (empty? scanned-value)
                           (on-change scanned-value)))
                       [scanned-value])
        [rn/view {:style style/container}
         [rn/text-input
          {:accessibility-label    :address-text-input
           :style                  (style/input-text theme)
           :placeholder            (i18n/label :t/name-ens-or-address)
           :placeholder-text-color placeholder-text-color
           :default-value          @value
           :auto-complete          (when platform/ios? :none)
           :auto-capitalize        :none
           :auto-correct           false
           :keyboard-appearance    (quo.theme/theme-value :light :dark theme)
           :on-focus               on-focus
           :on-blur                on-blur
           :on-change-text         on-change}]
         (when (or (= @status :default)
                   (= @status :active))
           [rn/view
            {:style               style/buttons-container
             :accessibility-label :paste-scan-buttons-container}
            [button/button
             {:accessibility-label :paste-button
              :type                :outline
              :size                24
              :container-style     {:margin-right 8}
              :inner-style         (style/accessory-button blur? theme)
              :on-press            on-paste}
             (i18n/label :t/paste)]
            [button/button
             {:accessibility-label :scan-button
              :icon-only?          true
              :type                :outline
              :size                24
              :inner-style         (style/accessory-button blur? theme)
              :on-press            on-scan}
             :main-icons/scan]])
         (when (= @status :typing)
           [rn/view
            {:style               style/buttons-container
             :accessibility-label :clear-button-container}
            [clear-button
             {:on-press on-clear
              :blur?    blur?
              :theme    theme}]])
         (when (and (= @status :loading) (not valid-ens?))
           [rn/view
            {:style               style/buttons-container
             :accessibility-label :loading-button-container}
            [loading-icon blur? theme]])
         (when (and (= @status :loading) valid-ens?)
           [rn/view
            {:style               style/buttons-container
             :accessibility-label :positive-button-container}
            [positive-state-icon theme]])]))))

(defn address-input-internal
  [props]
  [:f> f-address-input-internal props])

(def address-input
  (quo.theme/with-theme address-input-internal))
