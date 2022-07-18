(ns quo2.components.activity-logs
  (:require [quo.react-native :as rn]
            [quo2.foundations.colors :as colors]
            [quo2.components.status-tags :as status-tags]
            [quo2.components.button :as quo2.button]
            [quo2.components.text :as text]
            [quo2.components.icon :as quo2.icons]))

(defn activity-logs [_]
  (fn [{:keys [title
               button-1
               button-2
               icon
               unread
               status
               message
               timestamp]}]
    [rn/view {:flex-direction :row
              :flex 1
              :border-radius 16
              :padding-top 8
              :padding-horizontal 12
              :padding-bottom 12
              :background-color (when unread
                                  colors/primary-50-opa-10)}
     [rn/view {:height 32
               :width 32
               :border-radius 100
               :margin-top 8
               :background-color colors/neutral-80-opa-60
               :flex-direction :column
               :align-items :center
               :justify-content :center}
      [quo2.icons/icon icon {:color colors/white}]]
     [rn/view {:flex-direction :column
               :padding-left 8
               :flex 1}
      [rn/view {:flex 1
                :flex-wrap :wrap}
       [rn/view {:flex-direction :row}
        [text/text {:weight :semi-bold
                    :style {:color colors/white}
                    :size   :paragraph-1}
         title]
        [rn/view {:margin-left 8
                  :margin-top 5}
         [text/text {:size :label
                     :style {:text-transform :none
                             :color colors/neutral-40}} timestamp]]]]
      (when message
        [rn/view {:border-radius 12
                  :margin-top 13
                  :padding-horizontal 12
                  :padding-vertical 8
                  :background-color :red}
         [text/text {:style {:color colors/white}
                     :size :paragraph-1}
          message]])
      (when status
        [rn/view {:padding-top 10
                  :align-items :flex-start}
         [status-tags/status-tag {:size :small
                                  :override-theme :dark
                                  :status status}]])
      (when (or button-1 button-2)
        [rn/view {:padding-top 10
                  :flex 1
                  :flex-direction :row
                  :align-items :flex-start}
         (when button-1
           [quo2.button/button
            (assoc button-1
                   :override-them :dark
                   :style {:margin-right 8})
            (:label button-1)])
         (when button-2
           [quo2.button/button
            (assoc button-2
                   :override-theme
                   :dark)
            (:label button-2)])])]]))
