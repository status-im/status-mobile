(ns quo2.components.tags.status-tags
  (:require [quo2.components.icon :as icon]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as quo2.theme]
            [react-native.core :as rn]))

(def default-container-style
  {:border-radius 20
   :border-width  1})

(def small-container-style
  (merge default-container-style
         {:padding-horizontal 8
          :padding-vertical   1}))

(def large-container-style
  (merge default-container-style
         {:padding-horizontal 11
          :padding-vertical   4}))

(defn base-tag
  [_]
  (fn
    [{:keys [size
             border-color
             background-color
             icon
             text-color
             label
             accessibility-label]}]
    (let [paragraph-size (if (= size :small) :paragraph-2 :paragraph-1)]
      [rn/view
       {:accessible          true
        :accessibility-label accessibility-label
        :style               (assoc (if (= size :small)
                                      small-container-style
                                      large-container-style)
                                    :border-width     1
                                    :border-color     border-color
                                    :background-color background-color)}
       [rn/view
        {:flex-direction :row
         :flex           1}
        [rn/view
         {:style {:justify-content :center
                  :align-items     :center}}
         [icon/icon
          icon
          {:no-color true
           :size     (if (= size :large) 20 12)}]]
        [text/text
         {:size   paragraph-size
          :weight :medium
          :style  {:padding-left 5
                   :color        text-color}} label]]])))

(defn- positive
  [size theme label _]
  [base-tag
   {:accessibility-label :status-tag-positive
    :size                size
    :icon                :i/positive-state
    :background-color    colors/success-50-opa-10
    :border-color        colors/success-50-opa-20
    :label               label
    ;; The positive tag uses the same color for `light` and `dark blur` variant
    :text-color          (if (= theme :dark) colors/success-60 colors/success-50)}])

(defn- negative
  [size theme label _]
  [base-tag
   {:accessibility-label :status-tag-negative
    :size                size
    :icon                :i/negative-state
    :background-color    colors/danger-50-opa-10
    :border-color        colors/danger-50-opa-20
    :label               label
    ;; The negative tag uses the same color for `dark` and `dark blur` variant
    :text-color          (if (= theme :light) colors/danger-50 colors/danger-60)}])

(defn- pending
  [size theme label blur?]
  [base-tag
   {:accessibility-label :status-tag-pending
    :size                size
    :label               label
    :icon                (if blur?
                           :i/pending-dark-blur
                           (if (= theme :light) :i/pending-light :i/pending-dark))
    :background-color    (if blur?
                           colors/white-opa-5
                           (colors/theme-colors colors/neutral-10 colors/neutral-80-opa-40 theme))
    :border-color        (if blur?
                           colors/white-opa-5
                           (colors/theme-colors colors/neutral-20 colors/neutral-80 theme))

    :text-color          (if blur?
                           colors/white-opa-70
                           (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))}])

(defn status-tag
  [{:keys [status size override-theme label blur?]}]
  (when status
    (when-let [status-component (case (:type status)
                                  :positive positive
                                  :negative negative
                                  :pending  pending
                                  nil)]
      [status-component
       size
       (or override-theme (quo2.theme/get-theme))
       label
       blur?])))
