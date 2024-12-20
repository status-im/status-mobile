(ns status-im.contexts.profile.edit.introduce-yourself.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [utils.re-frame :as rf]))

(defn sheet
  [{:keys [pending-event]}]
  (let [on-accept (fn []
                    (rf/dispatch [:open-modal :screen/profile.edit-profile-modal
                                  {:pending-event pending-event}]))
        on-reject #(rf/dispatch pending-event)]
    [:<>
     [rn/view {:style {:padding-horizontal 20}}
      [quo/text
       {:style  {:padding-bottom 4}
        :size   :heading-2
        :weight :semi-bold}
       "Introduce yourself"]
      [quo/text
       {:style  {:padding-top 4 :padding-bottom 12}
        :size   :paragraph-1
        :weight :regular}
       "Add an optional display name and profile picture so others can easily recognize you"]]
     [quo/bottom-actions
      {:actions          :two-actions
       :buttons-style    {:flex-shrink 1 :flex-basis 0}
       :button-one-label "Edit Profile"
       :button-one-props {:type     :primary
                          :on-press on-accept}
       :button-two-label "Skip"
       :button-two-props {:type     :grey
                          :on-press on-reject}}]]))
