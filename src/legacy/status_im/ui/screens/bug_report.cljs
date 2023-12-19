(ns legacy.status-im.ui.screens.bug-report
  (:require
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.topbar :as topbar]
    [re-frame.core :as re-frame]
    [react-native.core :as react-native]
    [utils.i18n :as i18n]))

(defn bug-report
  []
  (let [{:keys [description steps]} @(re-frame/subscribe [:bug-report/details])]
    [react-native/view {:style {:flex 1}}
     [topbar/topbar
      {:title  (i18n/label :t/bug-report)
       :modal? true}]
     [react-native/view
      {:style {:flex               1
               :padding-top        8
               :padding-horizontal 16}}
      [quo/text-input
       {:label               (i18n/label :t/bug-report-description)
        :default-value       description
        :placeholder         (i18n/label :t/bug-report-description-placeholder)
        :style               {:margin-bottom 8}
        :multiline           true
        :error               @(re-frame/subscribe [:bug-report/description-error])
        :accessibility-label :bug-report-description
        :on-change-text      #(re-frame/dispatch [:logging/report-details :description %])}]
      [quo/text-input
       {:label               (i18n/label :t/bug-report-steps)
        :default-value       steps
        :placeholder         (i18n/label :t/bug-report-steps-placeholder)
        :style               {:margin-bottom 16}
        :multiline           true
        :accessibility-label :bug-report-steps
        :on-change-text      #(re-frame/dispatch [:logging/report-details :steps %])}]
      [quo/button
       {:type                :primary
        :accessibility-label :bug-report-submit
        :theme               :accent
        :on-press            #(re-frame/dispatch [:logging/submit-report])}
       (i18n/label :t/bug-report-submit-email)]
      [react-native/view
       {:style {:margin-vertical 16
                :align-items     :center}}
       [quo/text (i18n/label :t/or)]]
      [quo/button
       {:type                :primary
        :accessibility-label :bug-report-submit-gh-issue
        :theme               :accent
        :on-press            #(re-frame/dispatch [:logging/submit-gh-issue])}
       (i18n/label :t/bug-report-submit-gh-issue)]]]))
