(ns status-im.common.metrics-confirmation-modal.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.common.metrics-confirmation-modal.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- hide-bottom-sheet
  []
  (rf/dispatch [:hide-bottom-sheet]))

(def will-receive-points
  [(i18n/label :t/ip-address)
   (i18n/label :t/universally-unique-identifiers-of-device)
   (i18n/label :t/logs-of-actions-withing-the-app)])

(def not-receive-points
  [(i18n/label :t/your-profile-information)
   (i18n/label :t/your-addresses)
   (i18n/label :t/information-you-input-and-send)])

(defn- render-points
  [{:keys [title points]}]
  [rn/view
   [quo/text {:weight :semi-bold}
    title]
   (map-indexed
    (fn [idx label]
      ^{:key (str idx label)}
      [quo/markdown-list
       {:description     label
        :blur?           true
        :container-style style/item-text}])
    points)])

(defn view
  [{:keys [settings?]}]
  (let [on-cancel       hide-bottom-sheet
        on-share-usage  (rn/use-callback
                         (fn []
                           (hide-bottom-sheet)))
        on-do-not-share (rn/use-callback
                         (fn []
                           (hide-bottom-sheet)))]
    [rn/view
     [quo/drawer-top
      {:title       (i18n/label :t/help-us-improve-status)
       :description (i18n/label :t/collecting-usage-data)}]
     [rn/view {:style style/points-wrapper}
      [render-points
       {:title  (i18n/label :t/what-we-will-receive)
        :points will-receive-points}]
      [render-points
       {:title  (i18n/label :t/what-we-not-receive)
        :points not-receive-points}]
      (when-not settings?
        [quo/text
         {:size  :paragraph-2
          :style style/info-text}
         (i18n/label :t/sharing-usage-data-can-be-turned-off)])]
     [quo/bottom-actions
      {:actions          :two-actions
       :blur?            true
       :button-one-label (i18n/label :t/share-usage-data)
       :button-one-props {:on-press on-share-usage}
       :button-two-label (i18n/label (if settings? :t/do-not-share :t/not-now))
       :button-two-props {:type     :grey
                          :on-press (if settings? on-do-not-share on-cancel)}}]]))
