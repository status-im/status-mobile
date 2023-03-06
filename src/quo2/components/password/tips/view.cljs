(ns quo2.components.password.tips.view
  (:require [react-native.core :as rn]
            [quo2.components.password.tips.style :as style]
            [quo2.components.markdown.text :as text]))

(defn view
  "Options
   - `completed?` Password tip state
   
   `text` Password tip string
   "
  [{:keys [completed?]} text]
  [rn/view
   {:style               style/container
    :accessibility-label :password-tips}
   [text/text
    {:style  (style/tip-text completed?)
     :weight :regular
     :size   :paragraph-2} text]
   (when completed?
     [rn/view
      {:style               style/strike-through
       :accessibility-label :password-tips-completed}])])

