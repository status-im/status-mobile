(ns quo.components.browser.browser-input.view
  (:require
    [clojure.string :as string]
    [quo.components.browser.browser-input.style :as style]
    [quo.components.icon :as icon]
    [quo.context :as quo.context]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
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
  (colors/resolve-color customization-color theme))

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

(defn view
  [{:keys [disabled? blur? on-change-text customization-color
           on-clear on-focus on-blur get-ref locked?
           favicon favicon-color favicon-size default-value]
    :or   {default-value ""}
    :as   props}]
  (let [ref               (rn/use-ref-atom nil)
        on-ref            (rn/use-callback
                           (fn [r]
                             (reset! ref r)
                             (when get-ref (get-ref r)))
                           [get-ref])
        theme             (quo.context/use-theme)
        [state set-state] (rn/use-state :default)
        [value set-value] (rn/use-state default-value)
        on-clear          (rn/use-callback
                           (fn []
                             (.clear ^js @ref)
                             (set-value "")
                             (when on-clear (on-clear)))
                           [on-clear])
        focus-input       (rn/use-callback
                           (fn []
                             (set-state :active)
                             (.focus ^js @ref)))
        on-blur           (rn/use-callback
                           (fn []
                             (set-state :default)
                             (when on-blur (on-blur)))
                           [on-blur])
        on-change-text    (rn/use-callback
                           (fn [new-text]
                             (set-value new-text)
                             (when on-change-text (on-change-text new-text)))
                           [on-change-text])
        on-focus          (rn/use-callback
                           (fn []
                             (set-state :active)
                             (when on-focus (on-focus)))
                           [on-focus])
        clean-props       (apply dissoc props props-to-remove)]
    [rn/view {:style style/root-container}
     (when (and (seq value) (= state :default))
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
          :style               (style/text theme)}
         (remove-http-https-www value)]
        (when locked?
          [lock-icon {:blur? blur? :theme theme}])])
     [rn/view {:style (style/active-container (or (empty? value) (= state :active)))}
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
         :on-blur                on-blur
         :on-change-text         on-change-text
         :on-focus               on-focus
         :placeholder-text-color (placeholder-color state blur? theme)
         :ref                    on-ref
         :selection-color        (when platform/ios?
                                   (cursor-color customization-color theme))
         :select-text-on-focus   true
         :style                  (style/input disabled?)})]
      (when (seq value)
        [clear-button
         {:blur?    blur?
          :on-press on-clear
          :theme    theme}])]]))
