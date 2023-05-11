(ns quo2.components.inputs.recovery-phrase.view
  (:require [clojure.string :as string]
            [quo2.components.inputs.recovery-phrase.style :as style]
            [react-native.core :as rn]
            [reagent.core :as reagent]))

(def ^:private custom-props
  [:customization-color :override-theme :blur? :cursor-color :multiline :on-focus :on-blur
   :placeholder-text-color :mark-errors? :error-pred :word-limit])

(defn- error-word
  [text]
  [rn/text {:style (style/error-word)}
   text])

(defn- mark-error-words
  [pred text word-limit]
  (let [word-limit (or word-limit ##Inf)]
    (into [:<>]
          (comp (map-indexed (fn [idx word]
                               (if (or (pred word) (>= idx word-limit))
                                 [error-word word]
                                 word)))
                (interpose " "))
          (string/split text #" "))))

(defn recovery-phrase-input
  [_ _]
  (let [state       (reagent/atom :default)
        set-focused #(reset! state :focused)
        set-default #(reset! state :default)]
    (fn [{:keys [customization-color override-theme blur? on-focus on-blur mark-errors?
                 error-pred word-limit]
          :or   {customization-color :blue
                 error-pred          (constantly false)}
          :as   props}
         text]
      (let [extra-props (apply dissoc props custom-props)]
        [rn/view {:style style/container}
         [rn/text-input
          (merge {:accessibility-label    :recovery-phrase-input
                  :style                  (style/input)
                  :placeholder-text-color (style/placeholder-color @state override-theme blur?)
                  :cursor-color           (style/cursor-color customization-color override-theme)
                  :multiline              true
                  :on-focus               (fn []
                                            (set-focused)
                                            (when on-focus (on-focus)))
                  :on-blur                (fn []
                                            (set-default)
                                            (when on-blur (on-blur)))}
                 extra-props)
          (if mark-errors?
            (mark-error-words error-pred text word-limit)
            text)]]))))
