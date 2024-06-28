(ns status-im.contexts.profile.settings.visibility-sheet.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.constants :as constants]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn change-visibility-status
  [status-type]
  (rf/dispatch
   [:visibility-status-updates/delayed-visibility-status-update status-type])
  (rf/dispatch [:hide-bottom-sheet]))

(defn view
  []
  (let [{:keys [public-key]}  (rf/sub [:profile/profile])
        {:keys [status-type]} (rf/sub
                               [:visibility-status-updates/visibility-status-update
                                public-key])
        customization-color   (rf/sub [:profile/customization-color])]
    (rn/use-mount
     (fn []
       (rf/dispatch [:peer-stats/get-count])))

    [quo/action-drawer
     [[{:icon                :i/online
        :no-icon-color?      true
        :blur?               true
        :state               (when (= status-type constants/visibility-status-always-online) :selected)
        :customization-color customization-color
        :accessibility-label :online
        :label               (i18n/label :t/online)
        :on-press            #(change-visibility-status constants/visibility-status-always-online)}
       {:icon                :i/offline
        :no-icon-color?      true
        :blur?               true
        :state               (when (= status-type constants/visibility-status-inactive) :selected)
        :accessibility-label :offline
        :customization-color customization-color
        :label               (i18n/label :t/offline)
        :sub-label           (i18n/label :t/status-inactive-subtitle)
        :on-press            #(change-visibility-status constants/visibility-status-inactive)}
       {:icon                :i/automatic
        :no-icon-color?      true
        :blur?               true
        :state               (when (= status-type constants/visibility-status-automatic) :selected)
        :accessibility-label :automatic
        :customization-color customization-color
        :label               (i18n/label :t/status-automatic)
        :sub-label           (i18n/label :t/status-automatic-subtitle)
        :on-press            #(change-visibility-status constants/visibility-status-automatic)}]]]))
