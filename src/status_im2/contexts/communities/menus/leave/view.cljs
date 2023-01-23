(ns status-im2.contexts.communities.menus.leave.view
  (:require [utils.i18n :as i18n]
            [quo2.core :as quo]
            [status-im2.contexts.communities.menus.leave.style :as style]
            [react-native.core :as rn]
            [utils.re-frame :as rf]))

(defn hide-sheet-and-dispatch
  [event]
  (rf/dispatch [:bottom-sheet/hide])
  (rf/dispatch event))

(defn leave-sheet
  [id]
  (let [{:keys [name images]} (rf/sub [:communities/community id])]
    [rn/view {:style style/container}
     [rn/view {:style style/inner-container}
      [quo/text
       {:accessibility-label :communities-join-community
        :weight              :semi-bold
        :size                :heading-1}
       (i18n/label :t/leave-community?)]]
     [quo/context-tag
      {:style
       {:margin-right :auto
        :margin-top   8}}
      (:thumbnail images) name]
     [quo/text
      {:accessibility-label :communities-join-community
       :size                :paragraph-1
       :style               style/text}
      (i18n/label :t/leave-community-message)]
     [rn/view
      {:style style/button-container}
      [quo/button
       {:on-press #(rf/dispatch [:bottom-sheet/hide])
        :type     :grey
        :style    style/cancel-button}
       (i18n/label :t/cancel)]
      [quo/button
       {:on-press #(hide-sheet-and-dispatch [:communities/leave id])
        :style    style/action-button}
       (i18n/label :t/leave-community)]]]))
