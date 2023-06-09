(ns quo2.components.settings.category.view
  (:require [quo2.components.settings.category-list.style :as style]
            [quo2.components.settings.settings-list.view :as settings-list]
            [quo2.components.markdown.text :as text]
            [react-native.core :as rn]))

(defn category-label
  [title]
  [rn/view
   {:style style/title-container}
   [text/text
    {:accessibility-label :setting-item-name-text
     :ellipsize-mode      :tail
     :number-of-lines     1
     :style               (style/title)
     :weight              :medium
     :size                :paragraph-1}
    title]])

(defn category
  "Options
   - `label` label for list
   - `settings-list-data` vector of props to pass to individual settings list items
   - `container-style` map of values to pass to outer container"
  [{:keys [label
           settings-list-data
           container-style]}]
  [rn/view
   {:style (merge style/category-list-container container-style)}
   [category-label label]
   [rn/flat-list
    {:render-fn settings-list/settings-list
     :data settings-list-data
     :flex                         1
     :keyboard-should-persist-taps :always
     :key-fn                       str}]])
