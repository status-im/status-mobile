(ns quo.components.button.view
  (:require [quo.components.text :as text] ;; FIXME:
            [quo.design-system.colors :as colors]
            [quo.design-system.spacing :as spacing]
            [quo.haptic :as haptic]
            [quo.react-native :as rn]
            [status-im.ui.components.icons.icons :as icons]))

(defn style-container
  [type]
  (merge {:height          44
          :align-items     :center
          :justify-content :center
          :flex-direction  :row}
         (case type
           :primary   (:base spacing/padding-horizontal)
           :secondary (:x-tiny spacing/padding-horizontal)
           :icon      {}
           nil)))

(defn content-style
  [type]
  (case type
    :primary   (:base spacing/padding-horizontal)
    :secondary (:x-tiny spacing/padding-horizontal)
    :icon      (:tiny spacing/padding-horizontal)
    nil))

(defn themes
  [theme]
  (case theme
    :main         {:icon-color       (:icon-04 @colors/theme)
                   :background-color (:interactive-02 @colors/theme)
                   :text-color       (:text-04 @colors/theme)}
    :icon         {:icon-color       (:icon-01 @colors/theme)
                   :background-color (:interactive-02 @colors/theme)
                   :text-color       (:text-01 @colors/theme)}
    :negative     {:icon-color       (:negative-01 @colors/theme)
                   :background-color (:negative-02 @colors/theme)
                   :text-color       (:negative-01 @colors/theme)}
    :positive     {:icon-color       (:positive-01 @colors/theme)
                   :background-color (:positive-02 @colors/theme)
                   :text-color       (:positive-01 @colors/theme)}
    :accent       {:icon-color       (:icon-05 @colors/theme)
                   :background-color (:interactive-01 @colors/theme)
                   :text-color       (:text-05 @colors/theme)}
    :secondary    {:icon-color       (:icon-02 @colors/theme)
                   :background-color (:interactive-02 @colors/theme)
                   :text-color       (:text-02 @colors/theme)}
    :disabled     {:icon-color       (:icon-02 @colors/theme)
                   :background-color (:ui-01 @colors/theme)
                   :text-color       (:text-02 @colors/theme)}
    :monocromatic {:icon-color       (:icon-01 @colors/theme)
                   :background-color (:ui-background @colors/theme)
                   :text-color       (:text-01 @colors/theme)
                   :border-color     (:ui-01 @colors/theme)}))

(defn button
  [{:keys [on-press disabled type theme before after
           haptic-feedback haptic-type on-long-press on-press-start
           accessibility-label loading border-radius style test-ID]
    :or   {theme           :main
           type            :primary
           haptic-feedback true
           border-radius   8
           haptic-type     :selection}}
   children]
  (let [theme' (cond
                 disabled :disabled
                 :else    theme)
        {:keys [icon-color background-color text-color border-color]}
        (themes theme')

        optional-haptic (fn []
                          (when haptic-feedback
                            (haptic/trigger haptic-type)))]
    [rn/pressable
     (merge {:bg-color            background-color
             :border-radius       border-radius
             :type                type
             :disabled            disabled
             :accessibility-label accessibility-label}
            (when border-color
              {:border-color border-color
               :border-width 1})
            (when on-press
              {:on-press (fn []
                           (optional-haptic)
                           (on-press))})
            (when on-long-press
              {:on-long-press (fn []
                                (optional-haptic)
                                (on-long-press))})
            (when on-press-start
              {:on-press-start (fn []
                                 (optional-haptic)
                                 (on-press-start))}))
     [rn/view {:test-ID test-ID :style (merge (style-container type) style)}
      (when before
        [rn/view
         [icons/icon before {:color icon-color}]])
      (when loading
        [rn/view {:style {:position :absolute}}
         [rn/activity-indicator]])
      [rn/view
       {:style (merge (content-style type)
                      (when loading
                        {:opacity 0}))}
       (cond
         (= type :icon)
         [icons/icon children {:color icon-color}]

         (string? children)
         [text/text
          {:weight          :medium
           :number-of-lines 1
           :style           {:color text-color}}
          children]

         (vector? children)
         children)]
      (when after
        [rn/view
         [icons/icon after {:color icon-color}]])]]))
