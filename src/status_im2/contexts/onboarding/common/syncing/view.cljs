(ns status-im2.contexts.onboarding.common.syncing.view
  [:require
   [quo2.core :as quo]
   [utils.i18n :as i18n]
   [quo2.foundations.colors :as colors]
   [react-native.core :as rn]
   [status-im2.common.not-implemented :as not-implemented]
   [status-im2.contexts.onboarding.common.syncing.style :as style]])

;; TODO replace with section list component
;; https://github.com/status-im/status-mobile/issues/15665
(defn view
  [{:keys [name
           this-device?
           device-type]}]
  [rn/view {:style style/device-container}
   [rn/view {:style style/device-container-orientation}
    [rn/view {:style style/icon-container}
     [quo/icon
      (if (= device-type :mobile) :i/mobile :i/desktop)
      {:color colors/white}]]
    [rn/view
     [quo/text
      {:accessibility-label :device-name
       :weight              :medium
       :size                :paragraph-1
       :style               {:color colors/white}}
      name]
     (when this-device?
       [not-implemented/not-implemented
        [quo/text
         {:accessibility-label :next-back-up
          :size                :paragraph-2
          :style               {:color colors/white-opa-40}}
         "Next backup in 04:36:12"]])
     (when this-device?
       [rn/view {:style style/tag-container}
        [quo/status-tag
         {:size           :small
          :status         {:type :positive}
          :no-icon?       true
          :label          (i18n/label :t/this-device)
          :override-theme :dark}]])
     (when-not this-device?
       [rn/view {:style {:flex-direction :row}}
        [rn/view style/render-device-status]
        [quo/text
         {:accessibility-label :next-back-up
          :size                :paragraph-2
          :style               {:color colors/white-opa-40}}
         (i18n/label :t/online-now)]])]]])
