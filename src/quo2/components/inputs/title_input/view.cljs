(ns quo2.components.inputs.title-input.view
  (:require
    [quo2.components.inputs.title-input.style :as style]
    [quo2.components.markdown.text :as text]
    [reagent.core :as reagent]
    [react-native.core :as rn]
    [quo2.theme :as theme]))

(defn- pad-0
  [value]
  (if (<= (count value) 1)
    (str 0 value)
    value))

(defn title-input
  [{:keys [blur?
           on-change-text
           auto-focus
           placeholder
           max-length
           default-value
           override-theme]
    :or   {max-length    0
           auto-focus    false
           default-value ""}}]
  (let [focused?  (reagent/atom auto-focus)
        value     (reagent/atom default-value)
        on-change (fn [v]
                    (reset! value v)
                    (when on-change-text
                      (on-change-text v)))]
    (fn [{:keys [customization-color disabled?]}]
      [rn/view
       {:style style/container}
       [rn/view {:style style/text-input-container}
        [rn/text-input
         {:style
          (text/text-style
           {:size   :heading-2
            :weight :semi-bold
            :style  (style/title-text disabled? blur? override-theme)})
          :default-value default-value
          :accessibility-label :profile-title-input
          :keyboard-appearance (theme/theme-value :light :dark override-theme)
          :on-focus #(swap! focused? (fn [] true))
          :on-blur #(swap! focused? (fn [] false))
          :auto-focus auto-focus
          :input-mode :text
          :on-change-text on-change
          :editable (not disabled?)
          :max-length max-length
          :placeholder placeholder
          :selection-color (style/get-selection-color customization-color blur? override-theme)
          :placeholder-text-color (if @focused?
                                    (style/get-focused-placeholder-color blur? override-theme)
                                    (style/get-placeholder-color blur? override-theme))}]]
       [rn/view
        {:style style/counter-container}
        [text/text
         [text/text
          {:style (style/char-count blur? override-theme)
           :size  :paragraph-2}
          (pad-0
           (str
            (count @value)))]
         [text/text
          {:style (style/char-count blur? override-theme)
           :size  :paragraph-2}
          (str "/"
               (pad-0
                (str max-length)))]]]])))
