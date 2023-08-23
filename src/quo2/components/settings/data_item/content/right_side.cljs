(ns quo2.components.settings.data-item.content.right-side
  (:require [react-native.core :as rn]
            [quo2.components.icon :as icons]
            [quo2.components.settings.data-item.style :as style]
            [quo2.components.markdown.text :as text]
            [quo2.components.list-items.preview-list :as preview-list]))

(defn view
  [{:keys [label icon-right? icon-color]}]
  [rn/view {:style style/right-container}
   (case label
     :preview [preview-list/preview-list
               {:type      :user
                :size      24
                :list-size 3}]
     :graph   [text/text "graph"]
     :none    nil
     nil)
   (when icon-right?
     [rn/view {:style (style/right-icon label)}
      [icons/icon
       (if (= :none label)
         :i/copy
         :i/chevron-right)
       {:accessibility-label :icon-right
        :color               icon-color
        :size                20}]])])
