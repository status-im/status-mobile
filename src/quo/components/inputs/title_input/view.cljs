(ns quo.components.inputs.title-input.view
  (:require
    [quo.components.icon :as icon]
    [quo.components.inputs.title-input.style :as style]
    [quo.components.markdown.text :as text]
    [quo.theme :as quo.theme]
    [react-native.pure :as rn.pure]))

(defn- pad-0
  [value]
  (if (<= (count value) 1)
    (str 0 value)
    value))

(defn- view-pure
  [{:keys [blur?
           on-change-text
           auto-focus
           placeholder
           max-length
           default-value
           return-key-type
           size
           on-focus
           on-blur
           container-style
           customization-color
           disabled?]
    :or   {max-length    0
           auto-focus    false
           default-value ""}}]
  (let [theme                  (quo.theme/use-theme)
        [focused? set-focused] (rn.pure/use-state auto-focus)
        [value set-value]      (rn.pure/use-state default-value)
        input-ref              (atom nil)
        on-change              (fn [v]
                                 (set-value v)
                                 (when on-change-text
                                   (on-change-text v)))]
    (rn.pure/view
     {:style (merge (style/container disabled?) container-style)}
     (rn.pure/view
      {:style style/text-input-container}
      (rn.pure/text-input
       {:style
        (text/text-style
         {:size   (or size :heading-1)
          :weight :semi-bold
          :style  (style/title-text theme)}
         nil)
        :default-value default-value
        :accessibility-label :profile-title-input
        :keyboard-appearance (quo.theme/theme-value :light :dark theme)
        :return-key-type return-key-type
        :on-focus (fn []
                    (when (fn? on-focus)
                      (on-focus))
                    (set-focused true))
        :on-blur (fn []
                   (when (fn? on-blur)
                     (on-blur))
                   (set-focused false))
        :auto-focus auto-focus
        :input-mode :text
        :on-change-text on-change
        :editable (not disabled?)
        :max-length max-length
        :placeholder placeholder
        :ref #(reset! input-ref %)
        :selection-color (style/get-selection-color customization-color blur? theme)
        :placeholder-text-color (if focused?
                                  (style/get-focused-placeholder-color blur? theme)
                                  (style/get-placeholder-color blur? theme))}))
     (rn.pure/view
      {:style (style/counter-container focused?)}
      (if focused?
        (text/text
         (text/text
          {:style (style/char-count blur? theme)
           :size  :paragraph-2}
          (pad-0
           (str
            (count value))))
         (text/text
          {:style (style/char-count blur? theme)
           :size  :paragraph-2}
          (str "/"
               (pad-0
                (str max-length)))))
        (rn.pure/pressable
         {:on-press #(when-not disabled? (.focus ^js @input-ref))}
         (icon/icon :i/edit {:color (style/get-char-count-color blur? theme)})))))))

(defn view [props] (rn.pure/func view-pure props))
