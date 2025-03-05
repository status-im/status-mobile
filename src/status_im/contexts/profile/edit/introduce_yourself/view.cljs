(ns status-im.contexts.profile.edit.introduce-yourself.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [utils.i18n :as i18n]
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
       (i18n/label :t/just-introduce-yourself)]
      [quo/text
       {:style  {:padding-top 4 :padding-bottom 12}
        :size   :paragraph-1
        :weight :regular}
       (i18n/label :t/add-display-name-and-picture)]]
     [quo/bottom-actions
      {:actions          :two-actions
       :buttons-style    {:flex-shrink 1 :flex-basis 0}
       :button-one-label (i18n/label :t/edit-profile)
       :button-one-props {:type     :primary
                          :on-press on-accept}
       :button-two-label (i18n/label :t/skip)
       :button-two-props {:type     :grey
                          :on-press on-reject}}]]))
