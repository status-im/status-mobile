(ns quo.components.password.tips.view
  (:require
    [quo.components.markdown.text :as text]
    [quo.components.password.tips.style :as style]
    [react-native.core :as rn]))

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
     :size   :paragraph-2}
    text]
   (when completed?
     [rn/view
      {:style               style/strike-through
       :accessibility-label :password-tips-completed}])])

