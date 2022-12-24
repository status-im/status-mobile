(ns status-im2.contexts.communities.context-drawers.menus.leave.view
  (:require [i18n.i18n :as i18n]
            [quo2.core :as quo]
            [react-native.core :as rn]
            [utils.re-frame :as rf]))

(defn hide-sheet-and-dispatch
  [event]
  (rf/dispatch [:bottom-sheet/hide])
  (rf/dispatch event))


(defn leave-sheet
  [id]
  [rn/view {:style {:height 160 :margin-left 20 :margin-right 20 :margin-bottom 20}}
   [rn/view {:style {:flex 1 :flex-direction :row :align-items :center :justify-content :space-between}}
    [quo/text
     {:accessibility-label :communities-join-community
      :weight              :semi-bold
      :size                :heading-1}
     (i18n/label :t/leave-community?)]]
   ;; TODO get tag image from community data
   #_[quo/context-tag
      {:style
       {:margin-right :auto
        :margin-top   8}}
      (resources/get-image :status-logo) (:name community)]
   [quo/text
    {:accessibility-label :communities-join-community
     :size                :paragraph-1
     :style               {:margin-top 16}}
    (i18n/label :t/leave-community-message)]
   [rn/view
    {:style {:margin-top      16
             :margin-bottom   16
             :flex            1
             :flex-direction  :row
             :align-items     :center
             :justify-content :space-evenly}}
    [quo/button
     {:on-press #(rf/dispatch [:bottom-sheet/hide])
      :type     :grey
      :style    {:flex         1
                 :margin-right 12}}
     (i18n/label :t/cancel)]
    [quo/button
     {:on-press #(hide-sheet-and-dispatch [:communities/leave id])
      :style    {:flex 1}}
     (i18n/label :t/leave-community)]]])
