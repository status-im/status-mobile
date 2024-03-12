(ns status-im.contexts.profile.settings.screens.password.change-password.loading
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [insets   (safe-area/get-insets)
        loading? (rf/sub [:settings/change-password-loading])]
    [quo/overlay {:type :shell}
     [rn/view
      {:style {:flex            1
               :justify-content :space-between
               :padding-top     (:top insets)
               :padding-bottom  (:bottom insets)}}

      [quo/page-nav]
      [quo/page-top
       {;; :input            nil
        :title            "sojfsofjo"
        :description      :text
        :description-text "shadhiof"}]
      [rn/view {:style {:flex 1}}
       (when loading?
         [rn/activity-indicator])]
      (when-not loading?
        [quo/logout-button
         {:container-style {:margin-horizontal 20
                            :margin-vertical   12}
          :on-press        (fn []
                             (rf/dispatch [:multiaccounts.logout.ui/logout-pressed])

                             (rf/dispatch [:password-settings/reset-change-password]))}])]]))
