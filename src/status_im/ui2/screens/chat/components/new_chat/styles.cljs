(ns status-im.ui2.screens.chat.components.new-chat.styles)

(def contact-selection-heading
  {:style {:flex-direction     :row
           :justify-content    :space-between
           :align-items        :flex-end
           :padding-horizontal 20
           :margin-bottom      16}})

(def contact-selection-close
  {:width           32
   :height          32
   :border-radius   10
   :margin-left     20
   :margin-bottom   36
   :justify-content :center
   :align-items     :center})

(defn chat-button
  [{:keys [bottom]}]
  {:bottom (- bottom 50)})
