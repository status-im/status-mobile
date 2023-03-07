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
             label]}]
    (let [paragraph-size (if (= size :small) :paragraph-2 :paragraph-1)]
      [rn/view
       (assoc (if (= size :small)
                small-container-style
                large-container-style)
              :border-width     1
              :border-color     border-color
              :background-color background-color)
       [rn/view
        {:flex-direction :row
         :flex           1}
        [rn/view
         {:style {:justify-content :center
                  :align-items     :center}}
         [icon/icon
          icon
          {:no-color true
           :size     (if (= :large size) 20 12)}]]
        [text/text
         {:size   paragraph-size
          :weight :medium
          :style  {:padding-left 5
                   :color        text-color}} label]]])))

(defn- positive
  [size theme label blur?]
  [base-tag
   {:size             size
    :icon             :i/positive-state
    :background-color colors/success-50-opa-10
    :border-color     colors/success-50-opa-20
    :label            label
    :text-color       (cond
                        ;; The positive tag uses the same color for light themed and dark blur variant
                        (or (= theme :light) blur?) colors/success-50
                        (= theme :dark)             colors/success-60)}])

(defn- negative
  [size theme label blur?]
  [base-tag
   {:size             size
    :icon             :i/negative-state
    :background-color colors/danger-50-opa-10
    :border-color     colors/danger-50-opa-20
    :label            label
    :text-color       (cond
                        (= theme :light)           colors/danger-50
                        ;; The negative tag uses the same color for dark themed and dark blur variant
                        (or (= theme :dark) blur?) colors/danger-60)}])

(defn- get-color-or-icon-name
  [{:keys [blur? theme dark-blur-variant light-variant dark-variant]}]
  (if blur?
    ;; status tag support only dark blur variant
    dark-blur-variant
    (if (= theme :light) light-variant dark-variant)))

(defn- pending
  [size theme label blur?]
  [base-tag
   {:size             size
    :label            label
    :icon             (get-color-or-icon-name {:blur?             blur?
                                               :theme             theme
                                               :dark-blur-variant :i/pending-dark-blur
                                               :light-variant     :i/pending-light
                                               :dark-variant      :i/pending-dark})
    :background-color (get-color-or-icon-name {:blur?             blur?
                                               :theme             theme
                                               :dark-blur-variant colors/white-opa-5
                                               :light-variant     colors/neutral-10
                                               :dark-variant      colors/neutral-80-opa-40})
    :border-color     (get-color-or-icon-name {:blur?             blur?
                                               :theme             theme
                                               :dark-blur-variant colors/white-opa-5
                                               :light-variant     colors/neutral-20
                                               :dark-variant      colors/neutral-80})
    :text-color       (get-color-or-icon-name {:blur?             blur?
                                               :theme             theme
                                               :dark-blur-variant colors/white-opa-70
                                               :light-variant     colors/neutral-50
                                               :dark-variant      colors/neutral-40})}])

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
