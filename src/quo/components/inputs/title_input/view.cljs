(ns quo.components.inputs.title-input.view
  (:require
    [quo.components.icon :as icon]
    [quo.components.inputs.title-input.style :as style]
    [quo.components.markdown.text :as text]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]))

(defn- pad-0
  [value]
  (if (<= (count value) 1)
    (str 0 value)
    value))

(defn view
  [{:keys [blur? on-change-text auto-focus placeholder max-length default-value return-key-type
           size on-focus on-blur container-style customization-color disabled?]
    :or   {max-length    0
           auto-focus    false
           default-value ""}}]
  (let [theme                  (quo.theme/use-theme)
        [focused? set-focused] (rn/use-state auto-focus)
        [value set-value]      (rn/use-state default-value)
        input-ref              (rn/use-ref-atom nil)
        on-inpur-ref           (rn/use-callback #(reset! input-ref %))
        on-press               (rn/use-callback
                                #(when-not disabled? (.focus ^js @input-ref))
                                [disabled?])
        on-change              (rn/use-callback
                                (fn [v]
                                  (set-value v)
                                  (when on-change-text (on-change-text v))))
        on-focus               (rn/use-callback
                                (fn []
                                  (when (fn? on-focus) (on-focus))
                                  (set-focused true)))
        on-blur                (rn/use-callback
                                (fn []
                                  (when (fn? on-blur) (on-blur))
                                  (set-focused false)))]
    [rn/view
     {:style (merge (style/container disabled?) container-style)}
     [rn/view {:style style/text-input-container}
      [rn/text-input
       {:style                  (text/text-style
                                 {:size   (or size :heading-1)
                                  :weight :semi-bold
                                  :style  (style/title-text theme)}
                                 nil)
        :default-value          default-value
        :accessibility-label    :profile-title-input
        :keyboard-appearance    theme
        :return-key-type        return-key-type
        :on-focus               on-focus
        :on-blur                on-blur
        :auto-focus             auto-focus
        :input-mode             :text
        :on-change-text         on-change
        :editable               (not disabled?)
        :max-length             max-length
        :placeholder            placeholder
        :ref                    on-inpur-ref
        :selection-color        (style/get-selection-color customization-color blur? theme)
        :placeholder-text-color (if focused?
                                  (style/get-focused-placeholder-color blur? theme)
                                  (style/get-placeholder-color blur? theme))}]]
     [rn/view
      {:style (style/counter-container focused?)}
      (if focused?
        [text/text
         [text/text
          {:style (style/char-count blur? theme)
           :size  :paragraph-2}
          (pad-0
           (str
            (count value)))]
         [text/text
          {:style (style/char-count blur? theme)
           :size  :paragraph-2}
          (str "/" (pad-0 (str max-length)))]]
        [rn/pressable {:on-press on-press}
         [icon/icon :i/edit {:color (style/get-char-count-color blur? theme)}]])]]))
