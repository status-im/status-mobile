(ns status-im.contexts.settings.privacy-and-security.share-usage.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.events-helper :as events-helper]
    [status-im.common.privacy.view :as privacy]
    [status-im.contexts.settings.privacy-and-security.style :as privacy-and-security.style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def ^:private network-behavior-points
  [:t/number-of-messages-sent
   :t/connected-peers
   :t/successful-messages-rate
   :t/connection-type
   :t/os-app-version-bandwidth])

(def ^:private app-interactions-points
  [:t/action-logs
   :t/ip-addresses-uuid])

(def ^:private not-receive-points
  [:t/your-profile-information
   :t/your-addresses
   :t/information-you-input-and-send])

(defn- bullet-points
  [{:keys [title points on-toggle toggle-checked? description]}]
  [rn/view
   [rn/view {:flex-direction :row :justify-content :space-between :margin-bottom 8}
    [rn/view
     [quo/text {:weight :semi-bold}
      title]
     (when description
       [quo/text {:size :paragraph-2 :style {:color colors/white-opa-40}} description])]
    (when on-toggle
      [quo/selectors
       {:type      :toggle
        :on-change on-toggle
        :checked?  toggle-checked?}])]
   (for [label points]
     ^{:key label}
     [quo/markdown-list
      {:description     (i18n/label label)
       :blur?           true
       :container-style {}}])])

(defn- on-privacy-policy-press
  []
  (rf/dispatch
   [:show-bottom-sheet
    {:content (fn []
                [quo.theme/provider :dark
                 [privacy/privacy-statement]])
     :shell?  true}]))

(defn- privacy-policy-text
  []
  [rn/view {:style {}}
   [quo/text
    [quo/text
     {:style {:color colors/white-opa-50}
      :size  :paragraph-2}
     (i18n/label :t/more-details-in-privacy-policy-settings-1)]
    [quo/text
     {:size     :paragraph-2
      :weight   :bold
      :on-press on-privacy-policy-press}
     (i18n/label :t/more-details-in-privacy-policy-2)]]])

(defn view
  []
  (let [insets                       (safe-area/get-insets)
        telemetry-enabled?           (rf/sub [:profile/telemetry-enabled?])
        centralized-metrics-enabled? (rf/sub [:centralized-metrics/enabled?])]
    [quo/overlay
     {:type            :shell
      :container-style (privacy-and-security.style/page-wrapper (:top insets))}
     [quo/page-nav
      {:key        :header
       :background :blur
       :icon-name  :i/arrow-left
       :on-press   events-helper/navigate-back}]
     [quo/page-top
      {:title                     (i18n/label :t/share-usage-data)
       :title-accessibility-label :title-label
       :description               :text
       :description-text          (i18n/label :t/collecting-usage-data)}]
     [rn/view
      {:style {:padding-horizontal 20
               :padding-top        10
               :padding-bottom     (+ 10 (:bottom insets))
               :flex               1}}
      [rn/scroll-view {:style {:flex 1}}
       [bullet-points
        {:toggle-checked? telemetry-enabled?
         :on-toggle       #(rf/dispatch [:profile.settings/toggle-telemetry])
         :title           (i18n/label :t/network-behavior)
         :description     (i18n/label :t/will-be-shared-from-the-current-profile)
         :points          network-behavior-points}]
       [rn/view {:height 10}]
       [bullet-points
        {:toggle-checked? centralized-metrics-enabled?
         :on-toggle       #(rf/dispatch [:centralized-metrics/toggle-centralized-metrics
                                         (not centralized-metrics-enabled?)])
         :title           (i18n/label :t/app-interactions)
         :description     (i18n/label :t/will-be-shared-from-all-profiles)
         :points          app-interactions-points}]
       [rn/view {:height 20}]
       [bullet-points
        {:title  (i18n/label :t/what-we-wont-receive)
         :points not-receive-points}]]
      [rn/view {:align-items :center}
       [privacy-policy-text]]]]))
