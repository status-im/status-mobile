(ns quo.components.list-items.status-list-item.view
  (:require
    [clojure.string :as string]
    [quo.components.icon :as quo.icons]
    [quo.components.list-items.status-list-item.schema :as component-schema]
    [quo.components.list-items.status-list-item.style :as style]
    [quo.components.markdown.text :as text]
    [quo.context :as quo.context]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [schema.core :as schema]))

(defn view-internal
  [{:keys [status title description]}]
  (let [theme      (quo.context/use-theme)
        positive?  (= status :positive)
        icon-color (if positive?
                     (colors/theme-colors colors/success-50 colors/success-60 theme)
                     (colors/theme-colors colors/danger-50 colors/danger-60 theme))]
    [rn/view
     {:style               style/container
      :accessibility-label :status-list-item}
     [quo.icons/icon (if positive? :i/correct :i/warning)
      {:size            20
       :color           icon-color
       :container-style {:margin-vertical 1}}]
     [rn/view
      {:style {:flex-direction :column :margin-left 8}}
      [text/text
       {:weight :regular
        :size   :paragraph-1
        :style  (style/title theme)} title]
      (when-not (string/blank? description)
        [text/text
         {:weight :regular
          :size   :paragraph-2
          :style  (style/description theme)}
         description])]]))

(def view (schema/instrument #'view-internal component-schema/?schema))
