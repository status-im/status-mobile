(ns quo2.components.inputs.browser-input.view
  (:require [quo2.components.icon :as icon]
            [quo2.components.inputs.browser-input.style :as style]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [quo2.foundations.colors :as colors]
            [react-native.platform :as platform]))

(defn clear-icon-color
  [blur? override-theme]
  (if blur?
    (colors/theme-colors colors/neutral-80-opa-30 colors/white-opa-10 override-theme)
    (colors/theme-colors colors/neutral-40 colors/neutral-60 override-theme)))

(defn lock-icon-color
  [blur? override-theme]
  (if blur?
    (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-40 override-theme)
    (colors/theme-colors colors/neutral-50 colors/neutral-40 override-theme)))

(defn- clear-button
  [{:keys [on-press blur? override-theme]}]
  [rn/touchable-opacity
   {:accessibility-label :browser-input-clear-button
    :on-press            on-press
    :style               style/clear-icon-container}
   [icon/icon :i/clear
    {:color (clear-icon-color blur? override-theme)
     :size  20}]])

(defn lock-icon
  [{:keys [blur? override-theme]}]
  [rn/view
   [icon/icon :i/locked
    {:accessibility-label :browser-input-locked-icon
     :color               (lock-icon-color blur? override-theme)
     :container-style     style/lock-icon-container
     :size                16}]])

(defn cursor-color
  [customization-color override-theme]
  (colors/theme-colors (colors/custom-color customization-color 50)
                       (colors/custom-color customization-color 60)
                       override-theme))

(defn placeholder-color
  [state blur? override-theme]
  (cond
    (and blur? (= state :active))
    (colors/theme-colors colors/neutral-80-opa-20 colors/white-opa-20 override-theme)

    blur?
    (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-30 override-theme)

    (= state :active)
    (colors/theme-colors colors/neutral-30 colors/neutral-60 override-theme)

    :else
    (colors/theme-colors colors/neutral-40 colors/neutral-50 override-theme)))

(def ^:private props-to-remove
  [:cursor-color :placeholder-text-color :editable :on-change-text :on-focus
   :on-blur :on-clear :value :disabled? :blur? :customization-color :override-theme])

(defn browser-input
  [{:keys [default-value]
    :or   {default-value ""}}]
  (let [state       (reagent/atom :default)
        value       (reagent/atom default-value)
        set-active  #(reset! state :active)
        set-default #(reset! state :default)
        set-value   #(reset! value %)
        ref         (atom nil)
        clear-input (fn []
                      (.clear ^js @ref)
                      (reset! value ""))]
    (fn [{:keys [disabled? blur? on-change-text customization-color
                 on-clear on-focus on-blur override-theme get-ref use-ssl?
                 favicon favicon-color favicon-size label]
          :or   {customization-color :blue}
          :as   props}]
      (let [clean-props (apply dissoc props props-to-remove)]
        [rn/view
         (when label
           [rn/view {:style style/label-container}
            (when favicon
              [icon/icon favicon
               {:accessibility-label :browser-input-favicon
                :color               favicon-color
                :container-style     style/favicon-icon-container
                :size                favicon-size}])
            [rn/text
             {:accessibility-label :browser-input-label
              :style               (style/text)} label]
            (when use-ssl?
              [lock-icon
               {:blur?          blur?
                :override-theme override-theme}])])
         [rn/view {:style style/input-container}
          [rn/text-input
           (merge
            clean-props
            {:accessibility-label    :browser-input
             :cursor-color           (cursor-color customization-color override-theme)
             :editable               (not disabled?)
             :keyboard-appearance    (colors/theme-colors :light :dark override-theme)
             :on-blur                (fn []
                                       (set-default)
                                       (when on-blur (on-blur)))
             :on-change-text         (fn [new-text]
                                       (set-value new-text)
                                       (when on-change-text (on-change-text new-text)))
             :on-focus               (fn []
                                       (set-active)
                                       (when on-focus (on-focus)))
             :placeholder-text-color (placeholder-color @state blur? override-theme)
             :ref                    (fn [r]
                                       (reset! ref r)
                                       (when get-ref (get-ref r)))
             :selection-color        (when platform/ios?
                                       (cursor-color customization-color override-theme))
             :style                  (style/input disabled?)})]
          (when (seq @value)
            [clear-button
             {:blur?          blur?
              :on-press       (fn []
                                (clear-input)
                                (when on-clear (on-clear)))
              :override-theme override-theme}])]]))))
