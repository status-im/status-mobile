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

(defn- toggle-metrics
  [enabled?]
  (rf/dispatch [:centralized-metrics/toggle-centralized-metrics enabled?]))

(def ^:private will-receive-points
  [:t/ip-address
   :t/universally-unique-identifiers-of-device
   :t/logs-of-actions-withing-the-app])

(def ^:private not-receive-points
  [:t/your-profile-information
   :t/your-addresses
   :t/information-you-input-and-send])

(defn- bullet-points
  [{:keys [title points]}]
  [rn/view
   [quo/text {:weight :semi-bold}
    title]
   (for [label points]
     ^{:key label}
     [quo/markdown-list
      {:description     (i18n/label label)
       :blur?           true
       :container-style style/item-text}])])

(defn- on-share-usage
  []
  (toggle-metrics true)
  (hide-bottom-sheet))

(defn- on-do-not-share
  []
  (toggle-metrics false)
  (hide-bottom-sheet))

(declare view)

(defn- on-privacy-policy-press
  [settings?]
  (rf/dispatch
   [:show-bottom-sheet
    {:content  (fn []
                 [quo.theme/provider :dark
                  [privacy/privacy-statement]])
     :on-close (fn []
                 (rf/dispatch [:show-bottom-sheet
                               {:content (fn []
                                           [quo.theme/provider :dark
                                            [view {:settings? settings?}]])
                                :shell?  true}]))
     :shell?   true}]))

(defn- privacy-policy-text
  [settings?]
  [rn/view {:style style/privacy-policy}
   [quo/text
    [quo/text
     {:style {:color colors/white-opa-50}
      :size  :paragraph-2}
     (i18n/label :t/more-details-in-privacy-policy-1)]
    [quo/text
     {:size     :paragraph-2
      :weight   :bold
      :on-press #(on-privacy-policy-press settings?)}
     (i18n/label :t/more-details-in-privacy-policy-2)]]])

(defn view
  [{:keys [settings?]}]
  (rn/use-mount #(dismiss-keyboard))
  [:<>
   [quo/drawer-top
    {:title       (i18n/label :t/help-us-improve-status)
     :description (i18n/label :t/collecting-usage-data)}]
   [rn/view {:style style/points-wrapper}
    [bullet-points
     {:title  (i18n/label :t/what-we-will-receive)
      :points will-receive-points}]
    [bullet-points
     {:title  (i18n/label :t/what-we-wont-receive)
      :points not-receive-points}]
    (if settings?
      [quo/text
       {:size  :paragraph-2
        :style style/info-text}
       (i18n/label :t/usage-data-shared-from-all-profiles)]
      [quo/text
       {:size  :paragraph-2
        :style style/info-text}
       (i18n/label :t/usage-data-shared-from-all-profiles)
       (i18n/label :t/sharing-usage-data-can-be-turned-off)])]
   [quo/bottom-actions
    {:actions          :two-actions
     :blur?            true
     :button-one-label (i18n/label :t/share-usage-data)
     :button-one-props {:on-press on-share-usage}
     :button-two-label (i18n/label :t/not-now)
     :button-two-props {:type     :grey
                        :on-press on-do-not-share}}]
   [privacy-policy-text settings?]])
