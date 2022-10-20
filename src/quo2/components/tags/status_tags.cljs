(ns quo2.components.tags.status-tags
  (:require [quo.react-native :as rn]
            [quo.theme :as quo.theme]
            [quo2.components.icon :as icon]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [status-im.i18n.i18n :as i18n]))

(def default-container-style
  {:border-radius 20
   :border-width 1})

(def small-container-style
  (merge default-container-style
         {:padding-horizontal 8
          :padding-vertical 3}))

(def large-container-style
  (merge default-container-style
         {:padding-horizontal 11
          :padding-vertical 4}))

(defn base-tag [_]
  (fn [{:keys [size
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
              :border-width 1
              :border-color border-color
              :background-color background-color)
       [rn/view {:flex-direction :row
                 :flex 1}
        [rn/view {:style {:justify-content :center
                          :align-items :center}}
         [icon/icon
          icon
          {:no-color true
           :size 12}]]
        [text/text {:size paragraph-size
                    :weight :medium
                    :style {:padding-left 5
                            :color text-color}} label]]])))

(defn- positive
  [size theme label]
  [base-tag {:size             size
             :background-color colors/success-50-opa-10
             :icon             :verified
             :border-color     colors/success-50-opa-20
             :label            (or label (i18n/label :positive))
             :text-color       (if (= theme :light) colors/success-50
                                   colors/success-60)}])

(defn- negative
  [size theme label]
  [base-tag {:size             size
             :icon             :untrustworthy
             :background-color colors/danger-50-opa-10
             :border-color     colors/danger-50-opa-20
             :label            (or label (i18n/label :negative))
             :text-color       (if (= theme :light)
                                 colors/danger-50
                                 colors/danger-60)}])

(defn- pending
  [size theme label]
  [base-tag {:size             size
             :icon             :pending
             :label            (or label (i18n/label :pending))
             :background-color (if (= theme :light)
                                 colors/neutral-10
                                 colors/neutral-80)
             :border-color     (if (= theme :light)
                                 colors/neutral-20
                                 colors/neutral-70)
             :text-color       colors/neutral-50}])

(defn status-tag [{:keys [status size override-theme label]}]
  (when status
    (when-let [status-component (case (:type status)
                                  :positive positive
                                  :negative negative
                                  :pending  pending
                                  nil)]
      [status-component
       size
       (or override-theme (quo.theme/get-theme))
       label])))
