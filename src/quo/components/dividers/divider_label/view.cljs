(ns quo.components.dividers.divider-label.view
  (:require
    [quo.components.counter.counter.view :as counter]
    [quo.components.dividers.divider-label.style :as style]
    [quo.components.icon :as icons]
    [quo.components.markdown.text :as text]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]))

(defn- view-internal
  "Options:
  - `counter?` (default: nil) - Display a counter on the right side of the divider.
  - `counter-value` (default: nil) - Number to display if counter? is true.
  - `chevron` nil/:right/:left (default: nil) - Display a chevron on the left or right side of the divider.
  - `chevron-icon` (default: :i/chevron-right) - Icon to display when chevron is :left/:right.
  - `tight?` (default: true) - Use a tighter padding.
  - `blur?` (default: nil) - Use a blur background.
  - `theme` - Theme value from with-theme HOC.
  - `on-press` - Function called when the divider is pressed.
  - `container-style` - Style applied to the container view.

  label - String or markdown text component to display in the divider label."
  [{:keys [counter? counter-value chevron chevron-icon tight? blur? on-press container-style]
    :or   {tight? true}}
   label]
  (let [theme (quo.theme/use-theme)]
    [rn/pressable
     {:on-press            on-press
      :accessibility-label :divider-label
      :style               (merge (style/container blur? tight? chevron theme)
                                  container-style)}
     [rn/view
      {:style (style/content chevron)}
      (when chevron
        [icons/icon (or chevron-icon :i/chevron-right)
         {:color           (style/get-content-color blur? theme)
          :container-style {(if (= chevron :right)
                              :margin-left
                              :margin-right)
                            2}}])
      [text/text
       {:size   :paragraph-2
        :weight :medium
        :style  (style/text blur? theme)}
       label]]
     (when counter?
       [counter/view
        {:type            (if blur? :secondary :grey)
         :container-style {:margin-left 2}}
        counter-value])]))

(defn view
  ([props label] [view-internal props label])
  ([label] [view {} label]))
