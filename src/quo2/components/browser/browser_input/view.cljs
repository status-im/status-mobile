(ns quo2.components.browser.browser-input.view
  (:require [quo2.components.icon :as icon]
            [quo2.components.browser.browser-input.style :as style]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as quo2.theme]
            [clojure.string :as string]
            [react-native.platform :as platform]))

(defn remove-http-https-www
  [value]
  (string/replace value #"(https?://(www\.)?|http://)" ""))

(defn clear-icon-color
  [blur? theme]
  (if blur?
    (colors/theme-colors colors/neutral-80-opa-30 colors/white-opa-10 theme)
    (colors/theme-colors colors/neutral-40 colors/neutral-60 theme)))

(defn lock-icon-color
  [blur? theme]
  (if blur?
    (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-40 theme)
    (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)))

(defn- clear-button
  [{:keys [on-press blur? theme]}]
  [rn/touchable-opacity
   {:accessibility-label :browser-input-clear-button
    :on-press            on-press
    :style               style/clear-icon-container}
   [icon/icon :i/clear
    {:color (clear-icon-color blur? theme)}]])

(defn lock-icon
  [{:keys [blur? theme]}]
  [rn/view
   [icon/icon :i/locked
    {:accessibility-label :browser-input-locked-icon
     :color               (lock-icon-color blur? theme)
     :container-style     style/lock-icon-container
     :size                16}]])

(defn cursor-color
  [customization-color theme]
  (colors/theme-colors (colors/custom-color customization-color 50)
                       (colors/custom-color customization-color 60)
                       theme))

(defn placeholder-color
  [state blur? theme]
  (cond
    (and blur? (= state :active))
    (colors/theme-colors colors/neutral-80-opa-20 colors/white-opa-20 theme)

    blur?
    (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-30 theme)

    (= state :active)
    (colors/theme-colors colors/neutral-30 colors/neutral-60 theme)

    :else
    (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)))

(def ^:private props-to-remove
  [:cursor-color :placeholder-text-color :editable :on-change-text :on-focus
   :on-blur :on-clear :value :disabled? :blur? :customization-color :theme])

(defn- view-internal
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
                      (reset! value ""))
        focus-input (fn []
                      (set-active)
                      (.focus ^js @ref))]
    (fn [{:keys [disabled? blur? on-change-text customization-color
                 on-clear on-focus on-blur theme get-ref locked?
                 favicon favicon-color favicon-size]
          :as   props}]
      (let [clean-props (apply dissoc props props-to-remove)]
        [rn/view {:style style/root-container}
         (when (and (seq @value) (= @state :default))
           [rn/touchable-opacity
            {:style    style/default-container
             :on-press focus-input}
            (when favicon
              [icon/icon favicon
               {:accessibility-label :browser-input-favicon
                :color               favicon-color
                :container-style     style/favicon-icon-container
                :size                favicon-size}])
            [rn/text
             {:accessibility-label :browser-input-label
              :style               (style/text)} (remove-http-https-www @value)]
            (when locked?
              [lock-icon
               {:blur? blur?
                :theme theme}])])
         [rn/view {:style (style/active-container (or (empty? @value) (= @state :active)))}
          [rn/text-input
           (merge
            clean-props
            {:accessibility-label    :browser-input
             :auto-capitalize        :none
             :auto-correct           false
             :cursor-color           (cursor-color customization-color theme)
             :editable               (not disabled?)
             :keyboard-appearance    (colors/theme-colors :light :dark theme)
             :keyboard-type          :web-search
             :on-blur                (fn []
                                       (set-default)
                                       (when on-blur (on-blur)))
             :on-change-text         (fn [new-text]
                                       (set-value new-text)
                                       (when on-change-text (on-change-text new-text)))
             :on-focus               (fn []
                                       (set-active)
                                       (when on-focus (on-focus)))
             :placeholder-text-color (placeholder-color @state blur? theme)
             :ref                    (fn [r]
                                       (reset! ref r)
                                       (when get-ref (get-ref r)))
             :selection-color        (when platform/ios?
                                       (cursor-color customization-color theme))
             :select-text-on-focus   true
             :style                  (style/input disabled?)})]
          (when (seq @value)
            [clear-button
             {:blur?    blur?
              :on-press (fn []
                          (clear-input)
                          (when on-clear (on-clear)))
              :theme    theme}])]]))))

(def view (quo2.theme/with-theme view-internal))
