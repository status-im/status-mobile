(ns quo2.components.inputs.title-input.view
  (:require
    [quo2.components.inputs.title-input.style :as style]
    [quo2.components.markdown.text :as text]
    [reagent.core :as reagent]
    [react-native.core :as rn]
    [quo2.theme :as quo.theme]
    [quo2.components.icon :as icon]))

(defn- pad-0
  [value]
  (if (<= (count value) 1)
    (str 0 value)
    value))

(defn- view-internal
  [{:keys [blur?
           on-change-text
           auto-focus
           placeholder
           max-length
           default-value
           theme
           container-style]
    :or   {max-length    0
           auto-focus    false
           default-value ""}}]
  (let [focused?  (reagent/atom auto-focus)
        value     (reagent/atom default-value)
        input-ref (atom nil)
        on-change (fn [v]
                    (reset! value v)
                    (when on-change-text
                      (on-change-text v)))]
    (fn [{:keys [customization-color disabled?]}]
      [rn/view
       {:style (merge (style/container disabled?) container-style)}
       [rn/view {:style style/text-input-container}
        [rn/text-input
         {:style
          (text/text-style
           {:size   :heading-2
            :weight :semi-bold
            :style  (style/title-text theme)})
          :default-value default-value
          :accessibility-label :profile-title-input
          :keyboard-appearance (quo.theme/theme-value :light :dark theme)
          :on-focus #(swap! focused? (constantly true))
          :on-blur #(swap! focused? (constantly false))
          :auto-focus auto-focus
          :input-mode :text
          :on-change-text on-change
          :editable (not disabled?)
          :max-length max-length
          :placeholder placeholder
          :ref #(reset! input-ref %)
          :selection-color (style/get-selection-color customization-color blur? theme)
          :placeholder-text-color (if @focused?
                                    (style/get-focused-placeholder-color blur? theme)
                                    (style/get-placeholder-color blur? theme))}]]
       [rn/view
        {:style (style/counter-container @focused?)}
        (if @focused?
          [text/text
           [text/text
            {:style (style/char-count blur? theme)
             :size  :paragraph-2}
            (pad-0
             (str
              (count @value)))]
           [text/text
            {:style (style/char-count blur? theme)
             :size  :paragraph-2}
            (str "/"
                 (pad-0
                  (str max-length)))]]
          [rn/pressable
           {:on-press #(when-not disabled?
                         (.focus ^js @input-ref))}
           [icon/icon :i/edit {:color (style/get-char-count-color blur? theme)}]])]])))

(def view (quo.theme/with-theme view-internal))
