(ns quo2.components.tags.status-tags
  (:require [quo2.components.icon :as icon]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [quo2.theme :as quo.theme]))

(def default-container-style
  {:border-radius 20
   :border-width  1})

(def small-container-style
  (merge default-container-style
         {:min-height         26
          :padding-horizontal 8
          :padding-vertical   3}))

(def large-container-style
  (merge default-container-style
         {:min-height         32
          :padding-horizontal 11
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
             accessibility-label
             container-style]}]
    (let [paragraph-size (if (= size :small) :paragraph-2 :paragraph-1)]
      [rn/view
       {:accessible          true
        :accessibility-label accessibility-label
        :style               (merge
                              (assoc (if (= size :small)
                                       small-container-style
                                       large-container-style)
                                     :align-self       :flex-start
                                     :border-width     1
                                     :border-color     border-color
                                     :background-color background-color)
                              container-style)}
       [rn/view
        {:flex-direction :row
         :align-items    :center
         :flex           1}
        [rn/view
         {:style {:justify-content :center
                  :align-items     :center}}
         (when icon
           [icon/icon
            icon
            {:no-color true
             :size     (if (= size :large) 20 12)}])]
        [text/text
         {:size   paragraph-size
          :weight :medium
          :style  {:padding-left (if icon 5 0)
                   :color        text-color}} label]]])))

(defn- positive
  [size theme label _ no-icon? container-style]
  [base-tag
   {:accessibility-label :status-tag-positive
    :container-style     container-style
    :size                size
    :icon                (when-not no-icon? :i/positive-state)
    :background-color    colors/success-50-opa-10
    :border-color        colors/success-50-opa-20
    :label               label
    ;; The positive tag uses the same color for `light` and `dark blur` variant
    :text-color          (if (= theme :dark) colors/success-60 colors/success-50)}])

(defn- negative
  [size theme label _ no-icon? container-style]
  [base-tag
   {:accessibility-label :status-tag-negative
    :container-style     container-style
    :size                size
    :icon                (when-not no-icon? :i/negative-state)
    :background-color    colors/danger-50-opa-10
    :border-color        colors/danger-50-opa-20
    :label               label
    ;; The negative tag uses the same color for `dark` and `dark blur` variant
    :text-color          (colors/theme-colors colors/danger-50 colors/danger-60 theme)}])

(defn- pending
  [size theme label blur? no-icon? container-style]
  [base-tag
   {:accessibility-label :status-tag-pending
    :container-style     container-style
    :size                size
    :label               label
    :icon                (when-not no-icon?
                           (if blur?
                             :i/pending-dark-blur
                             (if (= theme :light) :i/pending-light :i/pending-dark)))
    :background-color    (if blur?
                           colors/white-opa-5
                           (colors/theme-colors colors/neutral-10 colors/neutral-80-opa-40 theme))
    :border-color        (if blur?
                           colors/white-opa-5
                           (colors/theme-colors colors/neutral-20 colors/neutral-80 theme))

    :text-color          (if blur?
                           colors/white-opa-70
                           (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))}])

(defn- status-tag-internal
  [{:keys [status size theme label blur? no-icon? container-style]}]
  (when status
    (when-let [status-component (case (:type status)
                                  :positive positive
                                  :negative negative
                                  :pending  pending
                                  nil)]
      [status-component
       size
       theme
       label
       blur?
       no-icon?
       container-style])))

(def status-tag (quo.theme/with-theme status-tag-internal))
