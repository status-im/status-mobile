(ns status-im.common.metrics-confirmation-modal.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.core :as rn]
    [status-im.common.metrics-confirmation-modal.style :as style]
    [status-im.common.privacy.view :as privacy]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- dismiss-keyboard
  []
  (rf/dispatch [:dismiss-keyboard]))

(defn- hide-bottom-sheet
  []
  (rf/dispatch [:hide-bottom-sheet]))

(def ^:private will-receive-for-current-points
  [:t/number-of-messages-sent
   :t/connected-peers
   :t/successful-messages-rate
   :t/connection-type
   :t/os-app-version-bandwidth])

(def ^:private will-receive-for-all-points
  [:t/action-logs
   :t/ip-addresses-uuid])

(def ^:private not-receive-points
  [:t/your-profile-information
   :t/your-addresses
   :t/information-you-input-and-send])

(defn- bullet-points
  [{:keys [title points lock?]}]
  [rn/view
   [quo/text {:weight :semi-bold}
    title]
   (for [label points]
     ^{:key label}
     [quo/markdown-list
      {:description     (i18n/label label)
       :blur?           true
       :type            (when lock? :lock)
       :container-style style/item-text}])])

(defn- on-share-usage
  []
  (rf/dispatch [:centralized-metrics/toggle-centralized-metrics true true])
  (hide-bottom-sheet))

(defn- on-do-not-share
  []
  (rf/dispatch [:centralized-metrics/toggle-centralized-metrics false true])
  (hide-bottom-sheet))

(declare view)

(defn- on-privacy-policy-press
  []
  (rf/dispatch
   [:show-bottom-sheet
    {:content  privacy/privacy-statement
     :on-close (fn []
                 (rf/dispatch [:show-bottom-sheet
                               {:content view
                                :theme   :dark
                                :shell?  true}]))
     :theme    :dark
     :shell?   true}]))

(defn- privacy-policy-text
  []
  [rn/view {:style style/privacy-policy}
   [quo/text
    [quo/text
     {:style {:color colors/white-opa-50}
      :size  :paragraph-2}
     (i18n/label :t/more-details-in-privacy-policy-1)]
    [quo/text
     {:size     :paragraph-2
      :weight   :bold
      :on-press on-privacy-policy-press}
     (i18n/label :t/more-details-in-privacy-policy-2)]]])

(defn view
  []
  (rn/use-mount #(dismiss-keyboard))
  [:<>
   [quo/drawer-top
    {:title       (i18n/label :t/help-us-improve-status)
     :description (i18n/label :t/collecting-usage-data)}]
   [rn/scroll-view
    [rn/view {:style style/points-wrapper}
     [bullet-points
      {:title  (i18n/label :t/we-will-receive-from-all-profiles)
       :points will-receive-for-all-points}]
     [bullet-points
      {:title  (i18n/label :t/we-will-receive-from-the-current-profile)
       :points will-receive-for-current-points}]
     [bullet-points
      {:title  (i18n/label :t/what-we-wont-receive)
       :points not-receive-points
       :lock?  true}]
     [quo/text
      {:size  :paragraph-2
       :style style/info-text}
      (i18n/label :t/sharing-usage-data-can-be-turned-off)]]]
   [quo/bottom-actions
    {:actions          :two-actions
     :blur?            true
     :button-one-label (i18n/label :t/help-us-improve-status)
     :button-one-props {:on-press on-share-usage}
     :button-two-label (i18n/label :t/not-now)
     :button-two-props {:type     :grey
                        :on-press on-do-not-share}}]
   [privacy-policy-text]])
