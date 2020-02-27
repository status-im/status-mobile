(ns status-im.ui.components.button
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.utils.label :as utils.label]))

(defn style-container [type disabled? theme]
  (merge
   (when (= type :main)
     {:margin-vertical 8 :margin-horizontal 16})
   (cond
     (#{:main :secondary} type)
     {:padding-horizontal 32}
     (= :next type)
     {:padding-right 12 :padding-left 20}
     (= :previous type)
     {:padding-right 20 :padding-left 12}
     :else nil)
   {:height 44 :border-radius 8
    :align-items :center :justify-content :center
    :background-color (cond
                        (#{:secondary :next :previous} type)
                        ""
                        disabled?
                        colors/gray-transparent-10
                        (= type :main)
                        (case theme
                          :red colors/red-transparent-10
                          :green colors/green-transparent-10
                          colors/blue-transparent-10)
                        :else
                        "")}))

(defn button

  "A general purpose status-react specfic button component
  'type'
  :main (default), :secondary, :next, :previous

  `theme`
  :blue (default), :green, :red

  `label`
   Any one of string, keyword representing translated string in the form of :t/{translation-key-in-translation-files}

  `disabled?`
  Bool

  `on-press`
  Fn

  Spec: https://www.figma.com/file/cb4p8AxLtTF3q1L6JYDnKN15/Index?node-id=858%3A0"

  [{:keys [label type theme disabled? on-press accessibility-label style container-style text-style]
    :or   {type :main theme :blue}}]
  (let [label (utils.label/stringify label)]
    [react/touchable-opacity (cond-> {:on-press on-press
                                      :active-opacity 0.5
                                      :style style}
                               ;;NOTE `:disabled` must be of type boolean
                               disabled?
                               (assoc :disabled (boolean disabled?))
                               accessibility-label
                               (assoc :accessibility-label accessibility-label))
     [react/view {:style (merge (style-container type disabled? theme) container-style)}
      [react/view {:flex-direction :row :align-items :center}
       (when (= type :previous)
         [vector-icons/icon :main-icons/back {:container-style {:width 24 :height 24 :margin-right 4}
                                              :color           (if disabled? colors/gray colors/blue)}])
       [react/text {:style (merge {:color (cond
                                            disabled?
                                            colors/gray
                                            (#{:main :secondary :next :previous} type)
                                            (case theme
                                              :green colors/green
                                              :red   colors/red
                                              colors/blue)
                                            :else
                                            "")}
                                  text-style)}
        label]
       (when (= type :next)
         [vector-icons/icon :main-icons/next {:container-style {:width 24 :height 24 :margin-left 4}
                                              :color           (if disabled? colors/gray colors/blue)}])]]]))
