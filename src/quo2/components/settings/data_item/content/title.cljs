(ns quo2.components.settings.data-item.content.title
  (:require [react-native.core :as rn]
            [quo2.components.settings.data-item.style :as style]
            [quo2.components.markdown.text :as text]
            [utils.i18n :as i18n]))

(defn view
  [{:keys [title label size theme]}]
  [rn/view {:style style/title-container}
   [text/text
    {:weight :regular
     :size   :paragraph-2
     :style  (style/title theme)}
    title]
   (when (and (= :graph label) (not= :small size))
     [text/text
      {:weight :regular
       :size   :label
       :style  (style/title theme)}
      (i18n/label :t/days)])])
